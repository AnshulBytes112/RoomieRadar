package com.anshul.RoomieRadarBackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(name = "bookings")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({ "rooms", "bookings", "favourites", "images", "roomateProfile" })
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @JsonIgnoreProperties({ "postedBy" })
    private Room room;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    private String phone;

    @Column
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum BookingStatus {
        PENDING, CONFIRMED, CANCELLED, SCHEDULE_INSPECTION
    }

    // Explicit setter for checkInDate to avoid naming conflicts
    public void setCheckInDateField(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }
}
