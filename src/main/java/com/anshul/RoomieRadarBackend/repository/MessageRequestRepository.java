package com.anshul.RoomieRadarBackend.repository;

import com.anshul.RoomieRadarBackend.Model.MessageRequest;
import com.anshul.RoomieRadarBackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRequestRepository extends JpaRepository<MessageRequest, Long> {
    List<MessageRequest> findByToUserAndStatus(User toUser, MessageRequest.RequestStatus status);
    Optional<MessageRequest> findByFromUserAndToUserAndStatus(User from, User to, MessageRequest.RequestStatus status);
}
