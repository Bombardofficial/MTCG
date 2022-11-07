package com.example.mtcg.card;

public class MonsterCard extends Card {


    public MonsterCard(String name, int damage, ElementType type, MonsterType monsterType) {
        super(name, damage, type, monsterType);
        this.cardtype = CardType.MONSTER;
    }
}