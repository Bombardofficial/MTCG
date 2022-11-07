package com.example.mtcg.card;

import lombok.Getter;

import java.util.Objects;
@Getter
public class Card {

    protected String name;

    protected int damage;

    protected ElementType type;

    protected MonsterType monsterType;

    protected CardType cardtype;

    public Card(String name, int damage, ElementType type ,MonsterType monsterType){

        this.name = name;
        this.damage = damage;
        this.type = type;
        this.monsterType = monsterType;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return damage == card.damage && name.equals(card.name) && type == card.type && monsterType == card.monsterType && cardtype == card.cardtype;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, damage, type, monsterType, cardtype);
    }
}

//parent class of spellcard and monstercard