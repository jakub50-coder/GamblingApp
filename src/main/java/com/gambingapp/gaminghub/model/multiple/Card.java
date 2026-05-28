//This class creates the card of the deck
package com.gambingapp.gaminghub.model.multiple;

public class Card {

    //possible suit
    public enum Suit{
        HEARTS,
        DIAMONDS,
        CLUBS,
        SPADES
    }
    // possible value for each card
    public enum Value{
        TWO,
        THREE,
        FOUR,
        FIVE,
        SIX,
        SEVEN,
        EIGHT,
        NINE,
        TEN,
        JACK,
        QUEEN,
        KING, 
        ACE
    }

    private final Suit suit;
    private final Value value;
    private boolean faceDown;

    public Card(Suit suit, Value value){
        this.suit = suit;
        this.value = value;
        this.faceDown = false;
    }

    public Suit getSuit() {
        return suit;
    }

    public Value getValue() {
        return value;
    }

    public boolean isFaceDown(){
        return faceDown;
    }

    public void setFaceDown(boolean faceDown){
        this.faceDown = faceDown;
    }

    //value of each card for blackjack
    public int getBlackjackValue() {
        switch(value){
            case TWO: return 2;
            case THREE: return 3;
            case FOUR: return 4;
            case FIVE: return 5;
            case SIX: return 6;
            case SEVEN: return 7;
            case EIGHT: return 8;
            case NINE: return 9;
            case TEN: return 10;
            case JACK: return 10;
            case QUEEN: return 10;
            case KING: return 10;
            case ACE: return 11;
            default: return 0;
        }
    }
    //display the number or blackJack value for each card
    public String getDisplayValue() {
        switch (value){
            case TWO: return "2";
            case THREE: return "3";
            case FOUR: return "4";
            case FIVE: return "5";
            case SIX: return "6";
            case SEVEN: return "7";
            case EIGHT: return "8";
            case NINE: return "9";
            case TEN: return "10";
            case JACK: return "J";
            case QUEEN: return "Q";
            case KING: return "K";
            case ACE: return "A";
            default: return "?";
        }
    }
    //suite look for each card
    public String getSuitSymbol(){
        switch (suit) {
            case HEARTS:   return "♥";
            case DIAMONDS: return "♦";
            case CLUBS:    return "♣";
            case SPADES:   return "♠";
            default:       return "?";
        }
    }
    //determine if a card is red
    public boolean isRed(){
        return suit == Suit.HEARTS || suit == Suit.DIAMONDS;
    }
    //determine if card is black
    public boolean isBlack() {
        return suit == Suit.CLUBS || suit == Suit.SPADES;
    }

    @Override
    public String toString(){
        return getDisplayValue() + getSuitSymbol();
    }
}