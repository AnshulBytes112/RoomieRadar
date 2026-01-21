package com.anshul.RoomieRadarBackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;

import java.time.Instant;

@Entity
@Table(name = "chat_requests")
@Data  // <-- Lombok annotation that generates getters/setters automatically
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id")
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id")
    private User toUser;

    private String message;

    @Enumerated(EnumType.STRING)
    private Status status= Status.PENDING;

    private Instant createdAt = Instant.now();

    public enum Status {
        PENDING, ACCEPTED, REJECTED
    }
}

