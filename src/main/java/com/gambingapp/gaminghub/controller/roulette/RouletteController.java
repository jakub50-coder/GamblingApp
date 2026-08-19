package com.gambingapp.gaminghub.controller.roulette;

import com.gambingapp.gaminghub.service.roulette.RouletteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roulette")
@CrossOrigin(origins = "*")
public class RouletteController {
    private final RouletteService rouletteService;
    
    public RouletteController(RouletteService rouletteService){
        this.rouletteService = rouletteService;
    }

    //POST /api/roulette/spin
    @PostMapping("/spin")
    public ResponseEntity<?> spin(@RequestBody Map<String, Object> request, Authentication authentication){
        String username = authentication.getName();
        @SuppressWarnings("unchecked") //suppress unchecked cast warning
        List<Map<String, Object>> bets = (List<Map<String, Object>>)request.get("bets");
        boolean americanWheel = Boolean.TRUE.equals(request.get("americanWheel"));
        boolean frenchWheel = Boolean.TRUE.equals(request.get("frenchWheel"));
        if(bets == null  || bets.isEmpty()){
            return ResponseEntity.badRequest().body(Map.of("error", "No bets provided"));
        }
        Map<String, Object> result = rouletteService.spin(username, bets, americanWheel, frenchWheel);
        if(result.containsKey("error")){
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}