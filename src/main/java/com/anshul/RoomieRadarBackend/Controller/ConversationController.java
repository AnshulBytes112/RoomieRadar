package com.anshul.RoomieRadarBackend.Controller;

import com.anshul.RoomieRadarBackend.dto.SendMessageDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

// ConversationController.java
@RestController
@RequestMapping("/api/conversations")
public class ConversationController {
    private final com.anshul.RoomieRadarBackend.Service.ChatService chatService;
    private final com.anshul.RoomieRadarBackend.repository.UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Autowired
    public ConversationController(com.anshul.RoomieRadarBackend.Service.ChatService chatService,
            com.anshul.RoomieRadarBackend.repository.UserRepository userRepository) {
        this.chatService = chatService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> list(Authentication authentication) {
        String email = authentication.getName();
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setLastActive(Instant.now());
            userRepository.save(user);
        });

        return ResponseEntity.ok(chatService.getConversationsForEmail(email));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<?> messages(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setLastActive(Instant.now());
            userRepository.save(user);
        });
        return ResponseEntity.ok(chatService.getMessagesForEmail(id, email));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<?> sendMessage(@PathVariable Long id, @RequestBody SendMessageDTO dto,
            Authentication authentication) {
        String email = authentication.getName();
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setLastActive(Instant.now());
            userRepository.save(user);
        });

        return ResponseEntity.status(HttpStatus.CREATED).body(chatService.sendMessageByEmail(email, id, dto));
    }
}
