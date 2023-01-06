package com.example.mtcg.user;

import com.example.mtcg.User;
import org.junit.Test;
import static org.junit.Assert.*;

public class UserTest {
    @Test
    public void testWin() {
        User user = new User("user", "password");
        int opponentElo = 1500;
        user.win(opponentElo);
        assertEquals(1, user.getWins());
        assertEquals(1, user.getGames());
        // Check that elo rating has increased
        int expectedElo = user.getElo();
        assertTrue(expectedElo > 2000);
        System.out.println("Elo rating has increased from 2000 to " + expectedElo);
    }

    @Test
    public void testLose() {
        User user = new User("user", "password");
        int opponentElo = 2500;
        user.lose(opponentElo);
        assertEquals(1, user.getLosses());
        assertEquals(1, user.getGames());
        // Check that elo rating has decreased
        int expectedElo = user.getElo();
        assertTrue(expectedElo < 2000);
        System.out.println("Elo rating has decreased from 2000 to " + expectedElo);
    }

    @Test
    public void testDraw() {
        User user = new User("user", "password");
        int opponentElo = 2000;
        user.draw(opponentElo);
        assertEquals(1, user.getGames());
        // Check that elo rating has not changed
        int expectedElo = user.getElo();
        assertEquals(2000, expectedElo);
        System.out.println("Elo rating has not changed");
    }

    @Test
    public void testWinAgainstAProPlayer(){
        User user = new User("user", "password");
        user.setElo(1679);
        int opponentElo = 2634;
        user.win(opponentElo);
        assertEquals(1, user.getWins());
        assertEquals(1, user.getGames());
        // Check that elo rating has decreased
        int expectedElo = user.getElo();
        assertTrue(expectedElo > 1679);
        System.out.println("Elo rating has decreased from 1679 to " + expectedElo);
    }

    /*@Test
    public void testPay() {
        User user = new User("user", "password");
        int coins = 10;
        user.pay(coins);
        assertEquals(10, user.getCoins());
    }*/
}
