//This class is the controller for baccarat game

package com.gambingapp.gaminghub.controller.baccarat;

import com.gambingapp.gaminghub.service.baccarat.BaccaratService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/baccarat")
@CrossOrigin(origins = "*")
public class BaccaratController{
    private final BaccaratService baccaratService;

    public BaccaratController(BaccaratService baccaratService){
        this.baccaratService = baccaratService;
    }

    //POST /api/baccarat/deal
    //This method deals a full bacarat round and resolves all bets
    @PostMapping("/deal")
    public ResponseEntity<?> deal(@RequestBody Map<String, Object> request, Authentication authentication){
        String username = authentication.getName();
        @SuppressWarnings("unchecked")
        Map<String, Integer> bets = (Map<String, Integer>) request.get("bets");
        if(bets == null || bets.isEmpty()){
            return ResponseEntity.badRequest().body(Map.of("error", "No bets placed."));
        }
        for(Map.Entry<String, Integer> entry : bets.entrySet()){
            int amount = entry.getValue();
            if(amount < 10){
                return ResponseEntity.badRequest().body(Map.of("error", "Minimum bet is 10 coins"));
            }
            if(amount > 75){
                return ResponseEntity.badRequest().body(Map.of("error", "Maximum bet is 75 coins"));
            }
        }
        Map<String, Object> result = baccaratService.deal(username, bets);
        if(result.containsKey("error")){
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}