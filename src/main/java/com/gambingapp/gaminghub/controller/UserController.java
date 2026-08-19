//a controller for handling user-related requests such as getting user information, transaction history, and other user-specific data
//Patch is method to provide partial updates to a JSON, minor updates
package com.gambingapp.gaminghub.controller;

import com.gambingapp.gaminghub.dto.ChangePasswordRequest;
import com.gambingapp.gaminghub.model.CoinTransaction;
import com.gambingapp.gaminghub.model.User;
import com.gambingapp.gaminghub.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins ="*")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //GET /api/user/me
    //what this does it return current player stats including coins and refill countdown

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMe(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.getUser(username);
        List<CoinTransaction> transactions = userService.getAllTransactionHistory(username);
        long gamesPlayed = transactions.stream().filter(t -> t.getType().equals("BET")
                            && t.getRoundId() != null && !t.getRoundId().endsWith("-split"))
                            .map(CoinTransaction::getRoundId).distinct().count();

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User user = userOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("coins", user.getCoins());
        response.put("hasSeenBlackjackTutorial", user.isHasSeenBlackjackTutorial());
        response.put("createdAt", user.getCreatedAt().toString());
        response.put("gamesPlayed", gamesPlayed);

        if (user.getCoins() < 100) {
            LocalDateTime refillAt = user.getLastRefillAt().plusHours(24);
            Duration timeUntilRefill = Duration.between(LocalDateTime.now(), refillAt);

            long secondsUntilRefill = Math.max(0,timeUntilRefill.toSeconds());
            response.put("refillAt", refillAt.toString());
            response.put("secondsUntilRefill", secondsUntilRefill);
        }
        else{
            response.put("refillAt", null);
            response.put("secondsUntilRefill", null);
        }
        return ResponseEntity.ok(response);
    }

    // GET /api/user/transactions 
    //Get the players transaction history including gaining or losing coins

    @GetMapping("/transactions")
    public ResponseEntity<Map<String, Object>> getTransactions(Authentication authentication) {
       String username = authentication.getName();
       List<CoinTransaction> transactions = userService.getTransactionHistory(username);
       Map<String, Map<String, Object>> roundMap = new LinkedHashMap<>();
       List<Map<String, Object>> standaloneList = new ArrayList<>();
       for(CoinTransaction t: transactions){
        if(t.getType().equals("REFILL")){
            Map<String,Object> entry = new HashMap<>();
            entry.put("outcome", "REFILL");
            entry.put("game", "System");
            entry.put("createdAt", t.getCreatedAt().toString());
            entry.put("betAmount", 0);
            entry.put("winAmount", t.getAmount());
            entry.put("netChange", t.getAmount());
            entry.put("icon", "🔄");
            standaloneList.add(entry);
            continue;
        }
        if(t.getRoundId() != null && t.getRoundId().endsWith("-split")){
            continue;
        }
        if(t.getRoundId() == null){
            continue;
        }
        roundMap.computeIfAbsent(t.getRoundId(), k-> {
            Map<String, Object> m =  new HashMap<>();
            m.put("game", t.getGame() != null ? t.getGame() : "unknown");
            m.put("createdAt", t.getCreatedAt().toString());
            m.put("betAmount", 0);
            m.put("winAmount", 0);
            return m;
        });
        Map<String, Object> round = roundMap.get(t.getRoundId());
        if(t.getType().equals("BET")){
            round.put("betAmount",Math.abs(t.getAmount()));
        }
        else if(t.getType().equals("WIN")){
            round.put("winAmount", t.getAmount());
        }
       }
       List<Map<String, Object>> displayList = new ArrayList<>();
       for(Map.Entry<String, Map<String, Object>> entry: roundMap.entrySet()){
        Map<String, Object>  round = entry.getValue();
        int bet = (int) round.get("betAmount");
        int won = (int) round.get("winAmount");
        int net = won - bet;
        String outcome;
        String icon;
        if(won == 0){
            outcome = "LOST";
            icon = "❌";
        }
        else if(won == bet){
            outcome = "PUSH";
            icon = "⚖️";
        }
        else if(won > bet){
            outcome = "WIN";
            icon = "✅";
        }
        else{
            outcome = "LOST";
            icon = "❌";
        }
        round.put("outcome", outcome);
        round.put("netChange", net);
        round.put("icon", icon);
        displayList.add(round);
       }
       displayList.addAll(standaloneList);
       displayList.sort((a,b) -> {
        String dateA = (String) a.get("createdAt");
        String dateB = (String) b.get("createdAt");
        return dateB.compareTo(dateA);
       });
       int totalWon = displayList.stream().filter(r -> r.get("outcome").equals("WIN"))
                .mapToInt(r -> (int) r.get("netChange")).sum();
        int totalLost = displayList.stream().filter(r -> r.get("outcome").equals("LOST"))
                .mapToInt(r -> (int) r.get("betAmount")).sum();
        int totalRefilled = standaloneList.stream()
                .filter(r -> r.get("outcome").equals("REFILL"))
                .mapToInt(r -> (int) r.get("winAmount")).sum();
        int netCoins = totalWon - totalLost + totalRefilled;

        Map<String, Object> response = new HashMap<>();
        response.put("transactions", displayList);
        response.put("totalWon", totalWon);
        response.put("totalLost", totalLost);
        response.put("totalRefilled", totalRefilled);
        response.put("netCoins", netCoins);
        return ResponseEntity.ok(response);
    }
    //Patch /api/user/tutorial/blackjack
    //Mark the blackjack tutorial as seen

    @PatchMapping("/tutorial/blackjack")
    public ResponseEntity<Map<String, String>> markBlackJackTutorialAsSeen(Authentication authentication) {
        String username = authentication.getName();
        userService.markTutorialSeen(username);

        Map<String, String> response = new HashMap<>();
        response.put("message", "You have seen the blackjack tutorial");
        return ResponseEntity.ok(response);
    }

    //Patch api/user/password
    //Changes password after verifying their current password
    //Returns 400 when wrong
    @PatchMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody ChangePasswordRequest request, Authentication authentication){
        String username = authentication.getName();
        Map<String, String> response = new HashMap<>();
        if(request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()){
            response.put("message", "Current password is required.");
            return ResponseEntity.badRequest().body(response);
        }
        if (request.getNewPassword() == null || request.getNewPassword().length() < 12) {
            response.put("message", "New password must be at least 12 characters.");
            return ResponseEntity.badRequest().body(response);
        }
        boolean success = userService.changePassword(
            username, request.getCurrentPassword(), request.getNewPassword());
        if (success) {
            response.put("message", "Password updated successfully");
            return ResponseEntity.ok(response);
        }
        else{
            response.put("message", "Current password is incorrect");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
