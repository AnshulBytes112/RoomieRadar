package com.anshul.RoomieRadarBackend.repository;

import com.anshul.RoomieRadarBackend.Model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("SELECT c FROM Conversation c JOIN c.participants p1 JOIN c.participants p2 " +
            "WHERE p1.id = :user1 AND p2.id = :user2")
    Optional<Conversation> findBetweenUsers(@Param("user1") Long user1, @Param("user2") Long user2);

    @Query("SELECT DISTINCT c FROM Conversation c JOIN c.participants p WHERE p.id = :userId")
    List<Conversation> findByParticipant(@Param("userId") Long userId);
}
