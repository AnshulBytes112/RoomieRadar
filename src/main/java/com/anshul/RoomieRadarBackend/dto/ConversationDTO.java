package com.anshul.RoomieRadarBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    private Long id;
    private Instant lastMessageAt;
    private ParticipantDTO otherParticipant;
    private LastMessageDTO lastMessage;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantDTO {
        private Long id;
        private String name;
        private String avatar;
        private Instant lastActive;
        private boolean isActive;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LastMessageDTO {
        private String content;
        private Instant createdAt;
    }
}
