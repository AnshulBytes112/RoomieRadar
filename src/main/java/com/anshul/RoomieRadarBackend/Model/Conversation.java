package com.anshul.RoomieRadarBackend.Model;

import com.anshul.RoomieRadarBackend.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    @JoinTable(name = "conversation_participants",
            joinColumns = @JoinColumn(name = "conversation_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> participants = new ArrayList<>();

    private Instant createdAt = Instant.now();
    private Instant lastMessageAt;

    // convenience
    public boolean hasParticipant(Long userId) {
        return participants.stream().anyMatch(u -> u.getId().equals(userId));
    }

    // getters/setters
}
