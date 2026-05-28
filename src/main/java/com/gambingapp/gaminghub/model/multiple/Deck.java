package com.gambingapp.gaminghub.model.multiple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// represents a deck of 52 cards that can be shuffled and drawn from and can have multiple decks
public class Deck{
    private final List<Card> cards;

    //one shuffled deck
    public Deck(){
        this(1);
    }

    //for games that need multiple decks
    public Deck(int numberOfDecks){
        cards = new ArrayList<>();
        for(int i = 0; i < numberOfDecks; i++){
            for(Card.Suit suit: Card.Suit.values()){
                for(Card.Value value: Card.Value.values()){
                    cards.add(new Card(suit,value));
                }
            }
        }
        shuffle();
    }

    //shuffle decks in Fisher-Yates algorithm
    public void shuffle(){
        Collections.shuffle(cards);
    }
    //Draw the top card and remove it
    public Card draw(){
        if(cards.isEmpty()){
            return null;
        }
        return cards.remove(cards.size() -1);
    }
    //Draws a card and sets it face down
    public Card drawFaceDown(){
        Card card = draw();
        if(card != null){
            card.setFaceDown(true);
        }
        return card;
    }
    //Checks how many cards are in the deck
    public int size(){
        return cards.size();
    }
    //Check if deck is empty
    public boolean isEmpty(){
        return cards.isEmpty();
    }
    //Returns all remaining cards
    public List<Card> getCards(){
        return new ArrayList<>(cards);
    }
}