//This is the serivce layer for slots game
package com.gambingapp.gaminghub.service.slots;

import com.gambingapp.gaminghub.model.slots.SlotSymbol;
import com.gambingapp.gaminghub.model.slots.SlotResult;
import com.gambingapp.gaminghub.service.UserService;
import org.springframework.stereotype.Service;
import java.util.Random;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

@Service
public class SlotService{

    private final UserService userService;
    private final Random random = new Random();

    //Symbol weights which represent how common a symbol will appear, the higher the number to more likely it is to appear
    private static final int[] WEIGHTS = {
        15, //cherry
        20, //lemon
        19, //grapes
        15, //star
        13, //bell
        11, //diamond
        7 //seven
    };

    private static final int TOTAL_WEIGHT = 15 + 20 + 19 + 15 + 13 + 11 + 7;

    public SlotService(UserService userService){
        this.userService = userService;
    }
    //This method gets thet bets, spins the slot, calulates the winnings and return the result
    public SlotResult spin(String username, int betAmount){
        String roundId = UUID.randomUUID().toString();
        if (!userService.placeBet(username, betAmount, roundId, "slots")) {
            throw new IllegalStateException("Not enough coins to place that bet");
        }

        SlotSymbol[][] reels = new SlotSymbol[3][3];
        for(int reel = 0; reel < 3; reel++){
            for(int row = 0; row < 3; row++){
                reels[reel][row] = randomSymbol();
            }
        }
        boolean[] winningLines = new boolean[3];
        int totalMultiplier = 0;
        StringBuilder resultMsg = new StringBuilder();

        for(int row = 0; row < 3; row++){
            SlotSymbol left = reels[0][row];
            SlotSymbol middle = reels[1][row];
            SlotSymbol right  = reels[2][row];

            int multiplier = calculateMultiplier(left, middle, right);
            if(multiplier > 0){
                winningLines[row] = true;
                totalMultiplier += multiplier;

                if(resultMsg.length() > 0){
                    resultMsg.append(", ");
                }
                resultMsg.append(left.getEmoji()).append(middle.getEmoji()).append(right.getEmoji()).append(" x").append(multiplier);
            }
        }
        //calulating coin change
        int coinChange;
        String finalMessage;

        if(totalMultiplier > 0){
            int winnings = betAmount * totalMultiplier;
            coinChange = winnings - betAmount;
            userService.awardWinnings(username, winnings, "slots", roundId);
            if(totalMultiplier >= 50){
                finalMessage = "Jackpot! " + resultMsg.toString();
            }
            else{
                finalMessage = "You Win! " + resultMsg.toString();
            }
        }
        else{
            coinChange = -betAmount;
            finalMessage = "No match - try again!";
        }
        return new SlotResult(reels, winningLines, totalMultiplier, coinChange, finalMessage);    
    }
    //return a random symbol
    private SlotSymbol randomSymbol(){
        int roll = random.nextInt(TOTAL_WEIGHT);
        int cumulative = 0;
        SlotSymbol[] symbols = SlotSymbol.values();
        for(int i = 0; i < symbols.length; i++){
            cumulative += WEIGHTS[i];
            if(roll < cumulative){
                return symbols[i];
            }
        }
        return SlotSymbol.CHERRY;
    }
    //Returns the payout multiplier
    private int calculateMultiplier(SlotSymbol left, SlotSymbol middle, SlotSymbol right){
        if(left == middle && middle == right){
            switch(left){
                case SEVEN: return 50;
                case DIAMOND: return 20;
                case BELL: return 15;
                case STAR: return 10;
                case GRAPES: return 8;
                case LEMON: return 5;
                case CHERRY: return 3;
                default: return 0;
            }
        }
        if(left == SlotSymbol.CHERRY && middle == SlotSymbol.CHERRY){
            return 2;
        }
        if(left == SlotSymbol.CHERRY){
            return 1;
        }
        return 0;
    }
    //Display for when the user wins
    public List<Map<String,Object>> getPayoutTable(){
        List<Map<String,Object>> table = new ArrayList<>();
        
        addPayout(table, "7️⃣7️⃣7️⃣", "Jackpot!", 50);
        addPayout(table, "💎💎💎", "Diamonds", 20);
        addPayout(table, "🔔🔔🔔", "Bells", 15);
        addPayout(table, "⭐⭐⭐", "Stars", 10);
        addPayout(table, "🍇🍇🍇", "Grapes", 8);
        addPayout(table, "🍋🍋🍋", "Lemons", 5);
        addPayout(table, "🍒🍒🍒", "Cherries", 3);
        addPayout(table, "🍒🍒 —", "Two Cherries", 2);
        addPayout(table, "🍒 — —", "One Cherry", 1);
        return table;
    }
    private void addPayout(List<Map<String, Object>> table, String symbols, String name, int multiplier){
        Map<String,Object> entry = new HashMap<>();
        entry.put("symbols", symbols);
        entry.put("name", name);
        entry.put("multiplier", multiplier);
        table.add(entry);
    }
}