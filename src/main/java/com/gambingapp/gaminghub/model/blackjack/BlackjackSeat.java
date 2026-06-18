package com.gambingapp.gaminghub.model.blackjack;

import com.gambingapp.gaminghub.model.multiple.BotPlayer;
import com.gambingapp.gaminghub.model.multiple.Card;
import java.util.ArrayList;
import java.util.List;

/*This class will represent a single sea at a blackjack table
Seat 0 is the dealer
Seat 1 and 2 is a bot for single player
Seat 3 is player */

public class BlackjackSeat{
    public enum SeatType{
        DEALER,
        BOT,
        PLAYER
    }

    private final int seatNumber;
    private final SeatType seatType;
    private final BotPlayer botPlayer;
    private final List<Card> hand;
    private int bet;
    private boolean turnComplete;
    private boolean busted;
    private boolean hasBlackjack;
    private boolean stood;
    //For split
    private List<Card> splitHand;
    private boolean isSplit;
    private boolean splitHandComplete;
    private boolean splitHandBusted;
    private boolean splitHandStood;
    private int splitBet;

    //Constructor for dealer
    public BlackjackSeat(int seatNumber){
        this.seatNumber = seatNumber;
        this.seatType = SeatType.DEALER;
        this.botPlayer = null;
        this.hand = new ArrayList<>();
        reset();
    }

    //Constructor for bot
    public BlackjackSeat(int seatNumber, BotPlayer botPlayer){
        this.seatNumber = seatNumber;
        this.seatType = SeatType.BOT;
        this.botPlayer = botPlayer;
        this.hand = new ArrayList<>();
        reset();
    }

    //Constructor for player
    public BlackjackSeat(int seatNumber, SeatType seatType){
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.botPlayer = null;
        this.hand = new ArrayList<>();
        reset();
    }

    //reset the seat for new round
    public void reset(){
        hand.clear();
        bet = 0;
        turnComplete = false;
        busted = false;
        hasBlackjack = false;
        stood = false;
        splitHand = null;
        isSplit = false;
        splitHandComplete = false;
        splitHandBusted = false;
        splitHandStood = false;
        splitBet = 0;
    }

    //Add a card to each seat
    public void addCard(Card card){
        hand.add(card);
    }

    //return true if seat can split
    public boolean canSplit() {
        if(hand.size() != 2){
            return false;
        }
        return hand.get(0).getBlackjackValue() == hand.get(1).getBlackjackValue();
    }
    //split the cards 
    public void performSplit(){
        splitHand = new ArrayList<>();
        splitHand.add(hand.remove(1));
        isSplit = true;
        splitBet = bet;
    }
    //Add card to split hand
    public void addCardToSplitHand(Card card){
        if(splitHand != null){
            splitHand.add(card);
        }
    }
    //Calculate split hand total
    public int getSplitHandTotal(){
        if(splitHand == null){
            return 0;
        }
        int total = 0;
        int aces = 0;
        for (Card card: splitHand){
            if(!card.isFaceDown()){
                total += card.getBlackjackValue();
                if(card.getValue() == Card.Value.ACE){
                    aces++;
                }
            }
        }
        while(total > 21 && aces > 0){
            total -= 10;
            aces--;
        }
        return total;
    }
    //Return true if split hand is busted
    public boolean isSplitHandBust(){
        return getFullSplitHandTotal() > 21;
    }
    public int getFullHandTotal() {
        int total = 0;
        int aces = 0;
        for (Card card : hand) {
            total += card.getBlackjackValue();
            if (card.getValue() == Card.Value.ACE) {
                aces++;
            }
        }
        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }
        return total;
    }
    public int getFullSplitHandTotal() {
        if (splitHand == null) {
            return 0;
        }
        int total = 0;
        int aces = 0;
        for (Card card : splitHand) {
            total += card.getBlackjackValue();
            if (card.getValue() == Card.Value.ACE) {
                aces++;
            }
        }
        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }
        return total;
    }

    //Calculates the best possible hand
    //Automatically deals with aces
    public int getHandTotal(){
        int total = 0;
        int aces = 0;

        for(Card card: hand){
            if(!card.isFaceDown()){
                total += card.getBlackjackValue();
                if(card.getValue() == Card.Value.ACE){
                    aces++;
                }
            }
        }
        while(total > 21 && aces > 0){
            total -= 10;
            aces--;
        }
        return total;
    }
    //Returns true if the seat is over 21
    public boolean isBust(){
        return getHandTotal() > 21;
    }

    //Returns true if the seat has 21 on the first 2 cards
    public boolean isNaturalBlackjack(){
        return hand.size() == 2 && getHandTotal() == 21;
    }

    //Returns the name of the seat person
    public String getDisplayName(){
        switch(seatType){
            case DEALER: return "Dealer";
            case BOT: return botPlayer != null ? botPlayer.getName(): "Bot";
            case PLAYER: return  "You";
            default: return "Unknown";
        }
    }

    public int getSeatNumber(){
        return seatNumber;
    }
    public SeatType getSeatType(){
        return seatType;
    }
    public BotPlayer getBotPlayer(){
        return botPlayer;
    }
    public List<Card> getHand(){
        return hand;
    }
    public int getBet(){
        return bet;
    }
    public void setBet(int bet){
        this.bet = bet;
    }
    public boolean isTurnComplete(){
        return turnComplete;
    }
    public void setTurnComplete(boolean turnComplete){
        this.turnComplete = turnComplete;
    }
    public boolean isBusted(){
        return busted;
    }
    public void setBusted(boolean busted){
        this.busted = busted;
    }
    public boolean isHasBlackjack(){
        return hasBlackjack;
    }
    public void setHasBlackjack(boolean hasBlackjack){
        this.hasBlackjack = hasBlackjack;
    }
    public boolean isStood(){
        return stood;
    }
    public void setStood(boolean stood){
        this.stood = stood;
    }
    public List<Card> getSplitHand(){
        return splitHand;
    }
    public boolean isSplit(){
        return isSplit;
    }
    public int getSplitBet(){
        return splitBet;
    }
    public boolean isSplitHandComplete(){
        return splitHandComplete;
    }
    public void setSplitHandComplete(boolean b){
        splitHandComplete = b;
    }
    public boolean isSplitHandBusted(){
        return splitHandBusted;
    }
    public void setSplitHandBusted(boolean b){
        splitHandBusted = b;
    }
    public boolean isSplitHandStood(){
        return splitHandStood;
    }
    public void setSplitHandStood(boolean b){
        splitHandStood = b;
    }

}