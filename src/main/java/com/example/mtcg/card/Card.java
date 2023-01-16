package com.example.mtcg.card;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
@Getter
@Setter
public class Card {

    protected String name;
    protected int damage;
    protected String id;
    protected ElementType type;

    protected MonsterType monsterType;

    protected CardType cardtype;

    public Card(String name, int damage, ElementType type ,MonsterType monsterType){

        this.name = name;
        this.damage = damage;
        this.type = type;
        this.monsterType = monsterType;

    }
    public Card(String id,String name, int damage, ElementType type ,MonsterType monsterType){
        this.id = id;
        this.name = name;
        this.damage = damage;
        this.type = type;
        this.monsterType = monsterType;

    }
    @JsonCreator
    public Card(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("damage") int damage){
        this.id = id;
        this.name = name;
        this.damage = damage;

    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return damage == card.damage && name.equals(card.name) && type == card.type && monsterType == card.monsterType && cardtype == card.cardtype;
    }
    public static Card generateCard(){

        int cardTypeIndex = (int) (Math.random() * 2);
        CardType cardtype = CardType.values()[cardTypeIndex];
        if (cardtype == CardType.SPELL){
            ElementType element = ElementType.generateElement();
            String name = element.toString() + " Spell";
            //capitalize first letter
            name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();

            return new SpellCard(name,ElementType.generateDamage(),element);

        }
        else if (cardtype == CardType.MONSTER){
            ElementType element = ElementType.generateElement();
            MonsterType monsterType = MonsterType.generateMonster();
            assert monsterType != null;
            String name = element.toString() + " " + monsterType;
            //capitalize first letter
            name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();



            return new MonsterCard(name, ElementType.generateDamage(), element, monsterType);

        }
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, damage, type, monsterType, cardtype);
    }





}

//parent class of spellcard and monstercard