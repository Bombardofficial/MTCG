package com.example.mtcg.card;

import java.util.LinkedList;

import java.util.Random;

public class Deck {

    private int Cards;

    private LinkedList<Card> deck;

    Random random = new Random();

    public Deck(){

        Cards = 4;
        deck = new LinkedList<>();

    }

    public Card randomCard(){
        int randomPosition = random.nextInt(this.deck.size());
        Card randomCard = this.deck.get(randomPosition);
        return randomCard;
    }





}
