package com.anshul.RoomieRadarBackend.dto;

import lombok.Data;
import org.antlr.v4.runtime.misc.NotNull;
import org.hibernate.annotations.processing.Pattern;

import java.time.LocalDate;

@Data
public class BookingRequest {
    private Long roomId;
    @NotNull
        private LocalDate checkInDate;
    private String message;

    private String phone;
}
