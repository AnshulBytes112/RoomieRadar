package com.anshul.RoomieRadarBackend.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {
    
    @GetMapping("/test-websocket")
    public String testWebSocket() {
        return "test-websocket";
    }
}
