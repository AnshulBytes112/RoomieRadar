package com.anshul.RoomieRadarBackend.config;

import com.anshul.RoomieRadarBackend.repository.UserRepository;
import jakarta.servlet.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PresenceFilter implements Filter {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();
            userRepository.findByEmail(email).ifPresent(user -> {
                // Only update if more than 10 seconds have passed to avoid excessive DB writes
                if (user.getLastActive() == null || user.getLastActive().isBefore(Instant.now().minusSeconds(10))) {
                    user.setLastActive(Instant.now());
                    userRepository.save(user);
                }
            });
        }
        chain.doFilter(request, response);
    }
}
