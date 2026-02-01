package com.anshul.RoomieRadarBackend.Service;

import com.anshul.RoomieRadarBackend.Mapper.RoomMapper;
import com.anshul.RoomieRadarBackend.Mapper.RoomateProfileMapper;
import com.anshul.RoomieRadarBackend.dto.RoomDto;
import com.anshul.RoomieRadarBackend.dto.RoomateProfileDTO;
import com.anshul.RoomieRadarBackend.entity.RoomateProfile;
import com.anshul.RoomieRadarBackend.entity.User;
import com.anshul.RoomieRadarBackend.repository.RoommateProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            newProfile.setHousingStatus(roomateProfile.getHousingStatus());
            newProfile.setUser(user);
            roomateProfileRepository.save(newProfile);
            return RoomateProfileMapper.toDto(newProfile);
        } catch (Exception e) {
            throw new RuntimeException("Error creating roomate Profile", e);
        }
    }

    public Page<RoomateProfileDTO> getAllRoommates(Pageable pageable) {
        Page<RoomateProfile> profiles = roomateProfileRepository.findAll(pageable);
        return profiles.map(RoomateProfileMapper::toDto);
    }

    public Page<RoomateProfileDTO> searchRoommates(String ageRange, String lifestyle, String budget, String location,
            String occupation, String gender, Pageable pageable) {
        Integer minAge = null;
        Integer maxAge = null;

        if (ageRange != null && !ageRange.equalsIgnoreCase("any")) {
            if (ageRange.contains("+")) {
                try {
                    minAge = Integer.parseInt(ageRange.replace("+", "").trim());
                } catch (NumberFormatException e) {
                    // ignore
                }
            } else if (ageRange.contains("-")) {
                String[] parts = ageRange.split("-");
                try {
                    minAge = Integer.parseInt(parts[0].trim());
                    if (parts.length > 1) {
                        maxAge = Integer.parseInt(parts[1].trim());
                    }
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }

        // Handle "any" values from frontend as null for backend query
        if ("any".equalsIgnoreCase(location))
            location = null;
        if ("any".equalsIgnoreCase(budget))
            budget = null;
        if ("any".equalsIgnoreCase(occupation))
            occupation = null;

        // format wildcards for LIKE queries
        if (location != null)
            location = "%" + location + "%";
        if (budget != null)
            budget = "%" + budget + "%";
        if (occupation != null)
            occupation = "%" + occupation + "%";

        // note: lifestyle filtering is not yet implemented in repo due to complexity
        // with ElementCollection in basic JPQL
        // for now we rely on other filters or post-filtering if strictly required, but
        // location/budget/age are key.

        return roomateProfileRepository.searchRoommates(location, occupation, budget, minAge, maxAge, gender, pageable)
                .map(RoomateProfileMapper::toDto);
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
        existingProfile.setHousingStatus(updatedProfile.getHousingStatus());
        if (updatedProfile.getAvatar() != null) {
            existingProfile.setAvatar(updatedProfile.getAvatar());
        }

        roomateProfileRepository.save(existingProfile);
        return RoomateProfileMapper.toDto(existingProfile);
    }
}
