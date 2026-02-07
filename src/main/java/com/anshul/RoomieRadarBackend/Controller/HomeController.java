package com.anshul.RoomieRadarBackend.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "status", "RoomieRadar API is operational",
                "version", "1.0.0",
                "message", "Welcome to RoomieRadar Backend");
    }
}
