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

            room1.setDescription(room.getDescription());
            room1.setAmenities(room.getAmenities());
            room1.setAvailaibleFrom(room.getAvailaibleFrom());
            room1.setDeposit(room.getDeposit());           // ← This was missing!
            room1.setMaintenance(room.getMaintenance());   // ← This was missing!
            room1.setParking(room.getParking());
            room1.setPetFriendly(room.getPetFriendly());
            room1.setFurnished(room.getFurnished());
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

    public Room getRoomById(Long id) {
        Optional<Room> roomOpt = roomRepository.findById(id);
        return roomOpt.orElse(null);
    }

    public List<Room> searchRooms(String location, String budget, String roomType, Integer bedrooms, Integer bathrooms) {
        if ((location == null || location.isBlank()) &&
                (budget == null || budget.isBlank()) &&
                (roomType == null || roomType.isBlank()) &&
                bedrooms == null &&
                bathrooms == null) {
            return roomRepository.findAll();
        }

        // Parse budget if provided
        Integer minBudget = null;
        Integer maxBudget = null;
        if (budget != null && !budget.isBlank()) {
            String[] parts = budget.split("\\s*[-–]\\s*");
            try {
                minBudget = Integer.parseInt(parts[0].trim());
                if (parts.length > 1) maxBudget = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                // ignore invalid budget
            }
        }

        // Filter dynamically in memory (or use Specification for DB-level filtering)
        Integer finalMaxBudget = maxBudget;
        Integer finalMinBudget = minBudget;
        return roomRepository.findAll().stream()
                .filter(room -> location == null || location.isBlank() ||
                        room.getLocation().toLowerCase().contains(location.toLowerCase()))
                .filter(room -> roomType == null || roomType.isBlank() ||
                        room.getType().toString().equalsIgnoreCase(roomType))
                .filter(room -> bedrooms == null || room.getBedrooms() == bedrooms)
                .filter(room -> bathrooms == null || room.getBathrooms() == bathrooms)
                .filter(room -> (finalMinBudget == null || room.getPrice() >= finalMinBudget) && (finalMaxBudget == null || room.getPrice() <= finalMaxBudget))
                .toList();
    }
}



