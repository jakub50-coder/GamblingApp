//This class gives you the result of one baccarat game
package com.gambingapp.gaminghub.model.baccarat;

import com.gambingapp.gaminghub.model.multiple.Card;
import java.util.List;

public class BaccaratResult{
    public enum Outcome{
        PLAYER_WIN,
        BANKER_WIN,
        TIE
    }
    private final List<Card> playerHand;
    private final List<Card> bankerHand;
    private final int playerScore;
    private final int bankerScore;
    private final Outcome outcome;
    private final boolean playerPair;
    private final boolean bankerPair;
    private final boolean playerNatural;
    private final boolean bankerNatural;

    public BaccaratResult(List<Card> playerHand, List<Card> bankerHand, int playerScore, int bankerScore, Outcome outcome, boolean playerPair, boolean bankerPair, boolean playerNatural, boolean bankerNatural){
        this.playerHand = playerHand;
        this.bankerHand = bankerHand;
        this.playerScore = playerScore;
        this.bankerScore = bankerScore;
        this.outcome = outcome;
        this.playerPair = playerPair;
        this.bankerPair = bankerPair;
        this.playerNatural = playerNatural;
        this.bankerNatural = bankerNatural;
    }
    public List<Card> getPlayerHand(){
        return playerHand;
    }
    public List<Card> getBankerHand(){
        return bankerHand;
    }
    public int getPlayerScore(){
        return playerScore;
    }
    public int getBankerScore(){
        return bankerScore;
    }
    public Outcome getOutcome(){
        return outcome;
    }
    public boolean isPlayerPair(){
        return playerPair;
    }
    public boolean isBankerPair(){
        return bankerPair;
    }
    public boolean isPlayerNatural(){
        return playerNatural;
    }
    public boolean isBankerNatural(){
        return bankerNatural;
    }
}

