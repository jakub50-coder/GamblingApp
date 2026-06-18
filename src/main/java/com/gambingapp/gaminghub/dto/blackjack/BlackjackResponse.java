package com.gambingapp.gaminghub.dto.blackjack;

import com.gambingapp.gaminghub.model.blackjack.BlackjackGame;
import com.gambingapp.gaminghub.model.blackjack.BlackjackSeat;
import com.gambingapp.gaminghub.model.multiple.Card;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Converts a Blackjack game object to a JSON response
public class BlackjackResponse{
    private String phase;
    private String roundResult;
    private int coinChange;
    private int playerCoins;
    private long secondsRemainingInTurn;
    private List<Map<String,Object>> seats;

    //build a response
    public static BlackjackResponse from(BlackjackGame game, int playerCoins){
        BlackjackResponse response = new BlackjackResponse();
        response.phase = game.getPhase().toString();
        response.roundResult = game.getRoundResult();
        response.coinChange = game.getCoinChange();
        response.playerCoins = playerCoins;
        response.secondsRemainingInTurn = game.getSecondsRemaining();
        response.seats = buildSeats(game);
        return response;
    }

    //Bulld seat data for each seat
    private static List<Map<String,Object>> buildSeats(BlackjackGame game){
        List<Map<String, Object>> seatList = new ArrayList<>();
        for(BlackjackSeat seat: game.getSeats()){
            Map<String, Object> seatMap = new HashMap<>();
            seatMap.put("seatNumber", seat.getSeatNumber());
            seatMap.put("seatType", seat.getSeatType().toString());
            seatMap.put("displayName", seat.getDisplayName());
            seatMap.put("bet", seat.getBet());
            seatMap.put("handTotal", seat.getHandTotal());
            seatMap.put("busted", seat.isBusted());
            seatMap.put("hasBlackjack", seat.isHasBlackjack());
            seatMap.put("stood", seat.isStood());
            seatMap.put("turnComplete", seat.isTurnComplete());
            seatMap.put("isSplit", seat.isSplit());
            if(seat.isSplit() && seat.getSplitHand() != null){
                seatMap.put("splitHandTotal", seat.getSplitHandTotal());
                seatMap.put("splitHandBusted", seat.isSplitHandBusted());
                seatMap.put("splitBet", seat.getSplitBet());
                seatMap.put("splitHandStood", seat.isSplitHandStood());
                seatMap.put("splitHandComplete", seat.isSplitHandComplete());
                List<Map<String,Object>> splitCards = new ArrayList<>();
                for(Card card: seat.getSplitHand()){
                    Map<String, Object> cardMap = new HashMap<>();
                    if(card.isFaceDown()){
                        cardMap.put("faceDown", true);
                        cardMap.put("displayValue", "?");
                        cardMap.put("suit", "?");
                        cardMap.put("isRed", false);
                    }
                    else{
                        cardMap.put("faceDown", false);
                        cardMap.put("displayValue", card.getDisplayValue());
                        cardMap.put("suit", card.getSuitSymbol());
                        cardMap.put("isRed",card.isRed());
                    }
                    splitCards.add(cardMap);
                }
                seatMap.put("splitCards", splitCards);
            }
            seatMap.put("activeHand", game != null && game.isPlayingSplitHand() ? "split": "main");
            if(seat.getSeatType() == BlackjackSeat.SeatType.BOT && seat.getBotPlayer() != null){
                seatMap.put("botPersonality", seat.getBotPlayer().getPersonality().toString());
                seatMap.put("botCoins", seat.getBotPlayer().getDisplayCoins());
            }

            List<Map<String, Object>> cards = new ArrayList<>();
            for(Card card: seat.getHand()){
                Map<String, Object> cardMap = new HashMap<>();
                if(card.isFaceDown()){
                    cardMap.put("faceDown", true);
                    cardMap.put("displayValue", "?");
                    cardMap.put("suit", "?");
                    cardMap.put("isRed", false);
                } else {
                    cardMap.put("faceDown", false);
                    cardMap.put("displayValue", card.getDisplayValue());
                    cardMap.put("suit", card.getSuitSymbol());
                    cardMap.put("isRed", card.isRed());
                }
                cards.add(cardMap);
            }
            seatMap.put("cards", cards);
            seatList.add(seatMap);
        }
        return seatList;
    }
    public String getPhase(){
        return phase;
    }
    public String getRoundResult(){
        return roundResult;
    }
    public int getCoinChange(){
        return coinChange;
    }
    public int getPlayerCoins(){
        return playerCoins;
    }
    public long getSecondsRemaining(){
        return secondsRemainingInTurn;
    }
    public List<Map<String, Object>> getSeats(){
        return seats;
    }
}