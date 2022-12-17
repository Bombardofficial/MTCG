package com.example.mtcg;

import com.example.mtcg.card.Card;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RestUser implements Rest<User> {

    private final PreparedStatement put;
    private final PreparedStatement deleting;
    private final PreparedStatement getById;
    private Connection conn;
    private final PreparedStatement getAll;
    private final PreparedStatement post;
    private final PreparedStatement updateDeck;

    private final PreparedStatement uploadCards;
    private final PreparedStatement checkUser;
    private final PreparedStatement createCardforPackage;
    private final PreparedStatement sendCardstoPackage;
    public RestUser(Connection conn) throws SQLException {
        this.conn = conn;
        this.getAll = conn.prepareStatement("SELECT * FROM users");
        this.getById = conn.prepareStatement("SELECT * FROM users WHERE id = ?");
        this.post = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
        this.put = conn.prepareStatement("UPDATE users SET username = ?, password = ? WHERE id = ?");
        this.deleting = conn.prepareStatement("DELETE FROM users WHERE id = ?");
        this.updateDeck = conn.prepareStatement("UPDATE cards SET in_deck = false WHERE user_id = ?");
        this.uploadCards = conn.prepareStatement("INSERT INTO cards (user_id, in_deck, name, damage, element, type) VALUES (?, ?, ?, ?, ?, ?)");
        this.checkUser = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
        this.createCardforPackage = conn.prepareStatement("INSERT INTO cards (idx, name, damage) VALUES (?, ?, ?)");
        this.sendCardstoPackage = conn.prepareStatement("INSERT INTO packages (user_id, card_id) VALUES (?, ?)");
    }

    @Override
    public List<User> getAll() throws SQLException {
        ResultSet rs = getAll.executeQuery();
        return makeList(rs);
    }

    private List<User> makeList(ResultSet rs) throws SQLException {
        List<User> users = new ArrayList<>();
        while (rs.next()){
            User user = makeOne(rs);
            users.add(user);
        }
        return users;
    }

    private User makeOne(ResultSet rs) throws SQLException {

        int id = rs.getInt("id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        return new User(id, username, password);
    }

    @Override
    public User post(User data) throws SQLException {
        post.setString(1, data.getUsername());
        post.setString(2, data.getPassword());
        post.executeUpdate();
        return data;
    }

    @Override
    public User put(User data) {
        return null;
    }

    @Override
    public User get(int id) throws SQLException {
        this.getById.setInt(1, id);
        ResultSet rs = this.getById.executeQuery();
        rs.next();
        return makeOne(rs);
    }

    @Override
    public void delete(int id) {

    }

    @Override
    public User deleting(int id) throws SQLException {
        User deletedUser = get(id);
        this.deleting.setInt(1, id);
        this.deleting.executeUpdate();
        return deletedUser;
    }

    @Override
    public User getById(int i) throws SQLException {
        return null;
    }

    @Override
    public User generateCard(int id) throws SQLException {
        User user = get(id);
        this.updateDeck.setInt(1, id);
        this.updateDeck.executeUpdate();

        for(int i = 0; i < 4; i++) {
            Card card = Card.generateCard();
            this.uploadCards.setInt(1, id);
            this.uploadCards.setBoolean(2, true);
            assert card != null;
            this.uploadCards.setString(3, card.getName());
            this.uploadCards.setInt(4, card.getDamage());
            this.uploadCards.setString(5, card.getType().toString());
            this.uploadCards.setString(6, card.getCardtype().toString());

            this.uploadCards.executeUpdate();

        }
        return user;
    }

    @Override
    public User login(String username, String password) throws SQLException {
        this.checkUser.setString(1, username);
        ResultSet rs = this.checkUser.executeQuery();
        if(rs.next()) {
            String hashedPassword = rs.getString("password");
            if(BCrypt.checkpw(password, hashedPassword)) {
                System.out.println("Login successful!");
                return makeOne(rs);
            }
            else {
                System.out.println("Wrong password!");
                return null;
            }

        }

        return null;
    }
/*
    @Override
    public User createPackage(int id) throws SQLException {
        User user = get(id);

        for(int i = 0; i < 5; i++) {
            Card card = get(id);
            this.createCardforPackage.setInt(1, id);
            this.createCardforPackage.setString(2, card.getName());
            this.createCardforPackage.setInt(3, card.getDamage());
            this.createCardforPackage.executeUpdate();

        }
        //set values for package


        return null;
    }
*/
    public boolean checkUser(User user) {
        try {
            this.checkUser.setString(1, user.getUsername());
            ResultSet rs = this.checkUser.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


}

