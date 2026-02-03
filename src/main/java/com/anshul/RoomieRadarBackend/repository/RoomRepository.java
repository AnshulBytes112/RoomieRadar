package com.anshul.RoomieRadarBackend.repository;

import com.anshul.RoomieRadarBackend.entity.Room;
import com.anshul.RoomieRadarBackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {

    @Query("SELECT r FROM Room r WHERE LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(r.location) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Room> searchRooms(@Param("keyword") String keyword, Pageable pageable);

    List<Room> findByPostedBy(User user);
}
