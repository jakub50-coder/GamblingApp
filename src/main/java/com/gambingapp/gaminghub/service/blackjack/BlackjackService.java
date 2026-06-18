package com.gambingapp.gaminghub.service.blackjack;

import com.gambingapp.gaminghub.model.blackjack.BlackjackGame;
import com.gambingapp.gaminghub.model.blackjack.BlackjackSeat;
import com.gambingapp.gaminghub.model.multiple.Card;
import com.gambingapp.gaminghub.service.UserService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
//This class is where all the logic for the game blakcjack is
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
            return null;
        }
        Card dealerUpCard = game.getDealerSeat().getHand().get(0);
        boolean dealerShouldPeek = dealerUpCard.getBlackjackValue() == 11 || dealerUpCard.getBlackjackValue() == 10;
        if(dealerShouldPeek && game.getDealerSeat().isNaturalBlackjack()){
            revealDealerHoleCard(game);
            game.getDealerSeat().setHasBlackjack(true);
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

    public String hitSplit(String username) {
        Optional<BlackjackGame> gameOpt = getGame(username);
        if (gameOpt.isEmpty()) {
            return "No active game found.";
        }

        BlackjackGame game = gameOpt.get();
        if (!game.isPlayingSplitHand()) {
            return "There is no active split hand.";
        }
        if (game.getPhase() != BlackjackGame.GamePhase.PLAYER_TURN) {
            return "It is not your turn";
        }
        if (game.isTurnTimerExpired()) {
            forfeitRound(game, username);
            return null;
        }

        BlackjackSeat player = game.getPlayerSeat();
        if (player.getSplitHand() == null) {
            return "No split hand available.";
        }

        Card card = game.getDeck().draw();
        if (card == null) {
            return "No more cards available.";
        }

        card.setFaceDown(false);
        player.addCardToSplitHand(card);

        if (player.getFullSplitHandTotal() > 21) {
            player.setSplitHandBusted(true);
            player.setSplitHandComplete(true);
            runBotTurns(game);
            runDealerTurn(game);
            resolveSplitRound(game, username);
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
    //When the player surrenders and loses half of its bet
    public String surrender(String username){
        Optional<BlackjackGame> gameOpt = getGame(username);
        if(gameOpt.isEmpty()){
            return "No active game found";
        }
        BlackjackGame game = gameOpt.get();
        if(game.getPhase() != BlackjackGame.GamePhase.PLAYER_TURN){
            return "Cannot surrender at this point";
        }
        if(game.getPlayerSeat().getHand().size() != 2){
            return "Can only surrender on your first turn";
        }
        int bet = game.getPlayerSeat().getBet();
        int halfBet = bet/2;
        userService.awardWinnings(username, halfBet, "blackjack", game.getRoundId());
        game.getPlayerSeat().setTurnComplete(true);
        game.setRoundResult("SURRENDER");
        game.setCoinChange(-(bet - halfBet));
        game.setPhase(BlackjackGame.GamePhase.ROUND_OVER);
        revealDealerHoleCard(game);
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
    //Double down option (an option where a player can double their bet if they are confident)
    //Player gains only one card and stand. Only can be done in beginning.
    public String doubleDown(String username){
        Optional<BlackjackGame> gameOpt = getGame(username);
        if(gameOpt.isEmpty()){
            return "No active game found.";
        }
        BlackjackGame game = gameOpt.get();
        if(game.getPhase() != BlackjackGame.GamePhase.PLAYER_TURN){
            return "Cannot double down at this point.";
        }
        if(game.getPlayerSeat().getHand().size() !=2){
            return "Can only double down on your first turn";
        }
        int originalBet = game.getPlayerSeat().getBet();
        int playerCoins = getPlayerCoins(username);
        if(playerCoins < originalBet){
            return "Not enough coins to double down.";
        }
        boolean betPlaced = userService.placeBet(username, originalBet, game.getRoundId());
        if(!betPlaced){
            return "Not enough coins to double down";
        }
        game.getPlayerSeat().setBet(originalBet * 2);
        dealCard(game.getPlayerSeat(), game, false);
        if(game.getPlayerSeat().isBusted()){
            game.getPlayerSeat().setBusted(true);
            game.getPlayerSeat().setTurnComplete(true);
            resolveRound(game, username);
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
    //splits the cards when allowed
    public String split(String username){
        Optional<BlackjackGame> gameOpt = getGame(username);
        if(gameOpt.isEmpty()){
            return "No active game found.";
        }
        BlackjackGame game = gameOpt.get();
        if(game.getPhase() != BlackjackGame.GamePhase.PLAYER_TURN){
            return "Cannot split at this point";
        }
        BlackjackSeat playerSeat = game.getPlayerSeat();
        if(!playerSeat.canSplit()){
            return "Can only split when the first two cards have the same value";
        }
        int originalBet = playerSeat.getBet();
        int playerCoins = getPlayerCoins(username);
        if(playerCoins < originalBet){
            return "Not enough coins to split.";
        }
        boolean betPlaced = userService.placeBet(username, originalBet, game.getRoundId() + "-split");
        if(!betPlaced){
            return "Not enough coins to split";
        }
        playerSeat.performSplit();
        dealCard(playerSeat, game, false);
        playerSeat.addCardToSplitHand(game.getDeck().draw());
        game.setPlayingSplitHand(false);
        game.startTurnTimer();
        return null;
    }
    public String standSplit(String username){
        Optional<BlackjackGame> gameOpt = getGame(username);
        if(gameOpt.isEmpty()){
            return "No active game found.";
        }
        BlackjackGame game = gameOpt.get();
        BlackjackSeat player = game.getPlayerSeat();
        if(game.isPlayingSplitHand()){
            player.setSplitHandStood(true);
            player.setSplitHandComplete(true);
            runBotTurns(game);
            runDealerTurn(game);
            resolveSplitRound(game, username);
        }
        else{
            player.setStood(true);
            player.setTurnComplete(true);
            game.setPlayingSplitHand(true);
            game.startTurnTimer();
        }
        return null;
    }
    //resolve both split hands
    private void resolveSplitRound(BlackjackGame game, String username){
        game.setPhase(BlackjackGame.GamePhase.ROUND_OVER);
        revealDealerHoleCard(game);
        BlackjackSeat player = game.getPlayerSeat();
        BlackjackSeat dealer = game.getDealerSeat();
        int dealerTotal = dealer.getFullHandTotal();
        boolean dealerBusted = dealer.isBust();
        boolean dealerBlackjack = dealer.isNaturalBlackjack();
        int totalCoinChange = 0;
        StringBuilder resultBuilder = new StringBuilder();
        int mainResult = resolveHand(player.getFullHandTotal(), player.isBusted(), false, dealerTotal, dealerBusted, dealerBlackjack, player.getBet(), username, "blackjack", game.getRoundId());
        totalCoinChange += mainResult;
        resultBuilder.append(mainResult >= 0 ? "WIN" : "LOSE");
        if(player.getSplitHand() != null){
            int splitResult = resolveHand(player.getFullSplitHandTotal(), player.isSplitHandBusted(), false, dealerTotal, dealerBusted, dealerBlackjack, player.getSplitBet(), username, "blackjack", game.getRoundId() + "-split");
            totalCoinChange += splitResult;
            resultBuilder.append("/");
            resultBuilder.append(splitResult >= 0 ? "WIN" : "LOSE");
        }
        game.setRoundResult("SPLIT_" + resultBuilder.toString());
        game.setCoinChange(totalCoinChange);
    }
    //resolve a one of the two hands from the split
    private int resolveHand(int handTotal, boolean busted, boolean hasBlackjack, int dealerTotal, boolean dealerBusted, boolean dealerBlackjack, int bet, String username, String game, String roundId){
        if(busted){
            return - bet;
        }
        else if(dealerBlackjack){
            return - bet;
        }
        else if(dealerBusted || handTotal > dealerTotal){
            userService.awardWinnings(username, bet * 2, game, roundId);
            return bet;
        }
        else if(handTotal == dealerTotal){
            userService.awardWinnings(username, bet, game, roundId);
            return 0;
        }
        else{
            return -bet;
        }
    }
}
