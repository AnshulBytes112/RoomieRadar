package com.anshul.RoomieRadarBackend.repository;

import com.anshul.RoomieRadarBackend.Model.Conversation;
import com.anshul.RoomieRadarBackend.Model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationOrderByCreatedAtAsc(Conversation conversation);
}
