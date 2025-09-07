package com.anshul.RoomieRadarBackend.Service;

import com.anshul.RoomieRadarBackend.Mapper.RoomMapper;
import com.anshul.RoomieRadarBackend.dto.RoomDto;
import com.anshul.RoomieRadarBackend.entity.Room;
import com.anshul.RoomieRadarBackend.entity.User;
import com.anshul.RoomieRadarBackend.repository.RoomRepository;
import com.anshul.RoomieRadarBackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service

public class RoomService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoomRepository roomRepository;
    public RoomDto createRoom(Room room, User user) {
        try {
            Room room1 = new Room();
            room1.setTitle(room.getTitle());
            room1.setLocation(room.getLocation());
            room1.setPrice(room.getPrice());
            room1.setType(room.getType());
            room1.setBedrooms(room.getBedrooms());
            room1.setBathrooms(room.getBathrooms());
            room1.setArea(room.getArea());
            room1.setImages(room.getImages());
            room1.setTags(room.getTags());

            // ✅ associate logged-in user
            room1.setPostedBy(user);

            // ✅ save to DB
            Room savedRoom = roomRepository.save(room1);

            // ✅ convert entity → DTO
            return RoomMapper.toDto(savedRoom);

        } catch (Exception e) {
            throw new RuntimeException("Error creating room", e);
        }
    }
    public List<Room> getAllRooms() {
        return roomRepository.findAll();     }

    public void savenewentry(Room newRoom) {
        roomRepository.save(newRoom);
    }

    public boolean deleteRoom(Long id, String username) {
        Optional<Room> roomOpt = roomRepository.findById(id);

        if (roomOpt.isEmpty()) {
            return false;
        }

        Room room = roomOpt.get();
        User user = room.getPostedBy(); // directly get the owner

        if (!username.equals(user.getUsername())) {
            return false; // Not the owner
        }

        // Remove room from user's list if present
        if (user.getRooms() != null) {
            user.getRooms().remove(room);
            userRepository.save(user); // Only needed if cascade is not configured
        }

        // Delete the room
        roomRepository.delete(room);

        return true;
    }
}



