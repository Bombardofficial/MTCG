package com.example.mtcg.card;

import lombok.Getter;
import lombok.Setter;
import java.util.LinkedList;
import java.util.List;

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
