package com.anshul.RoomieRadarBackend.Controller;

import com.anshul.RoomieRadarBackend.Service.UserDetailsServiceImpl;
import com.anshul.RoomieRadarBackend.Service.UserService;
import com.anshul.RoomieRadarBackend.dto.LoginRequest;
import com.anshul.RoomieRadarBackend.dto.RegisterRequest;
import com.anshul.RoomieRadarBackend.entity.User;
import com.anshul.RoomieRadarBackend.repository.UserRepository;
import com.anshul.RoomieRadarBackend.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailServiceImpl;

    @Autowired
    private JwtUtils jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest user) {
        try {
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            user.getPassword()
                    )
            );
            String jwt = jwtUtil.generateToken(user.getUsername());

            // Get user details
            User userr = userService.findByUsername(user.getUsername());

            // Build response map
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("user", userr);

            // Return response
            return ResponseEntity.ok(response);

        }
        catch (Exception e) {
            log.error("Error during authentication", e);
            return new ResponseEntity<>("Invalid credentials", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        try {
            // ✅ Check if email already exists
            if (userRepository.existsByEmail(request.getEmail())) {
                return new ResponseEntity<>("Email already exists", HttpStatus.BAD_REQUEST);
            }

            // ✅ Check if username already exists
            if (userRepository.existsByUsername(request.getUsername())) {
                return new ResponseEntity<>("Username already exists", HttpStatus.BAD_REQUEST);
            }

            // ✅ Create new user
            User user = new User();
            user.setUsername(request.getUsername());
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole("student");
            user.setPhone(request.getPhone());
            User savedUser = userRepository.save(user);

            // ✅ Generate JWT
            String token = jwtUtil.generateToken(savedUser.getUsername());

            // ✅ Response with token + user
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", savedUser);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error during registration", e);
            return new ResponseEntity<>("Registration failed: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

//    @PostMapping("/logout")
//    public ResponseEntity<Void> logout() {
//
//    }
//
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            user.setPassword(null); // Hide password
            return ResponseEntity.ok(user);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile( @RequestBody User updatedUser) {
        String username = userRepository.findByUsername(updatedUser.getUsername()).orElse(null).getUsername();
        User existingUser = userRepository.findByUsername(username).orElse(null);
        if (existingUser != null) {
            existingUser.setName(updatedUser.getName());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setPhone(updatedUser.getPhone());
            userRepository.save(existingUser);
            existingUser.setPassword(null); // Hide password
            return ResponseEntity.ok(existingUser);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }
}
