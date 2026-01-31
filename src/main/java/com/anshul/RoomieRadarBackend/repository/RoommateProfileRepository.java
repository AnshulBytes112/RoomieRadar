package com.anshul.RoomieRadarBackend.repository;

import com.anshul.RoomieRadarBackend.entity.RoomateProfile;
import com.anshul.RoomieRadarBackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface RoommateProfileRepository extends JpaRepository<RoomateProfile, Long> {

        Optional<RoomateProfile> findByUser(User user);

        @Query("SELECT r FROM RoomateProfile r WHERE " +
                        "(:location IS NULL OR LOWER(r.location) LIKE :location) AND " +
                        "(:occupation IS NULL OR LOWER(r.occupation) LIKE :occupation) AND " +
                        "(:budget IS NULL OR r.budget LIKE :budget) AND " +
                        "(:minAge IS NULL OR r.age >= :minAge) AND " +
                        "(:maxAge IS NULL OR r.age <= :maxAge) AND " +
                        "(:gender IS NULL OR LOWER(r.gender) = :gender)")
        Page<RoomateProfile> searchRoommates(
                        @Param("location") String location,
                        @Param("occupation") String occupation,
                        @Param("budget") String budget,
                        @Param("minAge") Integer minAge,
                        @Param("maxAge") Integer maxAge,
                        @Param("gender") String gender,
                        Pageable pageable);
}
