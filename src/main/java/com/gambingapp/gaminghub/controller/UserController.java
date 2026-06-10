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
import java.util.Optional;

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

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User user = userOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("coins", user.getCoins());
        response.put("hasSeenBlackjackTutorial", user.isHasSeenBlackjackTutorial());
        response.put("createdAt", user.getCreatedAt().toString());

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

        int totalWon = transactions.stream().filter(t -> t.getType().equals("WIN")).mapToInt(CoinTransaction::getAmount).sum();
        int totalBet = transactions.stream().filter(t -> t.getType().equals("BET")).mapToInt(t -> Math.abs(t.getAmount())).sum();
        int totalRefilled = transactions.stream().filter(t -> t.getType().equals("REFILL")).mapToInt(CoinTransaction::getAmount).sum();

        Map<String, Integer> betsByRound = new HashMap<>();
        Map<String, Integer> winsByRound = new HashMap<>();

        transactions.stream().filter(t -> t.getType().equals("BET") && t.getRoundId() != null).forEach(t -> betsByRound.put(t.getRoundId(), Math.abs(t.getAmount())));
        transactions.stream().filter(t -> t.getType().equals("WIN") && t.getRoundId() != null).forEach(t -> winsByRound.put(t.getRoundId(), t.getAmount()));
        int totalLost = betsByRound.entrySet().stream().filter(e -> !winsByRound.containsKey(e.getKey())).mapToInt(Map.Entry::getValue).sum();
        int netResult = totalWon - totalLost;
        List<Map<String, Object>> transactionList = transactions.stream().map(t ->{
            Map<String, Object> entry = new HashMap<>();
            entry.put("id", t.getId());
            entry.put("amount", t.getAmount());
            entry.put("type", t.getType());
            entry.put("game", t.getGame());
            entry.put("roundId", t.getRoundId());
            entry.put("createdAt", t.getCreatedAt().toString());
            return entry;
        })
        .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("transactions", transactions);
        response.put("totalWon", totalWon);
        response.put("totalBet", totalBet);
        response.put("totalLost", totalLost);
        response.put("totalRefilled", totalRefilled);
        response.put("netResult", netResult);
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
