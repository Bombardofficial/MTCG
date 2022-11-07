package com.example.mtcg.card;

import java.util.Random;

public enum MonsterType {

    GOBLIN, DRAGON, WIZARD, ORK, KNIGHT, KRAKEN, ELVE, TROLL, NORMAL, SPELL;

    static Random random = new Random();

    public static MonsterType generateMonster(){

        int randomMonster = random.nextInt(8);

        switch(randomMonster){

            case 0: return MonsterType.GOBLIN;

            case 1: return MonsterType.DRAGON;

            case 2: return MonsterType.WIZARD;

            case 3: return MonsterType.ORK;

            case 4: return MonsterType.KNIGHT;

            case 5: return MonsterType.KRAKEN;

            case 6: return MonsterType.ELVE;

            case 7: return MonsterType.TROLL;

        }

        return null;
    }

}

