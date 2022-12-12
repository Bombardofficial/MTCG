package com.example.mtcg;

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
}
