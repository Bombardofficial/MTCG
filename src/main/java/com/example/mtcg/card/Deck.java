package com.example.mtcg.card;

import lombok.Getter;

import java.util.LinkedList;

import java.util.Random;
@Getter
public class Deck {

    private final int Cards;

    private final LinkedList<Card> deck; //interfaceben hozzam be, ne itt dekraráljam explicit módon

    Random random = new Random();

    public Deck(){

        Cards = 4;
        deck = new LinkedList<>();

    }

    public Card randomCard(){
        int randomPosition = random.nextInt(this.deck.size());
        return this.deck.get(randomPosition);
    }

    public void addCards(){
        int numCards = 4;
        if (deck.size() < numCards) {
            // Generate a new card inside the loop
            for (int i = 0; i < numCards; i++) {
                Card card = Card.generateCard();
                deck.add(card);
            }
        }
    }
    public int Size() {
        return this.deck.size();
    }

    public void removeFirst() {
        this.deck.removeFirst();

    }

    public void add(Card c1) {
        this.deck.add(c1);
    }
}
