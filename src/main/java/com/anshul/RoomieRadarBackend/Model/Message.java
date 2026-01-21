package com.anshul.RoomieRadarBackend.Model;

import com.anshul.RoomieRadarBackend.entity.User;
import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "messages", indexes = {@Index(columnList = "conversation_id"), @Index(columnList = "sender_id")})
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne(optional = false) @JoinColumn(name = "sender_id")
    private User sender;

    @Column(columnDefinition = "text")
    private String content;

    private Instant createdAt = Instant.now();

    public Message(Conversation conversation, Long id, User sender, Instant createdAt, String content) {
        this.conversation = conversation;
        this.id = id;
        this.sender = sender;
        this.createdAt = createdAt;
        this.content = content;
    }

    // getters/setters
}
