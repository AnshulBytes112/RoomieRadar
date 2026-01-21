package com.anshul.RoomieRadarBackend.Model;

import com.anshul.RoomieRadarBackend.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import  jakarta.persistence.Id;

import java.time.Instant;

@Entity
@Table(name = "message_requests",
        indexes = {@Index(columnList = "to_user_id"), @Index(columnList = "from_user_id")})
@Data
@NoArgsConstructor
public class MessageRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "from_user_id")
    private User fromUser;

    @ManyToOne(optional = false) @JoinColumn(name = "to_user_id")
    private User toUser;

    @Column(columnDefinition = "text")
    private String message; // optional initial message

    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PENDING;

    private Instant createdAt = Instant.now();
    private Instant respondedAt;
    public enum RequestStatus {
        PENDING,
        ACCEPTED,
        REJECTED
    }

}
