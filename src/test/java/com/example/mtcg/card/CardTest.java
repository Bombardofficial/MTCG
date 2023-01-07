package com.example.mtcg.card;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CardTest {

    @Test
    @DisplayName("1. Testing the constructor of the Card class")
    public void testConstructor() {
        String name = "Fire Monster";
        int damage = 10;
        ElementType type = ElementType.FIRE;
        MonsterType monsterType = MonsterType.NORMAL;
        Card card = new Card(name, damage, type, monsterType);
        assertEquals(name, card.getName());
        assertEquals(damage, card.getDamage());
        assertEquals(type, card.getType());
        assertEquals(monsterType, card.getMonsterType());
    }

    @Test
    @DisplayName("2. Testing the JSON constructor of the Card class")
    public void testJsonCreatorConstructor() {
        String id = "123";
        String name = "Water Spell";
        int damage = 5;
        Card card = new Card(id, name, damage);
        assertEquals(id, card.getId());
        assertEquals(name, card.getName());
        assertEquals(damage, card.getDamage());
    }

    @Test
    @DisplayName("3. Testing if the cards are equal")
    public void testEquals() {
        String name = "Water Monster";
        int damage = 15;
        ElementType type = ElementType.WATER;
        MonsterType monsterType = MonsterType.NORMAL;
        Card card1 = new Card(name, damage, type, monsterType);
        Card card2 = new Card(name, damage, type, monsterType);
        assertEquals(card1, card2);
        card2.setDamage(20);
        assertNotEquals(card1, card2);
    }

    @RepeatedTest(5)
    @DisplayName("4. Testing the card generator")
    public void testGenerateCard() {
        Card card = Card.generateCard();
        assert card != null;
        System.out.println("Card name: "+card.getName());
        System.out.println("Card damage: "+card.getDamage());
        if(CardType.MONSTER.equals(card.getCardtype())) {
            System.out.println("Card type: "+card.getType());
            System.out.println("Card monster type: "+card.getMonsterType());
        }
        else {
            System.out.println("Card spell type: "+card.getType());
        }
        System.out.println("______________________________");


        assertNotNull(card);
    }

}