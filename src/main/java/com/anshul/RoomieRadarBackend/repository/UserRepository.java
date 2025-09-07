package com.anshul.RoomieRadarBackend.repository;

import com.anshul.RoomieRadarBackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    void deleteByUsername(String username);

    boolean existsByEmail(String email);   // ✅ Corrected
    boolean existsByUsername(String username); // ✅ Added for username check

    Optional<User> findByEmail(String email);

}
