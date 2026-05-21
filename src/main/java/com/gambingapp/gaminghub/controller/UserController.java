//a controller for handling user-related requests such as getting user information, transaction history, and other user-specific data
//Patch is method to provide partial updates to a JSON, minor updates
package com.gambingapp.gaminghub.controller;

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
        int totalLost = transactions.stream().filter(t -> t.getType().equals("LOSE")).mapToInt(CoinTransaction::getAmount).sum();
        int totalBet = transactions.stream()
            .filter(t -> t.getType().equals("BET"))
            .mapToInt(CoinTransaction::getAmount)
            .map(Math::abs)
            .sum();
        int totalRefilled = transactions.stream().filter(t -> t.getType().equals("REFILL")).mapToInt(CoinTransaction::getAmount).sum();

        Map<String, Object> response = new HashMap<>();
        response.put("transactions", transactions);
        response.put("totalWon", totalWon);
        response.put("totalLost", totalLost);
        response.put("totalBet", totalBet);
        response.put("totalRefilled", totalRefilled);
        response.put("netResult", totalWon - totalBet);

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
}
