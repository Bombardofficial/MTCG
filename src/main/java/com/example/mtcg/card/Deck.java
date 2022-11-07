package com.example.mtcg.card;

import java.util.LinkedList;
import java.util.List;

import java.util.Random;

public class Deck {

    private int Cards;

    private List<Card> deck; //interfaceben hozzam be, ne itt dekraráljam explicit módon

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
