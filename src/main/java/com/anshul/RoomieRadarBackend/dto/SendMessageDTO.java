package com.anshul.RoomieRadarBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageDTO {
    private Long toUserId;
    private String message;

//    public SendMessageDTO() {
//    }

//    public SendMessageDTO(Long toUserId, String message) {
//        this.toUserId = toUserId;
//        this.message = message;
//    }
//
//    public Long getToUserId() {
//        return toUserId;
//    }
//
//    public void setToUserId(Long toUserId) {
//        this.toUserId = toUserId;
//    }
//
//    public String getMessage() {
//        return message;
//    }
//
//    public void setMessage(String message) {
//        this.message = message;
//    }
}
