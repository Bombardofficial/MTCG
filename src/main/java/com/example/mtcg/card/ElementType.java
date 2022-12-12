package com.example.mtcg.card;

import java.util.Random;

public enum ElementType {

    FIRE,WATER,NORMAL;


    static final Random random = new Random();


    public static ElementType generateElement() {


        int randomType = random.nextInt(3);

        switch(randomType){

            case 0: return ElementType.FIRE;


            case 1: return ElementType.WATER;


            case 2: return ElementType.NORMAL;

        }

        return ElementType.NORMAL;

    }

    public static int generateDamage(){


        return random.nextInt(90)+1;

    }
}