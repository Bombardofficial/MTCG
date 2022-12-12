package com.example.mtcg;
import com.example.mtcg.card.*;

public class Battle {
    private final User player1;
    private final User player2;


    public Battle(User user1,User user2){

        this.player1 = user1;
        this.player2 = user2;

    }

    public void fight(){

        int round;

        Card user1Card;
        Card user2Card;

        int remainingRounds = 100;
        for(int i = 0; i< remainingRounds; i++){

            System.out.println("Rounds "+ remainingRounds +":");

            //Deck von Spieler 1 leer
            if(player1.getDeck().Size()==0) {

                System.out.println(player2.getUsername()+" has won.");
                player2.win(player1.getElo());
                player1.lose(player2.getElo());
                break;
            }
            //Deck von Spieler 2 leer
            if(player2.getDeck().Size()==0) {

                System.out.println(player1.getUsername()+" has won.");
                player1.win(player2.getElo());
                player2.lose(player1.getElo());
                break;

            }

            user1Card = player1.getDeck().randomCard();
            user2Card = player2.getDeck().randomCard();

            System.out.println( user1Card.getName()+" von "+ player1.getUsername()+" zieht in den Kampf.");
            System.out.println( user2Card.getName()+" von "+ player2.getUsername()+" zieht in den Kampf.");

            //Vergleichen von Karten
            //Monsterkampf
            if(user1Card instanceof MonsterCard && user2Card instanceof MonsterCard){

                round = MonsterFight(user1Card,user2Card);

            }
            else{

                round = SpellFight(user1Card,user2Card);

            }

            switch(round){

                case 0: System.out.println("Die Runde verlief unentschieden.");

                case 1: System.out.println(player1.getUsername()+" gewinnt die Runde. "+user1Card.getName()+" besiegte "+user2Card.getName());

                case 2: System.out.println(player2.getUsername()+" gewinnt die Runde. "+user2Card.getName()+" besiegte "+user1Card.getName());

            }

            takeCard(round,user1Card,user2Card);

        }

        if(player1.getDeck().Size()!=0 && player2.getDeck().Size()!=0){

            System.out.println("Keiner der beiden Spieler hat gewonnen! Unentschieden!");
            player1.draw(player2.getElo());
            player2.draw(player1.getElo());

        }

    }

    private void takeCard(int result, Card user1Card, Card user2Card){

        player1.getDeck().removeFirst();

        player2.getDeck().removeFirst();

        switch(result){

            case 0:{

                System.out.println("Keine Karten wurden übernommen.");
                player1.getDeck().add(user1Card);
                player2.getDeck().add(user2Card);
                break;

            }

            case 1:{

                System.out.println(player1.getUsername()+" übernahm "+user2Card.getName());
                player1.getDeck().add(user1Card);
                player1.getDeck().add(user2Card);
                break;

            }

            case 2:{

                System.out.println(player2.getUsername()+" übernahm "+user1Card.getName());
                player2.getDeck().add(user2Card);
                player2.getDeck().add(user1Card);
                break;

            }

        }

    }

    public int MonsterFight(Card user1Card, Card user2Card){

        int damage1= user1Card.getDamage();
        int damage2= user2Card.getDamage();

        if(user1Card.getMonsterType().equals(MonsterType.GOBLIN) && user2Card.getMonsterType().equals(MonsterType.DRAGON)){

            return 2;

        }
        else if (user1Card.getMonsterType().equals(MonsterType.DRAGON) && user2Card.getMonsterType().equals(MonsterType.GOBLIN)){


            return 1;

        }
        else if(user1Card.getMonsterType().equals(MonsterType.WIZARD) && user2Card.getMonsterType().equals(MonsterType.ORK)){

            return 1;

        }
        else if(user1Card.getMonsterType().equals(MonsterType.ORK) && user2Card.getMonsterType().equals(MonsterType.WIZARD)){

            return 2;

        }
        else if(user1Card.getMonsterType().equals(MonsterType.ELVE) && user1Card.getType().equals(ElementType.FIRE) && user2Card.getMonsterType().equals(MonsterType.DRAGON)){

            return 1;

        }
        else if(user1Card.getMonsterType().equals(MonsterType.DRAGON) && user2Card.getType().equals(ElementType.FIRE) && user2Card.getMonsterType().equals(MonsterType.ELVE)){

            return 2;

        }
        else if(damage1==damage2){

            return 0;

        }
        else if (damage1>damage2){

            return 1;

        }
        else {

            return 2;

        }

    }

    public int SpellFight(Card user1Card, Card user2Card){

        if(user1Card.getMonsterType().equals(MonsterType.KNIGHT) && user2Card.getType().equals(ElementType.WATER)){

            return 2;

        }

        if(user2Card.getMonsterType().equals(MonsterType.KNIGHT) && user1Card.getType().equals(ElementType.WATER)){

            return 1;

        }

        if(user1Card.getMonsterType().equals(MonsterType.KRAKEN)){

            return 1;

        }

        if(user2Card.getMonsterType().equals(MonsterType.KRAKEN)){

            return 1;

        }

        if(user1Card.getType().equals(ElementType.WATER) && user2Card.getType().equals(ElementType.FIRE)){

            if(user1Card.getDamage() * 2 > user2Card.getDamage() / 2){

                return 1;

            }
            else if (user1Card.getDamage() * 2 < user2Card.getDamage() / 2){

                return 2;

            }
            else {

                return 0;

            }

        }

        if(user1Card.getType().equals(ElementType.FIRE) && user2Card.getType().equals(ElementType.WATER)){

            if(user1Card.getDamage() / 2 > user2Card.getDamage() * 2){

                return 1;

            }
            else if (user1Card.getDamage() / 2 < user2Card.getDamage() * 2){

                return 2;

            }
            else {

                return 0;

            }

        }

        if(user1Card.getType().equals(ElementType.FIRE) && user2Card.getType().equals(ElementType.NORMAL)){

            if(user1Card.getDamage() * 2 > user2Card.getDamage() / 2){

                return 1;

            }
            else if (user1Card.getDamage() * 2 < user2Card.getDamage() / 2){

                return 2;

            }
            else {

                return 0;

            }

        }

        if(user1Card.getType().equals(ElementType.NORMAL) && user2Card.getType().equals(ElementType.FIRE)){

            if(user1Card.getDamage() / 2 > user2Card.getDamage() * 2){

                return 1;

            }
            else if (user1Card.getDamage() / 2 < user2Card.getDamage() * 2){

                return 2;

            }
            else {

                return 0;

            }

        }

        if(user1Card.getType().equals(ElementType.NORMAL) && user2Card.getType().equals(ElementType.WATER)){

            if(user1Card.getDamage() * 2 > user2Card.getDamage() / 2){

                return 1;

            }
            else if (user1Card.getDamage() * 2 < user2Card.getDamage() / 2){

                return 2;

            }
            else {

                return 0;

            }

        }

        if(user1Card.getType().equals(ElementType.WATER) && user2Card.getType().equals(ElementType.NORMAL)){

            if(user1Card.getDamage() / 2 > user2Card.getDamage() * 2){

                return 1;

            }
            else if (user1Card.getDamage() / 2 < user2Card.getDamage() * 2){

                return 2;

            }
            else {

                return 0;

            }

        }

        return 0;

    }

}
