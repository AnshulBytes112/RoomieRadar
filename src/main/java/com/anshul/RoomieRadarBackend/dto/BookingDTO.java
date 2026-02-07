package com.anshul.RoomieRadarBackend.dto;

import com.anshul.RoomieRadarBackend.entity.Booking.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDTO {
    private Long id;
    private UserSummaryDTO user;
    private RoomSummaryDTO room;
    private LocalDate checkInDate;
    private String phone;
    private String message;
    private BookingStatus status;
    private LocalDateTime createdAt;
}
