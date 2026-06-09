package com.gambingapp.gaminghub.dto.blackjack;
//this class has the betting amount
public class BetRequest{
    private int betAmount;
    public BetRequest(){

    }
    public int getBetAmount(){
        return betAmount;
    }
    public void setBetAmount(int betAmount){
        this.betAmount = betAmount;
    }
}