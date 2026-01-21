package com.anshul.RoomieRadarBackend.Controller;

import com.anshul.RoomieRadarBackend.Model.ChatMessage;
import com.anshul.RoomieRadarBackend.Service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;
import java.time.LocalDateTime;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/chat")
public class ChatController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private ChatService chatService;
    @MessageMapping("/private-message")
    public void sendPrivateMessage(Principal principal, ChatMessage message, StompHeaderAccessor accessor) {
        String sender = null;
        
        // Try to get username from session attributes set by JWT interceptor
        Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
        Object username = sessionAttrs != null ? sessionAttrs.get("username") : null;
        if (username instanceof String) {
            sender = (String) username;
        } else if (principal != null) {
            sender = principal.getName();
        }
        
        if (sender == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        System.out.println("WebSocket: Received message from " + sender + " to " + message.getReceiver());
        System.out.println("WebSocket: Principal type: " + (principal != null ? principal.getClass().getSimpleName() : "null"));
        System.out.println("WebSocket: Username from session: " + username);
        
        String receiver = message.getReceiver();
        if (receiver == null || receiver.trim().isEmpty()) {
            throw new IllegalArgumentException("Receiver is required");
        }
        receiver = receiver.trim();

        // enforce that private messaging is allowed only when a conversation exists
        boolean canChat = chatService.canPrivateChat(sender, receiver);
        System.out.println("WebSocket: Can private chat between " + sender + " and " + receiver + "? " + canChat);
        
        if (!canChat) {
            throw new AccessDeniedException("Private chat not allowed: request not accepted");
        }
        
        message.setSender(sender);
        message.setTimestamp(LocalDateTime.now().toString());

        System.out.println("WebSocket: Sending message to user " + receiver + " at /queue/messages");
        messagingTemplate.convertAndSendToUser(
                receiver, "/queue/messages", message
        );
        System.out.println("WebSocket: Message sent successfully");
    }
    public String chat() {
        return "chat";
    }
}
