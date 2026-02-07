package com.anshul.RoomieRadarBackend.Controller;

import com.anshul.RoomieRadarBackend.Service.EmailService;
import com.anshul.RoomieRadarBackend.Service.OtpService;
import com.anshul.RoomieRadarBackend.dto.*;
import com.anshul.RoomieRadarBackend.Mapper.UserMapper;
import com.anshul.RoomieRadarBackend.entity.RoomateProfile;
import com.anshul.RoomieRadarBackend.entity.User;
import com.anshul.RoomieRadarBackend.repository.RoommateProfileRepository;
import com.anshul.RoomieRadarBackend.repository.UserRepository;
import com.anshul.RoomieRadarBackend.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoommateProfileRepository roommateProfileRepository;

    @Autowired
    private JwtUtils jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @Transactional
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        log.info("Received login request for identifier: '{}'", loginRequest.getIdentifier());
        try {
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getIdentifier(),
                            loginRequest.getPassword()));

            // The Principal username is the email (set in UserDetailsServiceImpl)
            String email = authenticate.getName();
            String jwt = jwtUtil.generateToken(email);

            // Get user details
            User user = userRepository.findByEmail(email).orElseThrow();

            if (!user.isEmailVerified()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Please verify your email first"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("user", UserMapper.toDto(user));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Authentication failed: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // if (userRepository.existsByEmail(request.getEmail())) {
            // return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message",
            // "Email already exists"));
            // }

            if (request.getPhone() != null && !request.getPhone().isEmpty()
                    && userRepository.existsByPhone(request.getPhone())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Phone number already exists"));
            }

            User user = userRepository.findByEmail(request.getEmail()).orElse(null);
            if (user != null && !user.isEmailVerified()) {
                user.setName(request.getName());

                String phone = request.getPhone();
                if (phone != null && phone.trim().isEmpty()) {
                    phone = null;
                }
                user.setPhone(phone);

                user.setPassword(passwordEncoder.encode(request.getPassword()));
                user.setRole("student");
                user.setEmailVerified(false);
                userRepository.save(user);
            } else {

                // ✅ Create new user (unverified)
                user = new User();
                user.setName(request.getName());
                user.setEmail(request.getEmail());

                String phone = request.getPhone();
                if (phone != null && phone.trim().isEmpty()) {
                    phone = null;
                }
                user.setPhone(phone);

                user.setPassword(passwordEncoder.encode(request.getPassword()));
                user.setRole("student");
                user.setGender(request.getGender());
                user.setEmailVerified(false);
                user.setAge(request.getAge());
                userRepository.save(user);

                // Create default RoommateProfile
                RoomateProfile profile = new RoomateProfile();
                profile.setUser(user);
                profile.setName(user.getName());
                profile.setAge(request.getAge());
                profile.setGender(request.getGender());
                profile.setHousingStatus("Looking");
                try {
                    roommateProfileRepository.save(profile);
                } catch (Exception e) {
                    log.error("Failed to create default profile: " + e.getMessage());
                }
            }

            // ✅ Generate and Send OTP
            String otp = otpService.generateOtp(user.getEmail());
            emailService.sendOtpEmail(user.getEmail(), otp);

            return ResponseEntity
                    .ok(Map.of("message", "OTP sent to your email. Please verify to complete registration."));

        } catch (Exception e) {
            log.error("Error during registration", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Registration failed: " + e.getMessage()));
        }
    }

    @Transactional
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");

        if (email == null || otp == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Email and OTP are required"));
        }

        if (otpService.verifyOtp(email, otp)) {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                user.setEmailVerified(true);
                userRepository.save(user);

                // Generate initial token for convenience
                String token = jwtUtil.generateToken(user.getEmail());
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Email verified successfully");
                response.put("token", token);
                response.put("user", UserMapper.toDto(user));
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid or expired OTP"));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestParam String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            String otp = otpService.generateOtp(email);
            emailService.sendOtpEmail(email, otp);
            return ResponseEntity.ok(Map.of("message", "OTP resent successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
    }

    @GetMapping("/me")
    @Transactional(readOnly = true)
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            return ResponseEntity.ok(UserMapper.toDto(user));
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            // Safety: Don't reveal if user exists or not, but for this specific UX we might
            // want to let them know
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }

        String otp = otpService.generateOtp(email);
        emailService.sendPasswordResetOtpEmail(email, otp);

        return ResponseEntity.ok(Map.of("success", true, "message", "Reset code sent to your email"));
    }

    @Transactional
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (request.getEmail() == null || request.getOtp() == null || request.getNewPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing required fields"));
        }

        if (otpService.verifyOtp(request.getEmail(), request.getOtp())) {
            User user = userRepository.findByEmail(request.getEmail()).orElse(null);
            if (user != null) {
                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                userRepository.save(user);
                return ResponseEntity.ok(Map.of("success", true, "message", "Password reset successfully"));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid or expired reset code"));
    }

    @PutMapping("/profile")
    @Transactional
    public ResponseEntity<UserDTO> updateProfile(@RequestBody User updatedUser, Authentication authentication) {
        String currentEmail = authentication.getName();
        User existingUser = userRepository.findByEmail(currentEmail).orElse(null);
        if (existingUser != null) {
            existingUser.setName(updatedUser.getName());

            String phone = updatedUser.getPhone();
            if (phone != null && phone.trim().isEmpty()) {
                phone = null;
            }
            existingUser.setPhone(phone);
            // Email cannot be changed here
            userRepository.save(existingUser);
            return ResponseEntity.ok(UserMapper.toDto(existingUser));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
