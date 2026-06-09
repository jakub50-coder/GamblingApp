package com.gambingapp.gaminghub.controller.blackjack;

import com.gambingapp.gaminghub.dto.blackjack.BetRequest;
import com.gambingapp.gaminghub.dto.blackjack.BlackjackResponse;
import com.gambingapp.gaminghub.model.blackjack.BlackjackGame;
import com.gambingapp.gaminghub.service.blackjack.BlackjackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/blackjack")
@CrossOrigin(origins = "*")
public class BlackjackController {
    private final BlackjackService blackjackService;

    public BlackjackController(BlackjackService blackjackService) {
        this.blackjackService = blackjackService;
    }

    @GetMapping("/state")
    public ResponseEntity<?> getState(Authentication authentication) {
        String username = authentication.getName();
        Optional<BlackjackGame> gameOpt = blackjackService.getGame(username);
        if (gameOpt.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("phase", "WAITING_FOR_BET");
            return ResponseEntity.ok(response);
        }

        BlackjackGame game = gameOpt.get();
        int playerCoins = blackjackService.getPlayerCoins(username);
        return ResponseEntity.ok(BlackjackResponse.from(game, playerCoins));
    }

    @PostMapping("/start")
    public ResponseEntity<?> startRound(@RequestBody BetRequest request, Authentication authentication) {
        String username = authentication.getName();
        Map<String, Object> errorResponse = new HashMap<>();

        if (request.getBetAmount() < 10) {
            errorResponse.put("message", "Minimum bet is 10 coins");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        if (request.getBetAmount() > 75) {
            errorResponse.put("message", "Maximum bet is 75 coins");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        String error = blackjackService.startRound(username, request.getBetAmount());
        if (error != null) {
            errorResponse.put("message", error);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        BlackjackGame game = blackjackService.getOrCreateGame(username);
        int playerCoins = blackjackService.getPlayerCoins(username);
        return ResponseEntity.ok(BlackjackResponse.from(game, playerCoins));
    }

    @PostMapping("/hit")
    public ResponseEntity<?> hit(Authentication authentication) {
        String username = authentication.getName();
        Map<String, Object> errorResponse = new HashMap<>();

        String error = blackjackService.hit(username);
        if (error != null) {
            errorResponse.put("message", error);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Optional<BlackjackGame> gameOpt = blackjackService.getGame(username);
        if (gameOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        int playerCoins = blackjackService.getPlayerCoins(username);
        return ResponseEntity.ok(BlackjackResponse.from(gameOpt.get(), playerCoins));
    }

    @PostMapping("/stand")
    public ResponseEntity<?> stand(Authentication authentication) {
        String username = authentication.getName();
        Map<String, Object> errorResponse = new HashMap<>();

        String error = blackjackService.stand(username);
        if (error != null) {
            errorResponse.put("message", error);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Optional<BlackjackGame> gameOpt = blackjackService.getGame(username);
        if (gameOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        int playerCoins = blackjackService.getPlayerCoins(username);
        return ResponseEntity.ok(BlackjackResponse.from(gameOpt.get(), playerCoins));
    }

    @PostMapping("/new-round")
    public ResponseEntity<?> newRound(Authentication authentication) {
        String username = authentication.getName();
        Map<String, Object> response = new HashMap<>();
        int playerCoins = blackjackService.getPlayerCoins(username);

        if (playerCoins < 10) {
            response.put("message", "Not enough coins to start a new round");
            response.put("coins", playerCoins);
            return ResponseEntity.badRequest().body(response);
        }

        BlackjackGame game = blackjackService.getOrCreateGame(username);
        // Force reset regardless of current phase to ensure clean state
        game.resetForNewRound();
        response.put("phase", "WAITING_FOR_BET");
        response.put("coins", playerCoins);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forfeit")
    public ResponseEntity<?> forfeit(Authentication authentication) {
        String username = authentication.getName();
        Optional<BlackjackGame> gameOpt = blackjackService.getGame(username);
        if (gameOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        BlackjackGame game = gameOpt.get();
        if (game.getPhase() == BlackjackGame.GamePhase.PLAYER_TURN) {
            blackjackService.forfeitRound(game, username);
        }

        int playerCoins = blackjackService.getPlayerCoins(username);
        return ResponseEntity.ok(BlackjackResponse.from(game, playerCoins));
    }
}