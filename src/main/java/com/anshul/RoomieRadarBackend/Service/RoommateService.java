package com.anshul.RoomieRadarBackend.Service;

import com.anshul.RoomieRadarBackend.Mapper.RoomMapper;
import com.anshul.RoomieRadarBackend.Mapper.RoomateProfileMapper;
import com.anshul.RoomieRadarBackend.dto.RoomDto;
import com.anshul.RoomieRadarBackend.dto.RoomateProfileDTO;
import com.anshul.RoomieRadarBackend.entity.RoomateProfile;
import com.anshul.RoomieRadarBackend.entity.User;
import com.anshul.RoomieRadarBackend.repository.RoommateProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoommateService {

    @Autowired
    RoommateProfileRepository roomateProfileRepository;

    public RoomateProfileDTO createRoommate(RoomateProfile roomateProfile, User user) {
        try {
            RoomateProfile newProfile = new RoomateProfile();
            newProfile.setName(roomateProfile.getName());
            newProfile.setAge(roomateProfile.getAge());
            newProfile.setOccupation(roomateProfile.getOccupation());
            newProfile.setLifestyle(roomateProfile.getLifestyle());
            newProfile.setBudget(roomateProfile.getBudget());
            newProfile.setLocation(roomateProfile.getLocation());
            newProfile.setBio(roomateProfile.getBio());
            newProfile.setInterests(roomateProfile.getInterests());
            newProfile.setAvatar(roomateProfile.getAvatar());
            newProfile.setUser(user);
            roomateProfileRepository.save(newProfile);
            return RoomateProfileMapper.toDto(newProfile);
        } catch (Exception e) {
            throw new RuntimeException("Error creating roomate Profile", e);
        }
    }

    public List<RoomateProfileDTO> getAllRoommates() {
        List<RoomateProfile> profiles = roomateProfileRepository.findAll();
        List<RoomateProfileDTO> roommate = profiles
                .stream()
                .map(RoomateProfileMapper::toDto) // map each Room to RoomDto
                .toList();
        return roommate;
    }

    public RoomateProfileDTO updateRoommate(Long id, RoomateProfile updatedProfile, User user) {
        RoomateProfile existingProfile = roomateProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (!existingProfile.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to update this profile");
        }

        existingProfile.setName(updatedProfile.getName());
        existingProfile.setAge(updatedProfile.getAge());
        existingProfile.setOccupation(updatedProfile.getOccupation());
        existingProfile.setLifestyle(updatedProfile.getLifestyle());
        existingProfile.setBudget(updatedProfile.getBudget());
        existingProfile.setLocation(updatedProfile.getLocation());
        existingProfile.setBio(updatedProfile.getBio());
        existingProfile.setInterests(updatedProfile.getInterests());
        if (updatedProfile.getAvatar() != null) {
            existingProfile.setAvatar(updatedProfile.getAvatar());
        }

        roomateProfileRepository.save(existingProfile);
        return RoomateProfileMapper.toDto(existingProfile);
    }
}
