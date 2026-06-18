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
    //GET /api/blackjack/state
    //Gets the state of the game (whether there is a game or not)
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
    //POST /api/blakcjack/start
    //When the player wants to start a game
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
    //POST /api/blackjack/hit
    //When the player wants to hit in blackjack
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
    //POST /api/blackjack/stand
    //When the player wants to stand or are happy with their card values
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
    //POST /api/blackjack/new-round
    //When the player wants to play a new round
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
    //POST /api/blackjack/surrender
    //When the player wants to surrender and gain hald of their bet
    @PostMapping("/surrender")
    public ResponseEntity<?> surrender(Authentication authentication){
        String username = authentication.getName();
        Map<String,Object> errorResponse = new HashMap<>();
        String error = blackjackService.surrender(username);
        if(error != null){
            errorResponse.put("message", error);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        Optional<BlackjackGame> gameOpt = blackjackService.getGame(username);
        if(gameOpt.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        int playerCoins = blackjackService.getPlayerCoins(username);
        return ResponseEntity.ok(BlackjackResponse.from(gameOpt.get(), playerCoins));
    }
    //POST /api/blackjack/forferit
    //When the player wants to forfeit and leave the game
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

    //POST /api/blackjack/double-down
    //Players double their bet and recieve exactly one more card then automatically stands. Only first action
    @PostMapping("/double-down")
    public ResponseEntity<?> doubleDown(Authentication authentication){
        String username = authentication.getName();
        Map<String, Object> errorResponse = new HashMap<>();
        String error = blackjackService.doubleDown(username);
        if(error != null){
            errorResponse.put("message", error);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        Optional<BlackjackGame> gameOpt = blackjackService.getGame(username);
        if(gameOpt.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        int playerCoins = blackjackService.getPlayerCoins(username);
        return ResponseEntity.ok(BlackjackResponse.from(gameOpt.get(), playerCoins));
    }
    //POST /api/blackjack/split
    //Split the player's two equal cards into two hands
    @PostMapping("/split")
    public ResponseEntity<?> split(Authentication authentication){
        String username = authentication.getName();
        Map<String, Object> errorResponse = new HashMap<>();
        String error = blackjackService.split(username);
        if(error != null){
            errorResponse.put("message", error);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        Optional<BlackjackGame> gameOpt = blackjackService.getGame(username);
        if(gameOpt.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        int playerCoins = blackjackService.getPlayerCoins(username);
        return ResponseEntity.ok(BlackjackResponse.from(gameOpt.get(), playerCoins));
    }
    //POST /api/blackjack/hit-split
    //Hit on active split hand
    @PostMapping("/hit-split")
    public ResponseEntity<?> hitSplit(Authentication authentication){
        String username = authentication.getName();
        Map<String, Object> errorResponse = new HashMap<>();
        String error = blackjackService.hitSplit(username);
        if(error != null){
            errorResponse.put("message", error);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        Optional<BlackjackGame> gameOpt = blackjackService.getGame(username);
        if(gameOpt.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        int playerCoins = blackjackService.getPlayerCoins(username);
        return ResponseEntity.ok(BlackjackResponse.from(gameOpt.get(), playerCoins));
    }
    //POST /api/blackjack/stand-split
    //Stand on active split hand
    @PostMapping("/stand-split")
    public ResponseEntity<?> standSplit(Authentication authentication){
        String username = authentication.getName();
        Map<String, Object> errorResponse = new HashMap<>();
        String error = blackjackService.standSplit(username);
        if(error != null){
            errorResponse.put("message", error);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        Optional<BlackjackGame> gameOpt = blackjackService.getGame(username);
        if(gameOpt.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        int playerCoins = blackjackService.getPlayerCoins(username);
        return ResponseEntity.ok(BlackjackResponse.from(gameOpt.get(),playerCoins));
    }
}