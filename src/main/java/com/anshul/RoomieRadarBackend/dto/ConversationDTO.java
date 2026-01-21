package com.anshul.RoomieRadarBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO{
    private Long id;
    private List<Long> participantIds;
    private Instant lastMessageAt;
    // getters/setters and mapping methods


}
