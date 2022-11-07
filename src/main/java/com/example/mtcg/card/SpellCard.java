package com.example.mtcg.card;

public class SpellCard extends Card{

    public SpellCard(String name, int damage, ElementType type) {
        super(name, damage, type, MonsterType.SPELL);
        this.cardtype = CardType.SPELL;
    }
}
