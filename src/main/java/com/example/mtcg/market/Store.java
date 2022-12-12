package com.example.mtcg.market;

import com.example.mtcg.User;
import com.example.mtcg.card.Card;
//service point for trading
public class Store {



    public void buyPackage(User user) {
        user.pays(5);
        // generálunk egy package-et
        // odaadjuk a user-nek
        // user boldog. :-)
    }
}
