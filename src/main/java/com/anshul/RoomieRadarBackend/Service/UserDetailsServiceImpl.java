package com.anshul.RoomieRadarBackend.Service;

import com.anshul.RoomieRadarBackend.entity.User;
import com.anshul.RoomieRadarBackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {

        Optional<User> user = userRepository.findByEmail(identifier);
        if (user.isEmpty()) {
            user = userRepository.findByPhone(identifier);
        }

        if (user.isPresent()) {
            User userr = user.get();
            return org.springframework.security.core.userdetails.User
                    .builder()
                    .username(userr.getEmail()) // Use email as the principal username
                    .password(userr.getPassword())
                    .roles(userr.getRole().toUpperCase())
                    .build();
        }
        throw new UsernameNotFoundException("User not found with identifier: " + identifier);
    }
}
