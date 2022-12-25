package com.example.mtcg;

import com.example.mtcg.card.Card;

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

    T createPackage(Card data)  throws SQLException;

    T getCards(int id) throws SQLException;

    T getDeck(int id) throws SQLException;


    //T createPackage(int id) throws SQLException;
}
