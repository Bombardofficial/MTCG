package com.example.mtcg;

import com.example.mtcg.card.Card;
import com.example.mtcg.market.Trading;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static java.lang.System.out;

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
    private final PreparedStatement editUser;
    private final PreparedStatement getTradings;
    private final PreparedStatement postTradings;
    private final PreparedStatement deleteTradings;
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
        this.buyPackage = conn.prepareStatement("UPDATE cards SET user_id = ? WHERE package_id = (SELECT MIN(package_id)FROM cards WHERE user_id IS NULL)\n");
        this.selectUser = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
        this.setCoins = conn.prepareStatement("UPDATE users SET coins = ? WHERE username = ?");
        this.getCoins = conn.prepareStatement("SELECT coins FROM users WHERE username = ?");
        this.getTradings = conn.prepareStatement("SELECT * FROM tradings");
        this.postTradings = conn.prepareStatement("INSERT INTO tradings (id, cardtotrade, type, minimumdamage, user_id) VALUES (?,?,?,?,?)");
        this.deleteTradings = conn.prepareStatement("DELETE * FROM tradings WHERE id = ?");
        this.editUser = conn.prepareStatement("UPDATE users SET name = ?, bio = ?, image = ? WHERE username = ?");
        this.configureDeckbyUsername = conn.prepareStatement("UPDATE cards SET in_deck = true WHERE id = ?");
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
                out.println("Login successful!");
                return makeOne(rs);
            }
            else {
                out.println("Wrong password!");
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
    public void getDeckAsPlain(int id) throws SQLException {

        this.getDeck.setInt(1, id);
        ResultSet rs = getDeck.executeQuery();
        while (rs.next()) {
            String cardId = rs.getString("id");
            String name = rs.getString("name");
            int damage = rs.getInt("damage");
            String type = rs.getString("type");
            String elementType = rs.getString("element");
            out.println("ID: " + cardId);
            out.println("Name: " + name);
            out.println("Damage: " + damage);
            out.println("Type: " + type);
            out.println("Element Type: " + elementType);
            out.println();
        }


    }

    @Override
    public void configureDeck(List<String> cardIds, String username) throws SQLException {
        //check if cards are already in the deck

        PreparedStatement checkCardsInDeckStmt = conn.prepareStatement("SELECT id FROM cards WHERE id = ? AND user_id = ? AND in_deck = true");
        this.selectUser.setString(1,username);
        ResultSet rs = selectUser.executeQuery();
        int userId = 0;
        while (rs.next()) {
            userId = rs.getInt("id");
        }


        //getting id of the user
        checkCardsInDeckStmt.setInt(2, userId);
        //putting the number of cards to a list that are in the deck and are the same as the provided ids in the curl script
        List<String> cardsInDeck = new ArrayList<>();

        for (int i = 0; i < cardIds.size(); i++) {
            checkCardsInDeckStmt.setString(1, cardIds.get(i));
            ResultSet rs2 = checkCardsInDeckStmt.executeQuery();
            while (rs2.next()) {
            cardsInDeck.add(rs2.getString("id"));
            }
        }


        if(cardsInDeck.size()>0) {
            out.println("The following cards are already in the deck: " + cardsInDeck);
        }
        else {
            //Add cards to deck

            for (String cardId : cardIds) {
                this.configureDeckbyUsername.setString(1,cardId);
                this.configureDeckbyUsername.executeUpdate();
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


    }

    @Override
    public void editUser(List<String> userDatas, User authUser) throws SQLException {

        this.editUser.setString(4,authUser.getUsername());
        this.editUser.setString(1, userDatas.get(2));
        this.editUser.setString(2, userDatas.get(0));
        this.editUser.setString(3, userDatas.get(1));

        this.editUser.executeUpdate();


    }

    @Override
    public void showScoreboard() throws SQLException {
        List<User> userDatas = this.getAll();
        userDatas.sort(Comparator.comparing(User::getWins).reversed());
        out.println("Scoreboard by most wins: \n");
        for (User userData : userDatas) {
            out.println("Username: " + userData.getUsername());
            out.println("Wins: " + userData.getWins());

        }
        userDatas.sort(Comparator.comparingDouble((User u) -> (float)u.getWins()/(float)u.getGames()).reversed());
        out.println("Scoreboard by highest win rate: \n");
        for (User userData : userDatas) {
            out.println("Username: " + userData.getUsername());
            out.printf("Win Ratio: %.2f%%\n",(float)userData.getWins()/userData.getGames()*100);
        }


    }

    @Override
    public String getTradings() {
        try {

            ResultSet rs = getTradings.executeQuery();
            List<Trading> tradings = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String cardtotrade = rs.getString("cardtotrade");
                int minimumdamage = rs.getInt("minimumdamage");
                tradings.add(new Trading(id, cardtotrade, minimumdamage));
            }

            if(tradings != null && !tradings.isEmpty()){
                for(Trading trading : tradings){
                    out.println("Id: " + trading.getId());
                    out.println("Card to trade: " + trading.getCardtotrade());
                    out.println("Minimum damage: " + trading.getMinimumdamage());
                }
                return "";
            }
            else{

                return "There are no available trading deals.";

            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void postTradings(List<Trading> tradeElements, int id) throws SQLException {
        this.postTradings.setString(1, String.valueOf(tradeElements.get(0)));
        this.postTradings.setString(2, String.valueOf(tradeElements.get(1)));
        this.postTradings.setString(3, String.valueOf(tradeElements.get(2)));
        this.postTradings.setInt(4, Integer.parseInt(String.valueOf(tradeElements.get(3))));
        this.postTradings.setInt(5, id);
    }

    @Override
    public void deleteTradings(String tradeIdStr) throws SQLException {
        this.deleteTradings.setString(1,tradeIdStr);
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

