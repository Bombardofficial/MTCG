package com.example.mtcg.battle;
import com.example.mtcg.Battle;
import com.example.mtcg.User;
import com.example.mtcg.card.*;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class BattleTest {
    @Test
    @DisplayName("1. Testing the whole battle between two users")
    public void testFight() {
        User player1 = new User("Player1", "password");

        User player2 = new User("Player2", "password");


        //add cards to the deck
        player1.getDeck().addCards();
        player2.getDeck().addCards();
        Battle battle = new Battle(player1, player2);
        battle.fight();
        assertNotEquals(player1.getDeck().Size(), player2.getDeck().Size());
        System.out.println("Checking player 1's stats: \n");
        System.out.println("Games: "+player1.getGames());
        System.out.println("Wins: "+player1.getWins());
        System.out.println("Losses: "+player1.getLosses());
        System.out.println();
        System.out.println("Checking player 2's stats: \n");
        System.out.println("Games: "+player2.getGames());
        System.out.println("Wins: "+player2.getWins());
        System.out.println("Losses: "+player2.getLosses());
        System.out.println("______________________________");

    }



    @Test
    @DisplayName("2. Testing fight between fire and water normal monster")
    public void testMonsterFight() {

        Card user1Card = new MonsterCard("Fire Monster", 5, ElementType.FIRE, MonsterType.NORMAL);
        Card user2Card = new MonsterCard("Water Monster", 10, ElementType.WATER, MonsterType.NORMAL);
        Battle battle = new Battle(null, null);
        int result = battle.MonsterFight(user1Card, user2Card);
        assertEquals(2, result);
    }
    @Test
    @DisplayName("3. Testing fight between Water Goblin and Water Troll")
    public void testWaterGoblinVsFireTroll() {
        Card goblin = new MonsterCard("Water Goblin",10, ElementType.WATER,MonsterType.GOBLIN);
        Card troll = new MonsterCard("Fire Troll",15, ElementType.FIRE,MonsterType.TROLL);
        Battle battle = new Battle(null, null);
        int winner = battle.MonsterFight(goblin, troll);
        assertEquals(2, winner);
        int winner2 = battle.MonsterFight(troll, goblin);
        assertEquals(1, winner2);

        System.out.println("Fire Troll defeated Water Goblin.");
        System.out.println( "Fire Troll with "+troll.getDamage() + " damage defeated " + " Water Goblin with "+ goblin.getDamage()  +" damage.");


    }
    @Test
    @DisplayName("4. Testing if Water Goblin wins against Water Troll")
    public void testWaterSpellWinVsFireSpell() {
        Card spell1 = new SpellCard("Fire Spell", 10, ElementType.FIRE);
        Card spell2 = new SpellCard("Water Spell", 20, ElementType.WATER);
        Battle battle = new Battle(null, null);
        int winner = battle.SpellFight(spell1, spell2);
        assertEquals(2, winner);
        int winner2 = battle.SpellFight(spell2, spell1);
        assertEquals(1, winner2);

        System.out.println( "Water Spell with "+spell2.getDamage() + " damage defeated " + " Fire Spell with "+ spell1.getDamage()  +" damage.");

    }
    @Test
    @DisplayName("5. Testing if Water Goblin draws against Water Troll")
    public void testWaterSpellDrawVsFireSpell() {
        Card spell1 = new SpellCard("Fire Spell", 20, ElementType.FIRE);
        Card spell2 = new SpellCard("Water Spell", 5, ElementType.WATER);
        Battle battle = new Battle(null, null);
        int winner = battle.SpellFight(spell1, spell2);
        assertEquals(0, winner);
        int winner2 = battle.SpellFight(spell2, spell1);
        assertEquals(0, winner2);
        //not equal
        assertNotEquals(1, winner2); //not equal
        assertNotEquals(1, winner); //not equal

        System.out.println("No spells won.");
        System.out.println("Fire Spell damage: " + spell1.getDamage());
        System.out.println("Water Spell damage: " + spell2.getDamage());



    }
    @Test
    @DisplayName("6. Testing if Water Goblin loses against Water Troll")
    public void testWaterSpellLoseVsFireSpell() {
        Card spell1 = new SpellCard("Fire Spell", 90, ElementType.FIRE);
        Card spell2 = new SpellCard("Water Spell", 5, ElementType.WATER);
        Battle battle = new Battle(null, null);
        int winner = battle.SpellFight(spell1, spell2);
        assertEquals(1, winner);
        int winner2 = battle.SpellFight(spell2, spell1);
        assertNotEquals(1, winner2);
        System.out.println( "Fire Spell with "+spell1.getDamage() + " damage defeated " + " Water Spell with "+ spell2.getDamage()  +" damage.");

    }
    @Test
    @DisplayName("7. Testing if Water Goblin winning against Fire Spell")
    public void testFireSpellLoseVsWaterGoblin() {
        Card spell = new SpellCard("Fire Spell", 10, ElementType.FIRE);
        Card monster = new MonsterCard("Water Goblin",10, ElementType.WATER,MonsterType.GOBLIN);
        Battle battle = new Battle(null, null);
        int winner = battle.SpellFight(spell, monster);
        assertEquals(2, winner);
        int winner2 = battle.SpellFight(monster, spell);
        assertNotEquals(2, winner2);
        System.out.println( "Water Goblin with "+monster.getDamage() + " damage defeated " + " Fire Spell with "+ spell.getDamage()  +" damage.");

    }
    @Test
    @DisplayName("8. Testing if Water Goblin draws against Water Spell")
    public void testWaterSpellDrawVsWaterGoblin() {
        Card spell = new SpellCard("Water Spell", 10, ElementType.WATER);
        Card monster = new MonsterCard("Water Goblin",10, ElementType.WATER,MonsterType.GOBLIN);
        Battle battle = new Battle(null, null);
        int winner = battle.SpellFight(spell, monster);
        assertEquals(0, winner);
        int winner2 = battle.SpellFight(monster, spell);
        assertNotEquals(2, winner2);
        assertNotEquals(1, winner2);
        System.out.println("No spells won.");
        System.out.println("Water Spell damage: " + spell.getDamage());
        System.out.println("Water Goblin damage: " + monster.getDamage());


    }

    @Test
    @DisplayName("9. Testing if Regular spell winning against Water Goblin")
    public void testRegularSpellWinVsWaterGoblin() {
        Card spell = new SpellCard("Regular Spell", 10, ElementType.NORMAL);
        Card monster = new MonsterCard("Water Goblin",10, ElementType.WATER,MonsterType.GOBLIN);
        Battle battle = new Battle(null, null);
        int winner = battle.SpellFight(spell, monster);
        assertEquals(1, winner);
        int winner2 = battle.SpellFight(monster, spell);
        assertNotEquals(1, winner2); /////////////////////////////////////ne ezt hanem assertequals 2
        System.out.println( "Regular Spell with "+spell.getDamage() + " damage defeated " + " Water Goblin with "+ monster.getDamage()  +" damage.");

    }

    @Test
    @DisplayName("10. Testing if Regular spell loses against Knight")
    public void testRegularSpellLoseVsWaterGoblin() {
        Card spell = new SpellCard("Regular Spell", 10, ElementType.NORMAL);
        Card monster = new MonsterCard("Knight",15, ElementType.NORMAL,MonsterType.KNIGHT);
        Battle battle = new Battle(null, null);
        int winner = battle.SpellFight(spell, monster);
        assertEquals(2, winner);
        int winner2 = battle.SpellFight(monster, spell);
        assertNotEquals(2, winner2);
        System.out.println( "Knight with "+monster.getDamage() + " damage defeated " + " Regular Spell with "+ spell.getDamage()  +" damage.");

    }

    @Test
    @DisplayName("11. Testing taking monster cards between users")
    public void testTakeCard() {
        Card user1Card = new MonsterCard("Fire Monster", 5, ElementType.FIRE, MonsterType.NORMAL);
        Card user2Card = new MonsterCard("Water Monster", 10, ElementType.WATER, MonsterType.NORMAL);
        User player1 = new User(0,"admin", "istrator");
        User player2 = new User(1,"kienboec", "daniel");
        player1.getDeck().add(user1Card);
        player2.getDeck().add(user2Card);
        Battle battle = new Battle(player1, player2);
        battle.takeCard(1, user1Card, user2Card);
        assertEquals(2, player1.getDeck().Size());
        assertEquals(0, player2.getDeck().Size());
    }

}