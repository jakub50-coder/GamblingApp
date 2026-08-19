//This class represents the result of a single spin of a slot machine. It has the three reels wuth three rows,
//the winning payouts, payout multiplier, and coin change.
package com.gambingapp.gaminghub.model.slots;

public class SlotResult{
    private final SlotSymbol[][] reels;
    private final boolean [] winningLines;
    private final int totalMultiplier;
    private final int coinChange;
    private final String resultMessage;

    public SlotResult(SlotSymbol[][] reels, boolean[] winningLines, int totalMultiplier, int coinChange, String resultMessage){
        this.reels = reels;
        this.winningLines = winningLines;
        this.totalMultiplier = totalMultiplier;
        this.coinChange = coinChange;
        this.resultMessage = resultMessage;
    }

    public SlotSymbol[][] getReels(){
        return reels;
    }
    public boolean[] getWinningLines(){
        return winningLines;
    }
    public int getTotalMultiplier(){
        return totalMultiplier;
    }
    public int getCoinChange(){
        return coinChange;
    }
    public String getResultMessage(){
        return resultMessage;
    }
}   