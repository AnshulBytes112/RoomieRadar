package com.anshul.RoomieRadarBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDTO {
    public boolean accept;
    public boolean isAccept() { return accept; }
    public void setAccept(boolean accept) { this.accept = accept; }
}
