package com.gambingapp.gaminghub.model.multiple;

import java.util.Random;

//In this class, each bot has its own personality which determines how the bot plays plus a random name;
public class BotPlayer{

    //three bot personalities
    public enum Difficulty{
        PASSIVE,
        NEUTRAL,
        AGGRESSIVE
    }

    //a bunch of names that the bots can have
    private static final String[] BOT_NAMES = {
        "Liam", "Emma", "Noah", "Olivia", "Elijah", "Ava", "James", "Sophia",
        "William", "Isabella", "Peter", "Sam", "Josh", "Curtis", "Nicole", "Alexander"
    };

    private final String name;
    private final Difficulty personality;
    private int displayCoins;
    private int currentBet;

    //the creation of the bot player
    public BotPlayer(){
        Random random = new Random();
        this.name = BOT_NAMES[random.nextInt(BOT_NAMES.length)];
        this.personality = Difficulty.values()[random.nextInt(Difficulty.values().length)];
        this.displayCoins = 500 + random.nextInt(1500);
        this.currentBet = 0;
    }

    //how the bot decides how much it wants to bet
    public int decideBet(){
        Random random = new Random();
        switch(personality){
            case PASSIVE:
                currentBet = 10 + random.nextInt(16);
                break;
            case NEUTRAL:
                currentBet = 20 + random.nextInt(31);
                break;
            case AGGRESSIVE:
                currentBet = 40 + random.nextInt(36);
                break;
            default:
                currentBet = 10;
        }
        return currentBet;
    }

    //for blackjack, based on the personality, it will determine if the bot should stop or hit
    public boolean shouldHit(int handTotal){
        switch(personality){
            case PASSIVE: return handTotal < 15;
            case NEUTRAL: return handTotal < 17;
            case AGGRESSIVE: return handTotal < 19;
            default: return handTotal < 17;
        }
    }

    //based on the personality, it will determine how fast the bot thinks
    public int getThinkDelayMs(){
        Random random = new Random();
        switch(personality){
            case PASSIVE: return 1200 + random.nextInt(800);
            case NEUTRAL: return 800 + random.nextInt(600);
            case AGGRESSIVE: return 400 + random.nextInt(400);
            default: return 800;
        }
    }

    public String getName(){
        return name;
    }

    public Difficulty getPersonality(){
        return personality;
    }

    public int getDisplayCoins(){
        return displayCoins;
    }

    public int getCurrentBet(){
        return currentBet;
    }
}