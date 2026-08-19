///This is the service class that handles the logic for spinning the roulette wheel and resolving bets.
package com.gambingapp.gaminghub.service.roulette;

import com.gambingapp.gaminghub.model.roulette.RouletteBet;
import com.gambingapp.gaminghub.model.roulette.RouletteNumber;
import com.gambingapp.gaminghub.service.UserService;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class RouletteService{
    private final UserService userService;

    public RouletteService(UserService userService){
        this.userService = userService;
    }

    //SPIN the roulette wheel and resolve all bets
    public Map<String, Object> spin(String username, List<Map<String, Object>> betsData, boolean americanWheel, boolean frenchWheel){
        String roundId = UUID.randomUUID().toString();
        List<RouletteBet> bets = new ArrayList<>();
        int totalBet = 0;
        for (Map<String, Object> betData : betsData) {
            String typeStr = (String) betData.get("betType");
            if (typeStr == null) continue;
            RouletteBet.BetType betType = RouletteBet.BetType.valueOf(typeStr);
            Number amtNum = (Number) betData.get("amount");
            if (amtNum == null) continue;
            int amount = amtNum.intValue();
            int target = 0;
            if (betData.containsKey("target") && betData.get("target") instanceof Number) {
                target = ((Number) betData.get("target")).intValue();
            }
            if (amount < 10 || amount > 100) {
                continue;
            }
            bets.add(new RouletteBet(betType, amount, target));
            totalBet += amount;
        }
        if(bets.isEmpty() || totalBet <= 0){
            Map<String, Object> error = new HashMap<>();
            error.put("error", "No valid bets placed");
            return error;
        }
        // Attempt to deduct the bet from the user's coins
        if (!userService.placeBet(username, totalBet, roundId, "roulette")) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Unable to place bet (insufficient coins)");
            return error;
        }

        int maxNumber = americanWheel ? 38 : 37; // 38 values (0..37) for American, 0..36 for European
        Random random = new Random();
        int spin = random.nextInt(maxNumber);
        boolean isDoubleZero = americanWheel && spin == 37; // treat 37 as 00
        int resultNumber = spin; // keep 37 to represent 00
        String displayNumber = isDoubleZero ? "00" : String.valueOf(resultNumber);
        RouletteNumber result = new RouletteNumber(resultNumber);
        //Resolve outside bets
        int totalWinnings = 0;
        List<Map<String,Object>> betResults = new ArrayList<>();
        boolean isZero = resultNumber == 0 || isDoubleZero; // La Partage applies on zero/00 for french wheel
        boolean isFrench = frenchWheel;
        for (RouletteBet bet : bets) {
            boolean won = bet.isWinner(result, americanWheel);
            Map<String, Object> betResult = new HashMap<>();
            betResult.put("betType", bet.getBetType().toString());
            betResult.put("amount", bet.getAmount());
            betResult.put("target", bet.getTarget());
            boolean isOutside = isOutsideBet(bet.getBetType());
            boolean isPartageApplies = isFrench && isZero && isOutside;
            if (isPartageApplies) {
                int halfBack = bet.getAmount() / 2;
                betResult.put("won", false);
                betResult.put("laPartage", true);
                betResult.put("payout", halfBack);
                totalWinnings += halfBack;
            } else if (won) {
                int payout = bet.getAmount() * (bet.getPayoutMultiplier() + 1);
                betResult.put("won", true);
                betResult.put("laPartage", false);
                betResult.put("payout", payout);
                totalWinnings += payout;
            }
            else{
                betResult.put("won",false);
                betResult.put("laPartage", false);
                betResult.put("payout", 0);
            }
            betResults.add(betResult);
        }
        if(totalWinnings > 0){
            userService.awardWinnings(username, totalWinnings, "roulette", roundId);
        }
        int coinChange = totalWinnings - totalBet;

        //Build response
        Map<String, Object> response = new HashMap<>();
        response.put("resultNumber", resultNumber);
        response.put("displayNumber", displayNumber);
        response.put("resultColor", (isDoubleZero || result.getNumber() == 0) ? "GREEN" : result.getColor().toString());
        response.put("isOdd", result.isOdd());
        response.put("isEven", result.isEven());
        response.put("isLow", result.isLow());
        response.put("isHigh", result.isHigh());
        response.put("dozen", result.getDozen());
        response.put("column", result.getColumn());
        response.put("bets", betResults);
        response.put("totalBet", totalBet);
        response.put("totalWinnings", totalWinnings);
        response.put("coinChange", coinChange);
        response.put("won", totalWinnings > 0);
        response.put("americanWheel", americanWheel);
        response.put("frenchWheel", frenchWheel);
        response.put("isPartageTriggered", frenchWheel && (resultNumber == 0 || isDoubleZero));
        return response;
    }

    private boolean isOutsideBet(com.gambingapp.gaminghub.model.roulette.RouletteBet.BetType t) {
        switch (t) {
            case RED:
            case BLACK:
            case ODD:
            case EVEN:
            case LOW:
            case HIGH:
                return true;
            default:
                return false;
        }
    }
}