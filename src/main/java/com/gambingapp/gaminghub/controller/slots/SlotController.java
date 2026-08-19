//This is the controller layer for the slots game
package com.gambingapp.gaminghub.controller.slots;

import com.gambingapp.gaminghub.model.slots.SlotResult;
import com.gambingapp.gaminghub.model.slots.SlotSymbol;
import com.gambingapp.gaminghub.service.slots.SlotService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/slots")
@CrossOrigin(origins = "*")
public class SlotController{
    private final SlotService slotService;
    public SlotController(SlotService slotService){
        this.slotService = slotService;
    }

    //Post /api/slots/spin
    //Spin the reels with the given bet amount, calculate result, and award winnings
    @PostMapping("/spin")
    public ResponseEntity<?> spin(@RequestBody Map<String, Integer> request, Authentication authentication){
        String username = authentication.getName();
        Map<String, Object> errorResponse = new HashMap<>();
        Integer betAmount = request.get("betAmount");

        if(betAmount == null || betAmount < 5){
            errorResponse.put("message", "Minimum bet is 5 coins");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        try{
            SlotResult result = slotService.spin(username, betAmount);
            return ResponseEntity.ok(buildResponse(result));
        }
        catch(Exception e){
            errorResponse.put("message", e.getMessage() != null ? e.getMessage() : "Could not spin.");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    //GET /api/slots/payouts
    //Return payout table for dispplay on the frontend
    @GetMapping("/payouts")
    public ResponseEntity<?> getPayouts(){
        return ResponseEntity.ok(slotService.getPayoutTable());
    }
    //Build a clear JSON response to SlotResult
    private Map<String, Object> buildResponse(SlotResult result){
        Map<String, Object> response = new HashMap<>();
        SlotSymbol[][] reels = result.getReels();
        List<List<String>> rows = new ArrayList<>();
        for(int row = 0; row < 3; row++){
            List<String> rowSymbols = new ArrayList<>();
            for(int reel = 0; reel < 3; reel++){
                rowSymbols.add(reels[reel][row].getEmoji());
            }
            rows.add(rowSymbols);
        }
        response.put("rows", rows);
        response.put("winningLines", result.getWinningLines());
        response.put("totalMultiplier", result.getTotalMultiplier());
        response.put("coinChange", result.getCoinChange());
        response.put("resultMessage", result.getResultMessage());
        response.put("won", result.getTotalMultiplier() > 0);

        return response;
    }
}