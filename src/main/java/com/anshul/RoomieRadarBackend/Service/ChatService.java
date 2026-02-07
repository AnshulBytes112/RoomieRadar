package com.anshul.RoomieRadarBackend.Service;

import com.anshul.RoomieRadarBackend.Model.Conversation;
import com.anshul.RoomieRadarBackend.Model.Message;
import com.anshul.RoomieRadarBackend.Model.MessageRequest;
import com.anshul.RoomieRadarBackend.dto.*;
import com.anshul.RoomieRadarBackend.entity.User;
import com.anshul.RoomieRadarBackend.repository.ConversationRepository;
import com.anshul.RoomieRadarBackend.repository.MessageRepository;
import com.anshul.RoomieRadarBackend.repository.MessageRequestRepository;
import com.anshul.RoomieRadarBackend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final MessageRequestRepository reqRepo;
    private final ConversationRepository convRepo;
    private final MessageRepository msgRepo;
    private final UserRepository userRepo; // your existing user repo
    private final SimpMessagingTemplate messagingTemplate; // for websocket pushing

    public MessageRequest createRequestByEmail(String email, CreateRequestDTO dto) {
        // find current user
        var fromUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        // find target user
        var toUser = userRepo.findById(dto.getToUserId())
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        // prevent self-request
        if (fromUser.getId().equals(toUser.getId()))
            throw new IllegalArgumentException("Cannot send request to yourself");
        // prevent duplicate pending
        var existing = reqRepo.findByFromUserAndToUserAndStatus(fromUser, toUser, MessageRequest.RequestStatus.PENDING);
        if (existing.isPresent()) {
            return null;
        }
        // create request
        MessageRequest request = new MessageRequest();
        request.setFromUser(fromUser);
        request.setToUser(toUser);
        request.setStatus(MessageRequest.RequestStatus.PENDING);
        request.setCreatedAt(Instant.now());
        request.setMessage(dto.getMessage());

        return reqRepo.save(request);
    }

    // Transactional annotation helps in completeing a process if it fails in
    // between the whole process reverts back
    @Transactional
    public MessageRequest createRequest(Long fromUserId, CreateRequestDTO dto) {
        User from = userRepo.findById(fromUserId).orElseThrow(() -> new EntityNotFoundException("From user"));
        User to = userRepo.findById(dto.getToUserId()).orElseThrow(() -> new EntityNotFoundException("To user"));

        // prevent self-request
        if (from.getId().equals(to.getId()))
            throw new IllegalArgumentException("Cannot send request to yourself");

        // prevent duplicate pending
        var existing = reqRepo.findByFromUserAndToUserAndStatus(from, to, MessageRequest.RequestStatus.PENDING);
        if (existing.isPresent())
            throw new IllegalStateException("Request already pending");

        // saves a message request sent by user
        MessageRequest mr = new MessageRequest();
        mr.setFromUser(from);
        mr.setToUser(to);
        mr.setMessage(dto.getMessage());
        return reqRepo.save(mr);
    }

    @Transactional
    public Conversation respondToRequest(Long toUserId, Long requestId, ResponseDTO dto) {
        MessageRequest req = reqRepo.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Request not found"));
        if (!req.getToUser().getId().equals(toUserId))
            throw new AccessDeniedException("Not allowed");
        if (req.getStatus() != MessageRequest.RequestStatus.PENDING)
            throw new IllegalStateException("Already responded");

        if (dto.isAccept()) {
            req.setStatus(MessageRequest.RequestStatus.ACCEPTED);
            req.setRespondedAt(Instant.now());
            reqRepo.save(req);
            // now that request is accepted create a conversation between the two users if
            // not already present
            // find or create conversation
            Long a = req.getFromUser().getId();
            Long b = req.getToUser().getId();
            // canonical order not required for query, but we use findBetweenUsers
            // to ensure uniqueness
            // find existing conversation
            // or create new one
            var convOpt = convRepo.findBetweenUsers(a, b);
            Conversation conv = convOpt.orElseGet(() -> {
                Conversation c = new Conversation();
                c.getParticipants().add(req.getFromUser());
                c.getParticipants().add(req.getToUser());
                return convRepo.save(c);
            });

            // Notify the requester in real-time (if connected)
            messagingTemplate.convertAndSend(
                    "/topic/requests/" + req.getFromUser().getId(),
                    Map.of("type", "REQUEST_ACCEPTED", "conversationId", conv.getId()));

            return conv;
        } else {
            req.setStatus(MessageRequest.RequestStatus.REJECTED);
            req.setRespondedAt(Instant.now());
            reqRepo.save(req);
            // optionally notify
            messagingTemplate.convertAndSend(
                    "/topic/requests/" + req.getFromUser().getId(),
                    Map.of("type", "REQUEST_REJECTED", "requestId", req.getId()));
            return null;
        }
    }

    @Transactional
    public MessageDTO sendMessage(Long senderId, Long conversationId, SendMessageDTO dto) {
        Conversation conv = convRepo.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));
        if (!conv.hasParticipant(senderId))
            throw new AccessDeniedException("Not a participant");

        User sender = userRepo.findById(senderId).orElseThrow(() -> new EntityNotFoundException("Sender not found"));

        Message m = new Message();
        m.setConversation(conv);
        m.setSender(sender);
        m.setContent(dto.getMessage());
        m = msgRepo.save(m);

        conv.setLastMessageAt(Instant.now());
        convRepo.save(conv);

        // publish to websocket topic for this conversation
        String topic = "/topic/conversations." + conv.getId();
        MessageDTO messageDto = new MessageDTO(m.getId(), sender.getId(), m.getContent(), m.getCreatedAt());
        messagingTemplate.convertAndSend(topic, Map.of(
                "type", "NEW_MESSAGE",
                "message", messageDto));
        return messageDto;
    }

    @Transactional(readOnly = true)
    public List<MessageRequest> getPendingRequestsFor(Long toUserId) {
        User to = userRepo.getReferenceById(toUserId);
        return reqRepo.findByToUserAndStatus(to, MessageRequest.RequestStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<ConversationDTO> getConversationsFor(Long userId, String email) {
        List<Conversation> convs = convRepo.findByParticipant(userId);
        return convs.stream().map(c -> {
            ConversationDTO dto = new ConversationDTO();
            dto.setId(c.getId());
            dto.setLastMessageAt(c.getLastMessageAt());

            // Participant Details
            User otherUser = c.getParticipants().stream()
                    .filter(u -> !u.getEmail().equals(email))
                    .findFirst()
                    .orElse(null);

            if (otherUser != null) {
                var rp = otherUser.getRoomateProfile();
                Instant lastActive = otherUser.getLastActive();
                boolean isActive = lastActive != null && lastActive.isAfter(Instant.now().minusSeconds(90));

                ConversationDTO.ParticipantDTO participant = new ConversationDTO.ParticipantDTO(
                        otherUser.getId(),
                        (rp != null && rp.getName() != null) ? rp.getName() : otherUser.getName(),
                        (rp != null && rp.getAvatar() != null) ? rp.getAvatar() : "",
                        lastActive,
                        isActive);
                dto.setOtherParticipant(participant);
            }

            // Latest Message Snippet
            msgRepo.findFirstByConversationOrderByCreatedAtDesc(c).ifPresent(m -> {
                dto.setLastMessage(new ConversationDTO.LastMessageDTO(m.getContent(), m.getCreatedAt()));
            });

            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MessageDTO> getMessages(Long conversationId, Long userId) {
        Conversation conv = convRepo.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));
        if (!conv.hasParticipant(userId))
            throw new AccessDeniedException("Not a participant");
        return msgRepo.findByConversationOrderByCreatedAtAsc(conv).stream()
                .map(m -> new MessageDTO(m.getId(), m.getSender().getId(), m.getContent(), m.getCreatedAt()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPendingRequestsForEmail(String email) {
        User user = userRepo.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<MessageRequest> list = reqRepo.findByToUserAndStatus(user, MessageRequest.RequestStatus.PENDING);
        return list.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("fromUserId", r.getFromUser() != null ? r.getFromUser().getId() : null);
            map.put("fromEmail", r.getFromUser() != null ? r.getFromUser().getEmail() : null);
            map.put("fromName", r.getFromUser() != null ? r.getFromUser().getName() : null);
            map.put("message", r.getMessage());
            map.put("createdAt", r.getCreatedAt());
            return map;
        }).collect(Collectors.toList());
    }

    public Conversation respondToRequestByEmail(String email, Long id, ResponseDTO dto) {
        var user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        return respondToRequest(user.getId(), id, dto);
    }

    /**
     * Return true if a conversation between the two usernames exists (i.e. the
     * message
     * request was accepted and a private conversation was created).
     */
    public boolean canPrivateChat(String senderEmail, String receiverEmail) {
        var sender = userRepo.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + senderEmail));
        var receiver = userRepo.findByEmail(receiverEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + receiverEmail));

        return convRepo.findBetweenUsers(sender.getId(), receiver.getId()).isPresent();
    }

    @Transactional(readOnly = true)
    public List<ConversationDTO> getConversationsForEmail(String email) {
        var user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        return getConversationsFor(user.getId(), email);
    }

    @Transactional(readOnly = true)
    public List<MessageDTO> getMessagesForEmail(Long id, String email) {
        var user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        return getMessages(id, user.getId());
    }

    @Transactional
    public MessageDTO sendMessageByEmail(String email, Long id, SendMessageDTO dto) {
        var user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        return sendMessage(user.getId(), id, dto);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSentRequestsForEmail(String email) {
        User user = userRepo.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<MessageRequest> list = reqRepo.findByFromUserAndStatusNot(user, MessageRequest.RequestStatus.REJECTED);
        return list.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("toUserId", r.getToUser() != null ? r.getToUser().getId() : null);
            map.put("toEmail", r.getToUser() != null ? r.getToUser().getEmail() : null);
            map.put("toName", r.getToUser() != null ? r.getToUser().getName() : null);
            map.put("message", r.getMessage());
            map.put("status", r.getStatus());
            map.put("createdAt", r.getCreatedAt());
            return map;
        }).collect(Collectors.toList());
    }

    public void deleteRequest(Long id) {
        MessageRequest req = reqRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Request not found"));
        reqRepo.delete(req);
    }
}
