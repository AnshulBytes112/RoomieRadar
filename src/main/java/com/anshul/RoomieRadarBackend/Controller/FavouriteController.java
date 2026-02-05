package com.anshul.RoomieRadarBackend.Controller;

import com.anshul.RoomieRadarBackend.Service.FavouriteService;
import com.anshul.RoomieRadarBackend.Service.UserService;
import com.anshul.RoomieRadarBackend.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavouriteController {
    private final FavouriteService favouriteService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<?> getFavorites(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(favouriteService.getFavouritesByUser(user));
    }

    @PostMapping
    public ResponseEntity<?> addFavorite(@RequestBody Map<String, Long> payload, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        Long roomId = payload.get("roomId");
        return ResponseEntity.ok(favouriteService.addFavourite(user, roomId));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<?> removeFavorite(@PathVariable Long roomId, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        favouriteService.removeFavourite(user, roomId);
        return ResponseEntity.ok(Map.of("message", "Removed from favorites"));
    }

    @GetMapping("/{roomId}/check")
    public ResponseEntity<?> checkIfFavorited(@PathVariable Long roomId, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        boolean isFavorited = favouriteService.isFavorited(user, roomId);
        return ResponseEntity.ok(Map.of("isFavorited", isFavorited));
    }
}
