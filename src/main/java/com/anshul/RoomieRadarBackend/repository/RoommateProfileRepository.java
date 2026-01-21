package com.anshul.RoomieRadarBackend.repository;

import com.anshul.RoomieRadarBackend.entity.RoomateProfile;
import com.anshul.RoomieRadarBackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoommateProfileRepository extends JpaRepository<RoomateProfile, Long> {

    Optional<RoomateProfile> findByUser(User user);
}
