package com.example.mtcg.market;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Trading {
    private int id;
    private String cardtotrade;
    private int minimumdamage;

    public Trading(int id, String cardtotrade, int minimumdamage) {
        this.id = id;
        this.cardtotrade = cardtotrade;
        this.minimumdamage = minimumdamage;
    }

    public int getId() {
        return id;
    }

    public String getCardtotrade() {
        return cardtotrade;
    }

    public int getMinimumdamage() {
        return minimumdamage;
    }
}