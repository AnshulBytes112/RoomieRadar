package com.anshul.RoomieRadarBackend.repository;

import com.anshul.RoomieRadarBackend.entity.Favourite;
import com.anshul.RoomieRadarBackend.entity.Room;
import com.anshul.RoomieRadarBackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavouriteRepository extends JpaRepository<Favourite, Long> {
    List<Favourite> findByUser(User user);

    Optional<Favourite> findByUserAndRoom(User user, Room room);

    boolean existsByUserAndRoom(User user, Room room);
}
