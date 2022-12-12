package com.example.mtcg;

import com.example.mtcg.card.Deck;
import com.example.mtcg.card.Stack;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Data;

@Data
@Getter
public class User {

    private long id;
    private String username;
    private String password;
    private int coins; //default: 20 coins
    @JsonIgnore
    private final Deck deck; //collection of chosen cards for the game
    @JsonIgnore
    private final Stack stack; //collection of current cards
    private int elo;
    /*
     * Card game elo system:

       Expected score: predict the outcome of a game.
       Actual score: observe the outcome.
       Update: increase or decrease each player’s rating based on result.

       Expected score: Ea (expected score of Player A) = 1 / (1 + 10^((Rb - Ra)/D)
       Ra = Player A's elo rating
       Rb = PLayer B's elo rating
       D`s default value: 400
       K`s default value: 32
       Elo starting value: 2000

       Actual score: Sa (1 or 0 depends on winning(1) or losing(0))

       Update: R'A = Ra + K (Sa - Ea)

     */
    private int wins;
    private int losses;
    private int games;

    public User(int id, String username, String password){
        this.id = id;
        this.username = username;
        this.password = password;
        coins = 20;
        deck = new Deck();
        stack = new Stack();
        elo = 2000;
        wins = 0;
        losses = 0;
        games = 0;
    }

    public User(String username, String password) {
        this(0, username, password);
    }

    public User() {
        this(0, "", "");
    }

    public int getElo() {
        return elo;
    }
    public void eloCalculator(int opponent_elo, double score) {
        double expectedScore =  1.0 / (1.0 + Math.pow(10.0, ((double) (opponent_elo - this.elo) / 400.0)));
        this.elo = this.elo + (int) (32 * (score - expectedScore));
    }

    public void win(int opponent_elo) {
        this.wins++;
        this.games++;
        eloCalculator(opponent_elo , 1);

    }

    public void lose(int opponent_elo) {
        this.losses++;
        this.games++;
        eloCalculator(opponent_elo , 0);
    }

    public void draw(int opponent_elo) {
        this.games++;
        eloCalculator(opponent_elo , 0.5);
    }


    public void pays(int coins) {
        this.coins -= coins;
    }
}
