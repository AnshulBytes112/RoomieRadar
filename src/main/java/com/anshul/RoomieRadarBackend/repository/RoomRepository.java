package com.anshul.RoomieRadarBackend.repository;

import com.anshul.RoomieRadarBackend.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {

}
