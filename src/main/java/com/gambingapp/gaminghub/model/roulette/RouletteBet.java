//This model a bet being placed on the roulette wheel.
package com.gambingapp.gaminghub.model.roulette;

public class RouletteBet{

    public enum BetType{
        STRAIGHT, //single number 35:1
        RED,      //red color 1:!
        BLACK,    //black color 1:!
        ODD,      //odd numbers 1:1
        EVEN,     //even numbers 1:1
        LOW,      //1-18 1:1    
        HIGH,     //19-26 1:1
        DOZEN,    //1-12, 13-24, 25-36, 2:1
        COLUMN    //1st, 2nd, or 3rd column, 2:1
    }
    private final BetType betType;
    private final int amount;
    private final int target; //(used for straight, dozen, and column bets)

    public RouletteBet(BetType betType, int amount, int target){
        this.betType = betType;
        this.amount = amount;
        this.target = target;
    }

    //return payout multiplier for the bet type
    public int getPayoutMultiplier(){
        switch(betType){
            case STRAIGHT: return 35;
            case DOZEN:
            case COLUMN: return 2;
            default: return 1;
        }
    }
    //return true if this bet wins 
    public boolean isWinner(RouletteNumber result, boolean hasDoubleZero){
        switch(betType){
            case STRAIGHT: return result.getNumber() == target;
            case RED: return result.isRed();
            case BLACK: return result.isBlack();
            case ODD: return result.isOdd();
            case EVEN: return result.isEven();
            case LOW: return result.isLow();
            case HIGH: return result.isHigh();
            case DOZEN: return result.getDozen() == target;
            case COLUMN: return result.getColumn() == target;
            default: return false;
        }
    }

    public BetType getBetType(){
        return betType;
    }
    public int getAmount(){
        return amount;
    }
    public int getTarget(){
        return target;
    }
}