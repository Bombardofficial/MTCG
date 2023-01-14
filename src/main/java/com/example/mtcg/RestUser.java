package com.example.mtcg;

import com.example.mtcg.card.Card;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
public class RestUser implements Rest<User> {

    private final PreparedStatement put;
    private final PreparedStatement deleting;
    private final PreparedStatement getById;
    private final PreparedStatement createPackage;
    private Connection conn;
    private final PreparedStatement getAll;
    private final PreparedStatement post;
    private final PreparedStatement updateDeck;

    private final PreparedStatement uploadCards;
    private final PreparedStatement checkUser;
    private final PreparedStatement createCardforPackage;

    private final PreparedStatement buyPackage;
    private final PreparedStatement getCards;
    private final PreparedStatement getDeck;
    private final PreparedStatement selectUser;

    private final PreparedStatement setCoins;
    private final PreparedStatement getCoins;
    private final PreparedStatement configureDeckbyUsername;

    public RestUser(Connection conn) throws SQLException {
        this.conn = conn;
        this.getAll = conn.prepareStatement("SELECT * FROM users");
        this.getById = conn.prepareStatement("SELECT * FROM users WHERE id = ?");
        this.post = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
        this.put = conn.prepareStatement("UPDATE users SET username = ?, password = ? WHERE id = ?");
        this.deleting = conn.prepareStatement("DELETE FROM users WHERE id = ?");
        this.updateDeck = conn.prepareStatement("UPDATE cards SET in_deck = false WHERE user_id = ?");
        this.uploadCards = conn.prepareStatement("INSERT INTO cards (id,user_id, in_deck, name, damage, element, type) VALUES (?,?, ?, ?, ?, ?, ?)");
        this.checkUser = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
        this.createCardforPackage = conn.prepareStatement("INSERT INTO cards (id, name, damage, package_id) VALUES (?, ?, ?, ?)");

        this.getCards = conn.prepareStatement("SELECT * FROM cards WHERE user_id = ?");
        this.getDeck = conn.prepareStatement("SELECT * FROM cards WHERE user_id = ? AND in_deck = true");
        this.createPackage = conn.prepareStatement("INSERT INTO package(id) VALUES(DEFAULT) RETURNING id", PreparedStatement.RETURN_GENERATED_KEYS);
        this.buyPackage = conn.prepareStatement("UPDATE cards SET user_id = ? WHERE package_id = (SELECT MIN(package_id) FROM cards)\n");
        this.selectUser = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
        this.setCoins = conn.prepareStatement("UPDATE users SET coins = ? WHERE username = ?");
        this.getCoins = conn.prepareStatement("SELECT coins FROM users WHERE username = ?");

        this.configureDeckbyUsername = conn.prepareStatement("WITH user_id AS ( SELECT id FROM users WHERE username = ? )UPDATE cards SET in_deck = true WHERE id IN ? AND user_id = (SELECT id FROM user_id);");
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

    private List<Card> makeCardList(ResultSet rs) throws SQLException {
        List<Card> cards = new ArrayList<>();
        while (rs.next()){
            Card card = makeOneCard(rs);
            cards.add(card);
        }
        return cards;
    }

    private Card makeOneCard(ResultSet rs) throws SQLException {

        String id = rs.getString("id");
        //String username = rs.getString("username");
        //String password = rs.getString("password");
        // TODO
        return new Card(id, "", 0);
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
            String test = "test";
            this.uploadCards.setString(1, test+id);
            this.uploadCards.setInt(2, id);
            this.uploadCards.setBoolean(3, true);
            assert card != null;
            this.uploadCards.setString(4, card.getName());
            this.uploadCards.setInt(5, card.getDamage());
            this.uploadCards.setString(6, card.getType().toString());
            this.uploadCards.setString(7, card.getCardtype().toString());

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


    @Override
    public void createPackage(List<Card> cards) throws SQLException {
        this.createPackage.executeUpdate();
        ResultSet rs = this.createPackage.getGeneratedKeys();
        if (rs.next()) {
            int packageId = rs.getInt(1);
            for (Card card : cards) {
                this.createCardforPackage.setString(1, card.getId());
                this.createCardforPackage.setString(2, card.getName());
                this.createCardforPackage.setInt(3, card.getDamage());
                this.createCardforPackage.setInt(4, packageId);
                this.createCardforPackage.executeUpdate();
            }
        }
    }

    @Override
    public List<Card> getCards(int id) throws SQLException {
        //get all cards from user
        this.getCards.setInt(1, id);
        ResultSet rs = this.getCards.executeQuery();
        return makeCardList(rs);

    }

    @Override
    public List<Card> getDeck(int id) throws SQLException {
        //get all cards from user
        this.getDeck.setInt(1, id);
        ResultSet rs = this.getDeck.executeQuery();
        return makeCardList(rs);

    }

    @Override
    public void configureDeck(List<String> cardIds, String username) throws SQLException {
        //check if cards are already in the deck
        String checkCardsInDeckSql = "SELECT id FROM cards WHERE id IN (? " + String.join(",?", Collections.nCopies(cardIds.size()-1,"?")) + ") AND user_id = ? AND in_deck = true";
        PreparedStatement checkCardsInDeckStmt = conn.prepareStatement(checkCardsInDeckSql);
        for (int i = 0; i < cardIds.size(); i++) {
            checkCardsInDeckStmt.setString(i+1, cardIds.get(i));
        }
        //getting id of the user
        this.selectUser.setString(1,username);
        ResultSet rs = selectUser.executeQuery();
        int userId = 0;
        while (rs.next()) {
            userId = rs.getInt("id");
        }

        checkCardsInDeckStmt.setInt(cardIds.size()+1, userId);
        ResultSet rs2 = checkCardsInDeckStmt.executeQuery();
        //putting the number of cards to a list that are in the deck and are the same as the provided ids in the curl script
        List<String> cardsInDeck = new ArrayList<>();
        while (rs2.next()) {
            cardsInDeck.add(rs2.getString("id"));
        }
        if(cardsInDeck.size()>0) {
            System.out.println("The following cards are already in the deck: " + cardsInDeck);
        }
        else {
            //Add cards to deck
            for (String cardId : cardIds) {
                this.configureDeckbyUsername.setString(1,username);
                this.configureDeckbyUsername.setString(2, cardId);
            }
        }
    }

    @Override
    public void buyPackage(User authUser) throws SQLException {
        selectUser.setString(1, authUser.getUsername());
        ResultSet rs = selectUser.executeQuery();
        int coins = 0;
        while (rs.next()) {
            coins = rs.getInt("coins");
            try {
                coins -= 5;
                authUser.setCoins(coins);
                setCoins.setInt(1, coins);
                setCoins.setString(2, authUser.getUsername());
                setCoins.executeUpdate();
                this.buyPackage.setInt(1,rs.getInt("id"));
                this.buyPackage.executeUpdate();


            } catch (SQLException e) {
                e.printStackTrace();
            }
        }



        //rs.close();
        //selectUser.close();

        //setCoins.close();




    }



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

