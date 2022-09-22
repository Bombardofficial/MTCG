package com.example.mtcg.card;

public class Card {

    protected String name;

    protected int damage;

    protected ElementType type;

    protected MonsterType monsterType;

    public Card(String name, int damage, ElementType type ,MonsterType monsterType){

        this.name = name;
        this.damage = damage;
        this.type = type;
        this.monsterType = monsterType;

    }


}

//parent class of spellcard and monstercard