//This model represents a single number on the roulette wheet with all its properties used for bet resolution(color and so on)
package com.gambingapp.gaminghub.model.roulette;

public class RouletteNumber {
    public enum Color{
        RED,
        BLACK,
        GREEN
    }
    private final int number;
    private final Color color;

    //The numbers that are red on a roulette wheel
    private static final int[] RED_NUMBERS = {1,2,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36};

    public RouletteNumber(int number){
        this.number = number;
        this.color = determineColor(number);
    }

    //gets the color for each number on the roulette wheel
    private Color determineColor(int n){
        // treat 0 and 37 (double-zero) as green
        if(n == 0 || n == 37){
            return Color.GREEN;
        }
        for(int red: RED_NUMBERS){
            if(n == red){
                return Color.RED;
            }
        }
        return Color.BLACK;
    }

    public int getNumber(){
        return number;
    }
    public Color getColor(){
        return color;
    }
    public boolean isRed(){
        return color == Color.RED;
    }
    public boolean isBlack(){
        return color == Color.BLACK;
    }
    public boolean isGreen(){
        return color == Color.GREEN;
    }
    public boolean isEven(){
        return number >= 1 && number <= 36 && number % 2 == 0;
    }
    public boolean isOdd(){
        return number >= 1 && number <= 36 && number % 2 != 0;
    }
    public boolean isLow(){
        return number >= 1 && number <= 18;
    }
    public boolean isHigh(){
        return number >= 19 && number <= 36;
    }
    public int getDozen(){
        if(number >= 1 && number <= 12){
            return 1;
        }
        if(number >= 13 && number <= 24){
            return 2;
        }
        if(number >= 25 && number <= 36){
            return 3;
        }
        return 0;
    }
    public int getColumn(){
        if(number == 0 || number == 37){
            return 0;
        }
        return ((number - 1) % 3) + 1;
    }
}