package com.anshul.RoomieRadarBackend.Controller;

import com.anshul.RoomieRadarBackend.entity.User;
import com.anshul.RoomieRadarBackend.Service.ChatService;
import com.anshul.RoomieRadarBackend.dto.SendMessageDTO;
import com.sun.security.auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @GetMapping
    public ResponseEntity<?> list() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // currently logged-in user

        var convos = chatService.getConversationsForUsername(username);
        var dto = convos.stream()
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", c.getId());

                    // create a mutable list of participant IDs
                    var participantIds = c.getParticipants()
                            .stream()
                            .map(User::getId)
                            .collect(Collectors.toList());

                    map.put("participantIds", participantIds);
                    map.put("lastMessageAt", c.getLastMessageAt());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(dto);
    }


    @GetMapping("/{id}/messages")
    public ResponseEntity<?> messages(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal p) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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
    public ResponseEntity<?> sendMessage(@PathVariable Long id, @RequestBody SendMessageDTO dto, @AuthenticationPrincipal UserPrincipal p) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        var m = chatService.sendMessageByUsername(username, id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", m.getId()));
    }
}

