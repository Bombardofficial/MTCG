package com.example.mtcg.card;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
public class DeckTest {

    @Test
    @DisplayName("1. Testing the filling of the deck")
    public void testAddCards() {
        Deck deck = new Deck();
        deck.addCards();
        assertEquals(4, deck.Size());
        System.out.println("Filled deck with 4 cards");
        System.out.println("Deck size: "+deck.Size());
        System.out.println("Deck: ");
        for (int i = 0; i < deck.Size(); i++) {
            System.out.println(i+1 + ". ");
            System.out.println("Name: "+deck.getDeck().get(i).getName());
            System.out.println("Damage: "+deck.getDeck().get(i).getDamage());
            System.out.println("Type: "+deck.getDeck().get(i).getType());
            if(deck.getDeck().get(i) instanceof MonsterCard){
                System.out.println("Monster type: "+deck.getDeck().get(i).getMonsterType());
            }
            System.out.println();
        }
        System.out.println("______________________________");



    }

    @Test
    @DisplayName("2. Testing the Removing of the first card")
    public void testRemoveFirst() {
        Deck deck = new Deck();
        deck.addCards();
        deck.removeFirst();
        assertEquals(3, deck.Size());
        System.out.println("Removed first card from deck");
        System.out.println("Deck size: "+deck.Size());
        System.out.println("______________________________");
    }

    @Test
    @DisplayName("3. Testing the Adding of a card")
    public void testAdd() {
        Deck deck = new Deck();
        Card card = Card.generateCard();
        deck.add(card);
        assertEquals(1, deck.Size());
        System.out.println("Added card to deck");
        System.out.println("Deck size: "+deck.Size());
        System.out.println("______________________________");
    }

    @Test
    @DisplayName("4. Testing the Picking of a card")
    public void testRandomCard() {
        Deck deck = new Deck();
        deck.addCards();
        Card card = deck.randomCard();
        assertNotNull(card);
        System.out.println("Random card from deck: \n");
        System.out.println("Card name: "+card.getName());
        System.out.println("Card damage: "+card.getDamage());
        System.out.println("Card type: "+card.getType());
        System.out.println("______________________________");

    }

}