package com.anshul.RoomieRadarBackend.config;

import com.anshul.RoomieRadarBackend.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {

        if (!(request instanceof ServletServerHttpRequest)) {
            return true;
        }
        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;

        // Try query parameter first (e.g. /ws?token=...) - browsers can set this easily
        String token = null;
        URI uri = servletRequest.getURI();
        if (uri != null && uri.getQuery() != null) {
            String q = uri.getQuery();
            for (String part : q.split("&")) {
                String[] kv = part.split("=", 2);
                if (kv.length == 2 && (kv[0].equals("token") || kv[0].equals("access_token"))) {
                    token = java.net.URLDecoder.decode(kv[1], java.nio.charset.StandardCharsets.UTF_8);
                    break;
                }
            }
        }

        // Fallback to Authorization header
        if (token == null) {
            String authHeader = servletRequest.getServletRequest().getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        if (token != null && jwtUtils != null) {
            String email = null;
            try {
                email = jwtUtils.extractEmail(token);
                System.out.println("JWT Interceptor: Extracted email from token: " + email);
            } catch (Exception e) {
                System.out.println("JWT Interceptor: Failed to extract email: " + e.getMessage());
                // ignore invalid token here; handshake will proceed without a user
            }
            if (email != null) {
                // Set a Spring Security context so the handshake is authenticated
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(email, null,
                        List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);

                final String principalEmail = email; // make effectively final for lambda capture
                attributes.put("email", principalEmail); // store in session attributes
                // Also store Principal in WebSocket session
                attributes.put("principal", auth);
                System.out.println("JWT Interceptor: Stored email '" + principalEmail + "' in session attributes");
            } else {
                System.out.println("JWT Interceptor: Email is null after extraction");
            }
        } else {
            System.out.println("JWT Interceptor: Token is null or jwtUtils is null");
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
    }
}
