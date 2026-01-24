package com.anshul.RoomieRadarBackend.Controller;

import com.anshul.RoomieRadarBackend.entity.User;
import com.anshul.RoomieRadarBackend.Service.ChatService;
import com.anshul.RoomieRadarBackend.dto.SendMessageDTO;
import com.anshul.RoomieRadarBackend.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

// ConversationController.java
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {
    private final ChatService chatService;

    @Autowired
    private MessageRepository messageRepository;

    @GetMapping
    public ResponseEntity<?> list(Authentication authentication) {
        String username = authentication.getName();

        var convos = chatService.getConversationsForUsername(username);
        return ResponseEntity.ok(convos.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("lastMessageAt", c.getLastMessageAt());

            // Participant Details
            User otherUser = c.getParticipants().stream()
                    .filter(u -> !u.getUsername().equals(username))
                    .findFirst()
                    .orElse(null);

            if (otherUser != null) {
                var rp = otherUser.getRoomateProfile();
                map.put("otherParticipant", Map.of(
                        "id", otherUser.getId(),
                        "name", (rp != null && rp.getName() != null) ? rp.getName() : otherUser.getName(),
                        "avatar", (rp != null && rp.getAvatar() != null) ? rp.getAvatar() : ""));
            }

            // Latest Message Snippet
            messageRepository.findFirstByConversationOrderByCreatedAtDesc(c).ifPresent(m -> {
                map.put("lastMessage", Map.of(
                        "content", m.getContent(),
                        "createdAt", m.getCreatedAt()));
            });

            return map;
        }).collect(Collectors.toList()));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<?> messages(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        var msgs = chatService.getMessagesForUsername(id, username);
        var out = msgs.stream()
                .map(m -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", m.getId());
                    map.put("senderId", m.getSender().getId());
                    map.put("content", m.getContent());
                    map.put("createdAt", m.getCreatedAt());
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<?> sendMessage(@PathVariable Long id, @RequestBody SendMessageDTO dto,
            Authentication authentication) {
        String username = authentication.getName();

        var m = chatService.sendMessageByUsername(username, id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", m.getId()));
    }
}
