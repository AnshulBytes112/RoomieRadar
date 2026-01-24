package com.anshul.RoomieRadarBackend.Service;

import com.anshul.RoomieRadarBackend.Model.Conversation;
import com.anshul.RoomieRadarBackend.Model.Message;
import com.anshul.RoomieRadarBackend.Model.MessageRequest;
import com.anshul.RoomieRadarBackend.dto.CreateRequestDTO;
import com.anshul.RoomieRadarBackend.dto.ResponseDTO;
import com.anshul.RoomieRadarBackend.dto.SendMessageDTO;
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
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final MessageRequestRepository reqRepo;
    private final ConversationRepository convRepo;
    private final MessageRepository msgRepo;
    private final UserRepository userRepo; // your existing user repo
    private final SimpMessagingTemplate messagingTemplate; // for websocket pushing

    public MessageRequest createRequestByUsername(String username, CreateRequestDTO dto) {
        // find current user
        var fromUser = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

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
    public Message sendMessage(Long senderId, Long conversationId, SendMessageDTO dto) {
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
        messagingTemplate.convertAndSend(topic, Map.of(
                "type", "NEW_MESSAGE",
                "message", Map.of("id", m.getId(), "senderId", sender.getId(), "content", m.getContent(), "createdAt",
                        m.getCreatedAt())));
        return m;
    }

    @Transactional(readOnly = true)
    public List<MessageRequest> getPendingRequestsFor(Long toUserId) {
        User to = userRepo.getReferenceById(toUserId);
        return reqRepo.findByToUserAndStatus(to, MessageRequest.RequestStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<Conversation> getConversationsFor(Long userId) {
        return convRepo.findByParticipant(userId);
    }

    @Transactional(readOnly = true)
    public List<Message> getMessages(Long conversationId, Long userId) {
        Conversation conv = convRepo.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));
        if (!conv.hasParticipant(userId))
            throw new AccessDeniedException("Not a participant");
        return msgRepo.findByConversationOrderByCreatedAtAsc(conv);
    }

    public List<MessageRequest> getPendingRequestsForUsername(String username) {
        var user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // return reqRepo.findByToUserAndStatus(user,
        // MessageRequest.RequestStatus.PENDING);
        return reqRepo.findByToUserAndStatus(user, MessageRequest.RequestStatus.PENDING);
    }

    public Conversation respondToRequestByUsername(String username, Long id, ResponseDTO dto) {
        var user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return respondToRequest(user.getId(), id, dto);
    }

    /**
     * Return true if a conversation between the two usernames exists (i.e. the
     * message
     * request was accepted and a private conversation was created).
     */
    public boolean canPrivateChat(String senderUsername, String receiverUsername) {
        var sender = userRepo.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + senderUsername));
        var receiver = userRepo.findByUsername(receiverUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + receiverUsername));

        return convRepo.findBetweenUsers(sender.getId(), receiver.getId()).isPresent();
    }

    public List<Conversation> getConversationsForUsername(String username) {
        var user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return getConversationsFor(user.getId());
    }

    public List<Message> getMessagesForUsername(Long id, String username) {
        var user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return getMessages(id, user.getId());
    }

    public Message sendMessageByUsername(String username, Long id, SendMessageDTO dto) {
        var user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return sendMessage(user.getId(), id, dto);
    }

    public List<MessageRequest> getSentRequestsForUsername(String username) {
        var user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return reqRepo.findByFromUserAndStatus(user, MessageRequest.RequestStatus.PENDING);
    }
}
