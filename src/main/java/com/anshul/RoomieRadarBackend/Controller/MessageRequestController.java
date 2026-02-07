package com.anshul.RoomieRadarBackend.Controller;

import com.anshul.RoomieRadarBackend.Service.ChatService;
import com.anshul.RoomieRadarBackend.dto.CreateRequestDTO;
import com.anshul.RoomieRadarBackend.dto.ResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

// MessageRequestController.java
@RestController
@RequestMapping("/api/message-requests")
@RequiredArgsConstructor
public class MessageRequestController {
    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateRequestDTO dto, Authentication authentication) {
        String email = authentication.getName(); // current logged-in user
        var req = chatService.createRequestByEmail(email, dto);
        if (req == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Request already exists or user not found"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", req.getId()));
    }

    @GetMapping("/inbox")
    public ResponseEntity<?> inbox(Authentication authentication) {
        String email = authentication.getName(); // currently logged-in user
        return ResponseEntity.ok(chatService.getPendingRequestsForEmail(email));
    }

    @PostMapping("/{id}/respond")
    public ResponseEntity<?> respond(@PathVariable Long id,
            @RequestBody ResponseDTO dto, Authentication authentication) {
        String email = authentication.getName();

        // Process the response to the message request
        var conversation = chatService.respondToRequestByEmail(email, id, dto);

        // Return appropriate response based on user's choice
        if (dto.isAccept()) {
            return ResponseEntity.ok(Map.of("conversationId", conversation.getId()));
        } else {
            return ResponseEntity.ok(Map.of("status", "rejected"));
        }
    }

    @GetMapping("/sent")
    public ResponseEntity<?> getsentrequests(Authentication authentication) {
        String email = authentication.getName(); // currently logged-in user
        return ResponseEntity.ok(chatService.getSentRequestsForEmail(email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRequest(@PathVariable Long id) {
        chatService.deleteRequest(id);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }
}
