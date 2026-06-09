package com.gambingapp.gaminghub.model.blackjack;

import com.gambingapp.gaminghub.model.multiple.BotPlayer;
import com.gambingapp.gaminghub.model.multiple.Deck;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

//The class the gaming state of blackjack

public class BlackjackGame{

    //Order of the game
    public enum GamePhase{
        WAITING_FOR_BET,
        DEALING,
        PLAYER_TURN,
        BOT_TURN,
        DEALER_TURN,
        ROUND_OVER
    }

    
    private final String username;
    private Deck deck;
    private final List<BlackjackSeat> seats;
    private GamePhase phase;
    private LocalDateTime turnStartedAt;
    private LocalDateTime disconnectedAt;
    private boolean playerDisconnected;
    private String roundResult;
    private int coinChange;

    //constructor
    public BlackjackGame(String username){
        this.username = username;
        this.deck = new Deck(6);
        this.seats = new ArrayList<>();
        this.phase = GamePhase.WAITING_FOR_BET;
        this.playerDisconnected = false;
        initializeSeats();
    }
    //prepare the seats
    private void initializeSeats() {
        seats.add(new BlackjackSeat(0));
        seats.add(new BlackjackSeat(1, new BotPlayer()));
        seats.add(new BlackjackSeat(2, new BotPlayer()));
        seats.add(new BlackjackSeat(3,BlackjackSeat.SeatType.PLAYER));
    }
    //Reset all seats
    public void resetForNewRound(){
        for(BlackjackSeat seat: seats){
            seat.reset();
        }
        if(deck.size() < 52){
            deck = new Deck(6);
        }
        phase = GamePhase.WAITING_FOR_BET;
        roundResult = null;
        coinChange = 0;
        turnStartedAt = null;
        playerDisconnected = false;
        disconnectedAt = null;
    }

    //return dealer seat
    public BlackjackSeat getDealerSeat(){
        return seats.get(0);
    }

    //return first bot seat
    public BlackjackSeat getFirstBotSeat(){
        return seats.get(1);
    }

    //return second bot seat
    public BlackjackSeat getSecondBotSeat(){
        return seats.get(2);
    }

    //return player seat
    public BlackjackSeat getPlayerSeat(){
        return seats.get(3);
    }
    //get all seats
    public List<BlackjackSeat> getSeats(){
        return seats;
    }

    //checks to see if 15 second timer is up
    public boolean isTurnTimerExpired(){
        if(turnStartedAt == null){
            return false;
        }
        return LocalDateTime.now().isAfter(turnStartedAt.plusSeconds(15));
    }

    //Check to see if 30 second reconnect window
    public boolean isReconnectWindowExpired(){
        if(disconnectedAt == null){
            return false;
        }
        return LocalDateTime.now().isAfter(disconnectedAt.plusSeconds(30));
    }

    //Start timer for each turn
    public void startTurnTimer(){
        turnStartedAt = LocalDateTime.now();
    }

    //Returns seconds remaining
    public long getSecondsRemaining(){
        if(turnStartedAt == null){
            return 15;
        }
        long elapsed = Duration.between(turnStartedAt, LocalDateTime.now()).toSeconds();
        return Math.max(0,15 - elapsed);
    }

    public String getUsername(){
        return username;
    }
    public Deck getDeck(){
        return deck;
    }
    public GamePhase getPhase(){
        return phase;
    }
    public void setPhase(GamePhase phase){
        this.phase = phase;
    }
    public LocalDateTime getTurnStartedAt(){
        return turnStartedAt;
    }
    public void setTurnStartedAt(LocalDateTime t){
        this.turnStartedAt = t;
    }
    public boolean isPlayerDisconnected(){
        return playerDisconnected;
    }
    public void setPlayerDisconnected(boolean d){
        this.playerDisconnected = d;
    }
    public String getRoundResult(){
        return roundResult;
    }
    public void setRoundResult(String roundResult){
        this.roundResult = roundResult;
    }
    public int getCoinChange(){
        return coinChange;
    }
    public void setCoinChange(int coinChange){
        this.coinChange = coinChange;
    }   
}