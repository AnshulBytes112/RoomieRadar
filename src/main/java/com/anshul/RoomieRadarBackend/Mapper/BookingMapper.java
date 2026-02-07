package com.anshul.RoomieRadarBackend.Mapper;

import com.anshul.RoomieRadarBackend.dto.BookingDTO;
import com.anshul.RoomieRadarBackend.dto.RoomSummaryDTO;
import com.anshul.RoomieRadarBackend.dto.UserSummaryDTO;
import com.anshul.RoomieRadarBackend.entity.Booking;

public class BookingMapper {
    public static BookingDTO toDto(Booking booking) {
        if (booking == null)
            return null;

        return BookingDTO.builder()
                .id(booking.getId())
                .user(booking.getUser() != null ? new UserSummaryDTO(
                        booking.getUser().getId(),
                        booking.getUser().getName(),
                        booking.getUser().getEmail()) : null)
                .room(booking.getRoom() != null ? RoomSummaryDTO.builder()
                        .id(booking.getRoom().getId())
                        .title(booking.getRoom().getTitle())
                        .location(booking.getRoom().getLocation())
                        .price(booking.getRoom().getPrice())
                        .images(booking.getRoom().getImages() != null
                                ? new java.util.ArrayList<>(booking.getRoom().getImages())
                                : null)
                        .build() : null)
                .checkInDate(booking.getCheckInDate())
                .phone(booking.getPhone())
                .message(booking.getMessage())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
