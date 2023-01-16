package com.example.mtcg;

import com.example.mtcg.card.Card;
import com.example.mtcg.market.Trading;

import java.sql.SQLException;
import java.util.List;

public interface Rest<T> {

    List<T> getAll() throws SQLException; // GET
    T post(T data) throws SQLException; // POST
    T put(T data); // PUT
    T get(int id) throws SQLException; // GET with id
    void delete(int id); // DELETE

    T deleting(int i) throws SQLException;
    T getById(int i) throws SQLException;
    T generateCard(int id) throws SQLException;
    T login(String username, String password) throws SQLException;


    void createPackage(List<Card> cards) throws SQLException;

    List<Card> getCards(int id) throws SQLException;

    List<Card> getDeck(int id) throws SQLException;
    void configureDeck(List<String> cardIds, String username) throws SQLException;

    void buyPackage(T authUser) throws SQLException;

    void editUser(List<String> userDatas, T authUser) throws SQLException;
    void showScoreboard() throws SQLException;
    String getTradings();

    void postTradings(List<Trading> tradeElements, int id) throws SQLException;

    void deleteTradings(String tradeIdStr) throws SQLException;

    void getDeckAsPlain(int id) throws SQLException;


    //T createPackage(int id) throws SQLException;
}
