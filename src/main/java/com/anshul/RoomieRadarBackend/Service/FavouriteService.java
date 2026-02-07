package com.anshul.RoomieRadarBackend.Service;

import com.anshul.RoomieRadarBackend.Mapper.FavouriteMapper;
import com.anshul.RoomieRadarBackend.dto.FavouriteDTO;
import com.anshul.RoomieRadarBackend.entity.Favourite;
import com.anshul.RoomieRadarBackend.entity.Room;
import com.anshul.RoomieRadarBackend.entity.User;
import com.anshul.RoomieRadarBackend.repository.FavouriteRepository;
import com.anshul.RoomieRadarBackend.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavouriteService {
    private final FavouriteRepository favouriteRepository;
    private final RoomRepository roomRepository;

    @Transactional(readOnly = true)
    public List<FavouriteDTO> getFavouritesByUser(User user) {
        return favouriteRepository.findByUser(user).stream()
                .map(FavouriteMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public FavouriteDTO addFavourite(User user, @SuppressWarnings("null") Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        Favourite favorite;
        if (favouriteRepository.existsByUserAndRoom(user, room)) {
            favorite = favouriteRepository.findByUserAndRoom(user, room).get();
        } else {
            favorite = new Favourite();
            favorite.setUser(user);
            favorite.setRoom(room);
            favorite = favouriteRepository.save(favorite);
        }
        return FavouriteMapper.toDto(favorite);
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
