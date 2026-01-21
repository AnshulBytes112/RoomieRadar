package com.anshul.RoomieRadarBackend.Model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatMessage {
    private Long Id;
    private String sender;
    private String receiver;
    private String timestamp;
    private String content;
}
