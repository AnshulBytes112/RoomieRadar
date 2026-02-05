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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
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
        Specification<RoomateProfile> spec = (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("user").get("deleted"), false),
                criteriaBuilder.equal(root.get("deleted"), false));
        Page<RoomateProfile> profiles = roomateProfileRepository.findAll(spec, pageable);
        return profiles.map(RoomateProfileMapper::toDto);
    }

    public Page<RoomateProfileDTO> searchRoommates(String ageRange, String lifestyle, String budget, String location,
            String occupation, String gender, Pageable pageable) {

        Specification<RoomateProfile> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("user").get("deleted"), false));
            predicates.add(criteriaBuilder.equal(root.get("deleted"), false));

            // Location Filter
            if (location != null && !location.isBlank() && !"any".equalsIgnoreCase(location)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("location")),
                        "%" + location.toLowerCase() + "%"));
            }

            // Keyword Filter (Name, Occupation, Bio)
            if (occupation != null && !occupation.isBlank() && !"any".equalsIgnoreCase(occupation)) {
                String keyword = "%" + occupation.toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("occupation")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("bio")), keyword)));
            }

            // Budget Filter
            if (budget != null && !budget.isBlank() && !"any".equalsIgnoreCase(budget)) {
                predicates.add(criteriaBuilder.like(root.get("budget"), "%" + budget + "%"));
            }

            // Gender Filter
            if (gender != null && !gender.isBlank() && !"any".equalsIgnoreCase(gender)) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("gender")), gender.toLowerCase()));
            }

            // Age Range Filter
            if (ageRange != null && !ageRange.isBlank() && !"any".equalsIgnoreCase(ageRange)) {
                if (ageRange.endsWith("+")) {
                    try {
                        int min = Integer.parseInt(ageRange.replace("+", "").trim());
                        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("age"), min));
                    } catch (NumberFormatException e) {
                        /* ignore */ }
                } else if (ageRange.contains("-")) {
                    try {
                        String[] parts = ageRange.split("-");
                        int min = Integer.parseInt(parts[0].trim());
                        int max = Integer.parseInt(parts[1].trim());
                        predicates.add(criteriaBuilder.between(root.get("age"), min, max));
                    } catch (NumberFormatException e) {
                        /* ignore */ }
                }
            }

            // Lifestyle Filter (Optional, checking if any of the lifestyle traits match)
            if (lifestyle != null && !lifestyle.isBlank() && !"any".equalsIgnoreCase(lifestyle)) {
                predicates.add(criteriaBuilder.isMember(lifestyle, root.get("lifestyle")));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return roomateProfileRepository.findAll(spec, pageable).map(RoomateProfileMapper::toDto);
    }

    public RoomateProfileDTO getRoommateById(Long id) {
        RoomateProfile profile = roomateProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        return RoomateProfileMapper.toDto(profile);
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
        existingProfile.setGender(updatedProfile.getGender());
        existingProfile.setInstagram(updatedProfile.getInstagram());
        existingProfile.setDeleted(updatedProfile.isDeleted());

        // Sync with User entity
        if (existingProfile.getUser() != null) {
            existingProfile.getUser().setGender(updatedProfile.getGender());
        }

        if (updatedProfile.getAvatar() != null) {
            existingProfile.setAvatar(updatedProfile.getAvatar());
        }

        roomateProfileRepository.save(existingProfile);
        return RoomateProfileMapper.toDto(existingProfile);
    }

    public boolean deleteRoommateProfile(Long id, User user) {
        RoomateProfile profile = roomateProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (!profile.getUser().getId().equals(user.getId())) {
            return false;
        }

        profile.setDeleted(true);
        roomateProfileRepository.save(profile);
        return true;
    }
}
