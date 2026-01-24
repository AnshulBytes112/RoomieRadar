package com.anshul.RoomieRadarBackend.Service;

import com.anshul.RoomieRadarBackend.entity.Favourite;
import com.anshul.RoomieRadarBackend.entity.Room;
import com.anshul.RoomieRadarBackend.entity.User;
import com.anshul.RoomieRadarBackend.repository.FavouriteRepository;
import com.anshul.RoomieRadarBackend.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavouriteService {
    private final FavouriteRepository favouriteRepository;
    private final RoomRepository roomRepository;

    public List<Favourite> getFavouritesByUser(User user) {
        return favouriteRepository.findByUser(user);
    }

    @Transactional
    public Favourite addFavourite(User user, @SuppressWarnings("null") Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (favouriteRepository.existsByUserAndRoom(user, room)) {
            return favouriteRepository.findByUserAndRoom(user, room).get();
        }

        Favourite favourite = new Favourite();
        favourite.setUser(user);
        favourite.setRoom(room);
        return favouriteRepository.save(favourite);
    }

    @Transactional
    public void removeFavourite(User user, @SuppressWarnings("null") Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        favouriteRepository.findByUserAndRoom(user, room).ifPresent(favouriteRepository::delete);
    }

    public boolean isFavorited(User user, @SuppressWarnings("null") Long roomId) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null)
            return false;
        return favouriteRepository.existsByUserAndRoom(user, room);
    }
}
