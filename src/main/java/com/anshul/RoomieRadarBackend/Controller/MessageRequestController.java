package com.anshul.RoomieRadarBackend.Controller;

import com.anshul.RoomieRadarBackend.Service.ChatService;
import com.anshul.RoomieRadarBackend.dto.CreateRequestDTO;
import com.anshul.RoomieRadarBackend.dto.ResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

// MessageRequestController.java
@RestController
@RequestMapping("/api/message-requests")
@RequiredArgsConstructor
public class MessageRequestController {
    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateRequestDTO dto, Authentication authentication) {
        String username = authentication.getName(); // current logged-in user
        var req = chatService.createRequestByUsername(username, dto);
        if (req == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Request already exists or user not found"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", req.getId()));
    }

    @GetMapping("/inbox")
    public ResponseEntity<?> inbox(Authentication authentication) {
        String username = authentication.getName(); // currently logged-in user

        var list = chatService.getPendingRequestsForUsername(username);

        // map to DTOs or minimal view
        var view = list.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("fromUserId", r.getFromUser() != null ? r.getFromUser().getId() : null);
            map.put("fromUsername", r.getFromUser() != null ? r.getFromUser().getUsername() : null);
            map.put("message", r.getMessage());
            map.put("createdAt", r.getCreatedAt());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(view);
    }

    @PostMapping("/{id}/respond")
    public ResponseEntity<?> respond(@PathVariable Long id,
            @RequestBody ResponseDTO dto) {
        // Get current logged-in user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Process the response to the message request
        var conversation = chatService.respondToRequestByUsername(username, id, dto);

        // Return appropriate response based on user's choice
        if (dto.isAccept()) {
            return ResponseEntity.ok(Map.of("conversationId", conversation.getId()));
        } else {
            return ResponseEntity.ok(Map.of("status", "rejected"));
        }
    }

    @GetMapping("/sent")
    public ResponseEntity<?> getsentrequests(Authentication authentication) {
        String username = authentication.getName(); // currently logged-in user

        var list = chatService.getSentRequestsForUsername(username);

        // map to DTOs or minimal view
        var view = list.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("toUserId", r.getToUser() != null ? r.getToUser().getId() : null);
            map.put("toUsername", r.getToUser() != null ? r.getToUser().getUsername() : null);
            map.put("message", r.getMessage());
            map.put("status", r.getStatus());
            map.put("createdAt", r.getCreatedAt());
            return map;
        }).toList();
        return ResponseEntity.ok(view);
    }
}
