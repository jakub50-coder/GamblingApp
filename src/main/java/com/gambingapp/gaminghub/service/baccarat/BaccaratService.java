//This class deals with the rules of baccarat

package com.gambingapp.gaminghub.service.baccarat;

import com.gambingapp.gaminghub.model.baccarat.BaccaratResult;
import com.gambingapp.gaminghub.model.multiple.Card;
import com.gambingapp.gaminghub.model.multiple.Deck;
import com.gambingapp.gaminghub.service.UserService;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class BaccaratService{
    private final UserService userService;
    private static final Set<String> VALID_BETS = Set.of("PLAYER", "BANKER", "TIE", "PLAYER_PAIR", "BANKER_PAIR");
    
    public BaccaratService(UserService userService){
        this.userService = userService;
    }
    //Deals a full baccarat round and resolves all bets
    public Map<String, Object> deal(String username, Map<String,Integer> bets){
        String roundId = UUID.randomUUID().toString();
        int totalBet = 0;
        for(Map.Entry<String, Integer> entry: bets.entrySet()){
            if (!VALID_BETS.contains(entry.getKey())) {
                return Map.of("error", "Invalid bet type: " + entry.getKey());
            }
            int amount = entry.getValue();
            if(amount < 50 || amount > 1000) continue;
            totalBet += amount;
        }
        if (totalBet <= 0 || !userService.placeBet(username, totalBet, roundId, "baccarat")) {
            return Map.of("error", "Unable to place bet (insufficient coins)");
        }
        Deck deck = new Deck(8); //Standard 8 decks for baccarat

        //Intially deal 4 cards
        List<Card> playerHand = new ArrayList<>();
        List<Card> bankerHand = new ArrayList<>();
        // 2 cards for player and 2 cards for banker
        playerHand.add(deck.draw());
        bankerHand.add(deck.draw());
        playerHand.add(deck.draw());
        bankerHand.add(deck.draw());

        int playerScore = calculateScore(playerHand);
        int bankerScore = calculateScore(bankerHand);
        boolean playerNatural = playerScore >= 8;
        boolean bankerNatural = bankerScore >= 8;

        //Draw a third card unless one hand is natural
        if(!playerNatural && !bankerNatural){
            boolean playerDrewThird = false;
            Card playerThird = null;

            if(playerScore <= 5){
                playerThird = deck.draw();
                playerHand.add(playerThird);
                playerScore = calculateScore(playerHand);
                playerDrewThird = true;
            }
            if(playerDrewThird){
                int p3Value = getBaccaratValue(playerThird);
                boolean bankerDraws = false;
                switch(bankerScore){
                    case 0:
                    case 1:
                    case 2: bankerDraws = true; break;
                    case 3: bankerDraws = p3Value != 0; break;
                    case 4: bankerDraws = p3Value >= 2 && p3Value <= 7; break;
                    case 5: bankerDraws = p3Value >= 4 && p3Value <= 7; break;
                    case 6: bankerDraws = p3Value == 6 || p3Value == 7; break;
                    case 7: bankerDraws = false;  break;  
                }
                if(bankerDraws){
                    bankerHand.add(deck.draw());
                    bankerScore = calculateScore(bankerHand);
                }
            }
            else{
                if(bankerScore <= 5){
                    bankerHand.add(deck.draw());
                    bankerScore = calculateScore(bankerHand);
                }
            }
        }
        //Determine outcome
        BaccaratResult.Outcome outcome;
        if(playerScore > bankerScore){
            outcome = BaccaratResult.Outcome.PLAYER_WIN;
        }
        else if(bankerScore > playerScore){
            outcome = BaccaratResult.Outcome.BANKER_WIN;
        }
        else{
            outcome = BaccaratResult.Outcome.TIE;
        }
        boolean playerPair = playerHand.get(0).getValue() == playerHand.get(1).getValue();
        boolean bankerPair = bankerHand.get(0).getValue() == bankerHand.get(1).getValue();

        int totalWinnings = 0;
        Map<String, Object> betResults = new HashMap<>();
        //Player bet
        if(bets.containsKey("PLAYER") && bets.get("PLAYER") >= 10){
            int amount = bets.get("PLAYER");
            if(outcome == BaccaratResult.Outcome.PLAYER_WIN){
                int payout = amount * 2;
                totalWinnings += payout;
                betResults.put("playerBet", Map.of("won", true, "payout", payout));
            }
            else if(outcome == BaccaratResult.Outcome.TIE){
                totalWinnings += amount;
                betResults.put("playerBet", Map.of("won", false, "push", true, "payout", amount));
            }
            else{
                betResults.put("playerBet", Map.of("won", false, "payout", 0));
            }
        }
        //Banker bet - 5% commission on wins
        if(bets.containsKey("BANKER") && bets.get("BANKER") >= 10){
            int amount = bets.get("BANKER");
            if(outcome == BaccaratResult.Outcome.BANKER_WIN){
                int payout = amount + (int) Math.floor(amount * 0.95);
                totalWinnings += payout;
                betResults.put("bankerBet", Map.of("won", true, "payout", payout));
            }
            else if(outcome == BaccaratResult.Outcome.TIE){
                totalWinnings += amount;
                betResults.put("bankerBet", Map.of("won", false, "push", true, "payout", amount));
            }
            else{
                betResults.put("bankerBet", Map.of("won", false, "payout", 0));
            }
        }
        if(bets.containsKey("TIE") && bets.get("TIE") >= 10){
            int amount = bets.get("TIE");
            if(outcome == BaccaratResult.Outcome.TIE){
                int payout = amount * 9;
                totalWinnings += payout;
                betResults.put("tieBet", Map.of("won", true, "payout", payout));
            }
            else{
                betResults.put("tieBet", Map.of("won", false, "payout", 0));
            }
        }
        if(bets.containsKey("PLAYER_PAIR") && bets.get("PLAYER_PAIR") >= 10){
            int amount = bets.get("PLAYER_PAIR");
            if(playerPair){
                int payout = amount * 12;
                totalWinnings += payout;
                betResults.put("playerPairBet", Map.of("won", true, "payout", payout));
            }
            else{
                betResults.put("playerPairBet", Map.of("won", false, "payout", 0));
            }
        }
        if(bets.containsKey("BANKER_PAIR") && bets.get("BANKER_PAIR") >= 10){
            int amount = bets.get("BANKER_PAIR");
            if(bankerPair){
                int payout = amount * 12;
                totalWinnings += payout;
                betResults.put("bankerPairBet", Map.of("won", true, "payout", payout));
            }
            else{
                betResults.put("bankerPairBet", Map.of("won", false, "payout", 0));
            }
        }
        //Award Winnings
        if(totalWinnings > 0){
            userService.awardWinnings(username, totalWinnings, "baccarat", roundId);
        }
        int coinChange = totalWinnings - totalBet;

        Map<String, Object> response = new HashMap<>();
        response.put("playerHand", buildCardsList(playerHand));
        response.put("bankerHand", buildCardsList(bankerHand));
        response.put("playerScore", playerScore);
        response.put("bankerScore", bankerScore);
        response.put("outcome", outcome.toString());
        response.put("playerNatural", playerNatural);
        response.put("bankerNatural", bankerNatural);
        response.put("playerPair", playerPair);
        response.put("bankerPair", bankerPair);
        response.put("betResults", betResults);
        response.put("totalBet", totalBet);
        response.put("totalWinnings", totalWinnings);
        response.put("coinChange", coinChange);
        response.put("won", totalWinnings > totalBet);
        return response;
    }
    //Score Calculation
    private int calculateScore(List<Card> hand){
        int total = 0;
        for(Card card: hand){
            total += getBaccaratValue(card);
        }
        return total % 10;
    }
    //Baccarat Card Value
    private int getBaccaratValue(Card card){
        switch(card.getValue()){
            case ACE: return 1;
            case TWO: return 2;
            case THREE: return 3;
            case FOUR: return 4;
            case FIVE: return 5;
            case SIX: return 6;
            case SEVEN: return 7;
            case EIGHT: return 8;
            case NINE: return 9;
            default: return 0; //for 10, J, Q, K
        }
    }
    //Builds a card list for JSON Response
    private List<Map<String, Object>> buildCardsList(List<Card> hand){
        List<Map<String, Object>> list = new ArrayList<>();
        for(Card card: hand){
            Map<String, Object> c = new HashMap<>();
            c.put("displayValue", card.getDisplayValue());
            c.put("suit", card.getSuitSymbol());
            c.put("isRed", card.isRed());
            list.add(c);
        }
        return list;
    }
}
