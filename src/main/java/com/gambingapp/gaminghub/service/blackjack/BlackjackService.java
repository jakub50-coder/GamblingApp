package com.gambingapp.gaminghub.service.blackjack;

import com.gambingapp.gaminghub.model.blackjack.BlackjackGame;
import com.gambingapp.gaminghub.model.blackjack.BlackjackSeat;
import com.gambingapp.gaminghub.model.multiple.Card;
import com.gambingapp.gaminghub.service.UserService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
//This class is where all the logic for the game is
@Service
public class BlackjackService {
    private final UserService userService;
    private final Map<String, BlackjackGame> activeGames = new HashMap<>();

    public BlackjackService(UserService userService) {
        this.userService = userService;
    }

    public Optional<BlackjackGame> getGame(String username) {
        return Optional.ofNullable(activeGames.get(username));
    }
    //Either create or get a current game
    public BlackjackGame getOrCreateGame(String username) {
        return activeGames.computeIfAbsent(username, BlackjackGame::new);
    }
    //start the round by putting a minimum bet and the dealer dealing the cards
    public String startRound(String username, int betAmount) {
        if (betAmount < 10) {
            return "Minimum bet is 10 coins";
        }
        if (betAmount > 75) {
            return "Maximum bet is 75 coins";
        }

        BlackjackGame game = getOrCreateGame(username);
        // Reset if game is in ROUND_OVER or any other invalid state for betting
        if (game.getPhase() != BlackjackGame.GamePhase.WAITING_FOR_BET) {
            if (game.getPhase() == BlackjackGame.GamePhase.ROUND_OVER) {
                game.resetForNewRound();
            } else {
                return "A round is already in progress. Current phase: " + game.getPhase();
            }
        }

        if (!userService.placeBet(username, betAmount, game.getRoundId())) {
            return "Not enough coins to place that bet";
        }

        game.getPlayerSeat().setBet(betAmount);
        game.getFirstBotSeat().setBet(game.getFirstBotSeat().getBotPlayer().decideBet());
        game.getSecondBotSeat().setBet(game.getSecondBotSeat().getBotPlayer().decideBet());

        dealCard(game.getPlayerSeat(), game, false);
        dealCard(game.getFirstBotSeat(), game, false);
        dealCard(game.getSecondBotSeat(), game, false);
        dealCard(game.getDealerSeat(), game, false);

        dealCard(game.getPlayerSeat(), game, false);
        dealCard(game.getFirstBotSeat(), game, false);
        dealCard(game.getSecondBotSeat(), game, false);
        dealCard(game.getDealerSeat(), game, true);

        game.setPhase(BlackjackGame.GamePhase.PLAYER_TURN);
        game.startTurnTimer();

        if (game.getPlayerSeat().isNaturalBlackjack()) {
            game.getPlayerSeat().setHasBlackjack(true);
            resolveRound(game, username);
        }
        return null;
    }
    //when a player wants to hit or get another card
    public String hit(String username) {
        Optional<BlackjackGame> gameOpt = getGame(username);
        if (gameOpt.isEmpty()) {
            return "No active game found.";
        }

        BlackjackGame game = gameOpt.get();
        if (game.getPhase() != BlackjackGame.GamePhase.PLAYER_TURN) {
            return "It is not your turn";
        }
        if (game.isTurnTimerExpired()) {
            forfeitRound(game, username);
            return null;
        }

        dealCard(game.getPlayerSeat(), game, false);

        if (game.getPlayerSeat().isBust()) {
            game.getPlayerSeat().setBusted(true);
            game.getPlayerSeat().setTurnComplete(true);
            resolveRound(game, username);
        }
        return null;
    }
    //when the player stands or is happy with the total
    public String stand(String username) {
        Optional<BlackjackGame> gameOpt = getGame(username);
        if (gameOpt.isEmpty()) {
            return "no active game found";
        }

        BlackjackGame game = gameOpt.get();
        if (game.getPhase() != BlackjackGame.GamePhase.PLAYER_TURN) {
            return "It is not your turn";
        }
        if (game.isTurnTimerExpired()) {
            forfeitRound(game, username);
            return null;
        }

        game.getPlayerSeat().setStood(true);
        game.getPlayerSeat().setTurnComplete(true);
        game.setPhase(BlackjackGame.GamePhase.BOT_TURN);
        runBotTurns(game);
        runDealerTurn(game);
        resolveRound(game, username);
        return null;
    }
    //when the player forfeits the round
    public void forfeitRound(BlackjackGame game, String username) {
        game.getPlayerSeat().setBusted(true);
        game.getPlayerSeat().setTurnComplete(true);
        game.setRoundResult("FORFEIT");
        game.setCoinChange(-game.getPlayerSeat().getBet());
        game.setPhase(BlackjackGame.GamePhase.ROUND_OVER);
        revealDealerHoleCard(game);
    }
    //lets the two bots run their turns
    private void runBotTurns(BlackjackGame game) {
        game.setPhase(BlackjackGame.GamePhase.BOT_TURN);
        runBotTurn(game, game.getFirstBotSeat());
        runBotTurn(game, game.getSecondBotSeat());
    }
    //specifics into each bots turn
    private void runBotTurn(BlackjackGame game, BlackjackSeat botSeat) {
        while (!botSeat.isTurnComplete()) {
            int total = botSeat.getHandTotal();
            if (botSeat.getBotPlayer().shouldHit(total)) {
                dealCard(botSeat, game, false);
                if (botSeat.isBust()) {
                    botSeat.setBusted(true);
                    botSeat.setTurnComplete(true);
                }
            } else {
                botSeat.setStood(true);
                botSeat.setTurnComplete(true);
            }
        }
    }
    //dealer's turn
    private void runDealerTurn(BlackjackGame game) {
        game.setPhase(BlackjackGame.GamePhase.DEALER_TURN);
        revealDealerHoleCard(game);
        BlackjackSeat dealer = game.getDealerSeat();
        while (dealer.getHandTotal() < 17) {
            dealCard(dealer, game, false);
        }
        if (dealer.isBust()) {
            dealer.setBusted(true);
        }
        dealer.setTurnComplete(true);
    }
    //final results of the round with scenarios for each result
    private void resolveRound(BlackjackGame game, String username) {
        game.setPhase(BlackjackGame.GamePhase.ROUND_OVER);
        revealDealerHoleCard(game);

        BlackjackSeat player = game.getPlayerSeat();
        BlackjackSeat dealer = game.getDealerSeat();
        int playerTotal = player.getHandTotal();
        int dealerTotal = dealer.getHandTotal();
        int bet = player.getBet();
        boolean dealerBlackjack = dealer.isNaturalBlackjack();
        String roundId = game.getRoundId();

        String result;
        int coinChange;

        if (player.isBust()) {
            result = "BUST";
            coinChange = -bet;
        } 
        else if (player.isHasBlackjack() && dealerBlackjack) {
            result = "PUSH";
            coinChange = 0;
        } 
        else if (player.isHasBlackjack()) {
            int winnings = bet + (int) Math.floor(bet * 1.5);
            result = "BLACKJACK";
            coinChange = (int) Math.floor(bet * 1.5);
            userService.awardWinnings(username, winnings, "blackjack",roundId);
        } 
        else if (dealerBlackjack) {
            result = "DEALER_BLACKJACK";
            coinChange = -bet;
        } 
        else if (dealer.isBust()) {
            int winnings = bet * 2;
            result = "WIN";
            coinChange = bet;
            userService.awardWinnings(username, winnings, "blackjack", roundId);
        } 
        else if (playerTotal > dealerTotal) {
            int winnings = bet * 2;
            result = "WIN";
            coinChange = bet;
            userService.awardWinnings(username, winnings, "blackjack", roundId);
        } 
        else if (playerTotal < dealerTotal) {
            result = "LOSE";
            coinChange = -bet;
        } 
        else {
            result = "PUSH";
            coinChange = 0;
        }

        game.setRoundResult(result);
        game.setCoinChange(coinChange);
    }
    //deals the card for each seat
    private void dealCard(BlackjackSeat seat, BlackjackGame game, boolean faceDown) {
        Card card = game.getDeck().draw();
        if (card != null) {
            card.setFaceDown(faceDown);
            seat.addCard(card);
        }
    }
    //reveals card that is hidden for dealer
    private void revealDealerHoleCard(BlackjackGame game) {
        for (Card card : game.getDealerSeat().getHand()) {
            if (card.isFaceDown()) {
                card.setFaceDown(false);
            }
        }
    }
    //get the players coin total
    public int getPlayerCoins(String username) {
        return userService.getUser(username).map(user -> user.getCoins()).orElse(0);
    }
}
