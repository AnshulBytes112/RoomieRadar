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

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<User> user= userRepository.findByUsername(username);
        if(user.isPresent()){
            User userr=user.get();
            UserDetails userdetails=org.springframework.security.core.userdetails.User
                    .builder()
                    .username(userr.getUsername())
                    .password(userr.getPassword())
                    .roles(userr.getRole().toUpperCase())
                    .build();

            return userdetails;
        }
        throw new UsernameNotFoundException("UserName not found"+username);
    }
}
