package com.example.mtcg;

import com.example.mtcg.card.Card;
import com.example.mtcg.market.Trading;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import java.sql.*;
import java.util.*;



public class EchoMultiServer {

    private ServerSocket serverSocket;

    public void start(int port) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "Bazisugrokatica11");
            serverSocket = new ServerSocket(port);
            System.out.println("MTCG Server started on port " + port);

            String sql = "DELETE FROM PACKAGE;DELETE FROM GAME;DELETE FROM CARDS;DELETE FROM USERS;";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.executeUpdate();

            while (true) {
                new EchoClientHandler(serverSocket.accept(), conn).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            stop();
        }

    }

    public void stop() {
        try {

            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static class EchoClientHandler extends Thread {
        private final Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private final Map<String, Rest<User>> endPoints = new HashMap<>();
        private final RestUser restUser;

        private final ObjectMapper objectMapper = new ObjectMapper();

        public EchoClientHandler(Socket socket, Connection conn) throws SQLException {
            this.clientSocket = socket;
            restUser = new RestUser(conn);
            endPoints.put("/users", restUser);
        }


        public void run() {
            try {
                Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "Bazisugrokatica11");
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);

                    String[] elsoSorDarabok = inputLine.split(" ");

                    Rest<User> rest = endPoints.get("/users");


                    if (elsoSorDarabok[1].startsWith("/generatecardsfor/")) {
                        User usercards = rest.generateCard(Integer.parseInt(elsoSorDarabok[1].substring(18)));
                        out.println("HTTP/1.1 200 OK");
                        out.println("Content-Type: application/json");
                        out.println("Connection: close");
                        out.println("");
                        out.println(objectMapper.writeValueAsString(usercards));
                        out.println();
                        out.println("Cards have been generated for " + usercards.getUsername());
                        break;

                    } else if (elsoSorDarabok[1].startsWith("/sessions")) {
                        String userJson = getRequestBody(in);
                        User user = objectMapper.readValue(userJson, User.class);
                        System.out.println(userJson);
                        User authUser = restUser.login(user.getUsername(), user.getPassword());

                        if (authUser != null) {
                            out.println("HTTP/1.1 200 OK");
                            out.println("Content-Type: text/plain");
                            out.println("Connection: close");
                            out.println("");
                            out.println("Authorization: "+authUser.getUsername()+"-mtcgToken");
                            out.println(authUser.getUsername() + " logged in");
                            out.println();
                        } else {
                            out.println("HTTP/1.1 401 Unauthorized");
                            out.println("Content-Type: text/html");
                            out.println("");
                            out.println("<html><body><h1>401 Unauthorized</h1></body></html>");
                            out.println("Error: Wrong username or password");
                        }
                        break;


                    }
                    else if (elsoSorDarabok[1].startsWith("/packages")){
                        String token = getAuthorizationToken(in);
                        if (!token.equals("admin-mtcgToken")) {
                            // hiba van
                            out.println("HTTP/1.1 401 Unauthorized");
                            out.println("Content-Type: text/html");
                            out.println("");
                            out.println("<html><body><h1>401 Unauthorized</h1></body></html>");
                        } else {
                            try {
                                String packageJson = getRequestBodyAsList(in);

                                JSONArray jsonArray = new JSONArray(packageJson);
                                List<Card> cards = new ArrayList<>();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonObjectToLowerCase(jsonArray.getJSONObject(i));
                                    Card card = objectMapper.readValue(jsonObject.toString(), Card.class);
                                    cards.add(card);
                                }
                                rest.createPackage(cards);
                                out.println("HTTP/1.1 200 OK");
                                out.println("Content-Type: text/plain");
                                out.println("Connection: close");
                                out.println("");
                                out.println("Package created");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                        }
                        break;
                    } else if (elsoSorDarabok[1].startsWith("/transactions/packages")) {
                        String token = getAuthorizationToken(in);

                        String username = "";
                        // TODO token ellenőrzése: van, és -mctgToken-re végződik, és ebből kellene az, ami ez előtt van
                        if(token != null && token.endsWith("-mtcgToken")){

                            int indexToken = token.indexOf("-mtcgToken");
                            username = token.substring(0, indexToken);

                        }
                        User authUser = new User(username, "1234");

                        PreparedStatement stmt = conn.prepareStatement("SELECT coins FROM users WHERE username = ?");
                        stmt.setString(1, authUser.getUsername());
                        ResultSet rs = stmt.executeQuery();
                        while (rs.next()) {
                            //authUser.setCoins(10);
                            if (authUser == null){
                                out.println("HTTP/1.1 401 Unauthorized");
                                out.println("Content-Type: text/html");
                                out.println("");
                                out.println("<html><body><h1>401 Unauthorized</h1></body></html>");
                            }
                            PreparedStatement checkPackages = conn.prepareStatement("SELECT COUNT(DISTINCT package_id) FROM cards WHERE package_id NOT IN (SELECT package_id FROM cards WHERE user_id IS NOT NULL)");
                            ResultSet rs2 = checkPackages.executeQuery();
                            int remaining = 0;
                            if (rs2.next()) {
                                remaining = rs2.getInt(1);
                                System.out.println(remaining);
                                if(remaining == 0){
                                    out.println("HTTP/1.1 403 Forbidden");
                                    out.println("Content-Type: text/html");
                                    out.println("");
                                    out.println("<html><body><h1>403 Forbidden</h1></body></html>");
                                    System.out.println("No packages left.");
                                    break;
                                }
                                if(rs.getInt("coins") < 5) {
                                    out.println("HTTP/1.1 403 Forbidden");
                                    out.println("Content-Type: text/html");
                                    out.println("");
                                    out.println("<html><body><h1>403 Forbidden</h1></body></html>");
                                    System.out.println("Not enough coins");
                                    break;
                                } else {
                                    out.println("HTTP/1.1 200 OK");
                                    out.println("Content-Type: text/plain");
                                    out.println("Connection: close");
                                    out.println("");
                                    out.println("Authorization: "+token);
                                    out.println();
                                    rest.buyPackage(authUser);
                                    out.println("Transaction successful.");

                                }
                            }

                        }

                        rs.close();
                        stmt.close();

                        break;

                    } else if (elsoSorDarabok[1].startsWith("/cards")) {

                        String token = getAuthorizationToken(in);

                        String username = "";
                        if(token != null && token.endsWith("-mtcgToken")){

                            int indexToken = token.indexOf("-mtcgToken");
                            username = token.substring(0, indexToken);

                        }
                        else{
                            out.println("HTTP/1.1 403 Forbidden");
                            out.println("Content-Type: text/html");
                            out.println("");
                            out.println("<html><body><h1>403 Forbidden</h1></body></html>");
                            out.println("No token provided.");
                            break;
                        }
                        User authUser = new User(username, "1234");
                        PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE username = ?");
                        stmt.setString(1, authUser.getUsername());

                        ResultSet rs = stmt.executeQuery();

                        while (rs.next()) {
                            if (token != null) {
                                out.println("HTTP/1.1 200 OK");
                                out.println("Content-Type: application/json");
                                out.println("Connection: close");
                                out.println("");
                                List<Card> cards = rest.getCards(rs.getInt("id"));
                                out.println(objectMapper.writeValueAsString(cards));
                                out.println();
                            } else {
                                out.println("HTTP/1.1 401 Unauthorized");
                                out.println("Content-Type: text/html");
                                out.println("");
                                out.println("<html><body><h1>401 Unauthorized</h1></body></html>");
                            }
                        }
                        break;

                    }
                    else if (elsoSorDarabok[1].startsWith("/deck?format=plain")) {
                        String token = getAuthorizationToken(in);
                        String username = "";
                        if(token != null && token.endsWith("-mtcgToken")){

                            int indexToken = token.indexOf("-mtcgToken");
                            username = token.substring(0, indexToken);

                        }
                        User authUser = new User(username, "1234");
                        PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE username = ?");
                        stmt.setString(1, authUser.getUsername());

                        ResultSet rs = stmt.executeQuery();

                        while (rs.next()) {

                            if (token != null) {
                                out.println("HTTP/1.1 200 OK");
                                out.println("Content-Type: text/plain"); //Plain format
                                out.println("Connection: close");
                                out.println();
                            } else {
                                out.println("HTTP/1.1 401 Unauthorized");
                                out.println("Content-Type: text/html");
                                out.println("");
                                out.println("<html><body><h1>401 Unauthorized</h1></body></html>");
                            }
                            PreparedStatement getFullDeck = conn.prepareStatement("SELECT * FROM cards WHERE user_id = ? AND in_deck = true");
                            getFullDeck.setInt(1, rs.getInt("id"));
                            ResultSet rs2 = getFullDeck.executeQuery();
                            while (rs2.next()) {
                                String cardId = rs2.getString("id");
                                String name = rs2.getString("name");
                                int damage = rs2.getInt("damage");
                                String type = rs2.getString("type");
                                String elementType = rs2.getString("element");
                                out.println("ID: " + cardId);
                                out.println("Name: " + name);
                                out.println("Damage: " + damage);
                                //out.println("Type: " + type);
                                //out.println("Element Type: " + elementType);
                                out.println();
                            }
                            //rest.getDeckAsPlain(rs.getInt("id"));

                        }
                        break;
                    }
                    else if (elsoSorDarabok[1].startsWith("/deck")) {
                        String token = getAuthorizationToken(in);
                        boolean isConfigured = false;
                        String deckJson = null;
                        if (elsoSorDarabok[0].equals("PUT")) {
                            deckJson = getRequestBodyAsList(in);
                        }


                        String username = "";
                        if(token != null && token.endsWith("-mtcgToken")){

                            int indexToken = token.indexOf("-mtcgToken");
                            username = token.substring(0, indexToken);

                        }

                        if(deckJson != null){


                            JSONArray jsonArray = new JSONArray(deckJson);
                            if(jsonArray.length() < 4){
                                out.println("HTTP/1.1 403 Forbidden");
                                out.println("Content-Type: text/html");
                                out.println("");
                                out.println("<html><body><h1>403 Forbidden</h1></body></html>");
                                System.out.println("Not enough cards provided");
                                break;
                            }
                            List<String> cardIds = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                cardIds.add(jsonArray.getString(i));
                            }
                            rest.configureDeck(cardIds,username);
                            isConfigured = true;
                        }



                        User authUser = new User(username, "1234");
                        PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE username = ?");
                        stmt.setString(1, authUser.getUsername());

                        ResultSet rs = stmt.executeQuery();

                        while (rs.next()) {

                            if (token != null) {
                                out.println("HTTP/1.1 200 OK");
                                out.println("Content-Type: application/json");
                                out.println("Connection: close");
                                out.println();
                            } else {
                                out.println("HTTP/1.1 401 Unauthorized");
                                out.println("Content-Type: text/html");
                                out.println("");
                                out.println("<html><body><h1>401 Unauthorized</h1></body></html>");
                            }
                            List<Card> deck = rest.getDeck(rs.getInt("id"));
                            if(deck.isEmpty()){
                                out.println(username+"'s deck: ");
                                out.println("\nThe deck is empty.");
                            }
                            else{
                                if(isConfigured){
                                    out.println("\n"+username+"'s deck reconfigured.\n");
                                }
                                out.println(username+"'s deck: ");
                                out.println(objectMapper.writeValueAsString(deck));
                            }

                            out.println();
                        }
                        break;

                    }

                    else if (elsoSorDarabok[1].startsWith("/stats")) {
                        String token = getAuthorizationToken(in);

                        String username = "";
                        if(token != null && token.endsWith("-mtcgToken")){

                            int indexToken = token.indexOf("-mtcgToken");
                            username = token.substring(0, indexToken);

                        }
                        User authUser = new User(username, "1234");

                        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
                        stmt.setString(1, authUser.getUsername());

                        ResultSet rs = stmt.executeQuery();

                        while (rs.next()) {

                            if (token != null) {
                                out.println("HTTP/1.1 200 OK");
                                out.println("Content-Type: text/plain"); //Plain format
                                out.println("Connection: close");
                                out.println();
                            } else {
                                out.println("HTTP/1.1 401 Unauthorized");
                                out.println("Content-Type: text/html");
                                out.println("");
                                out.println("<html><body><h1>401 Unauthorized</h1></body></html>");
                            }
                            out.println(rs.getString("username") + " data: ");
                            out.println();
                            out.println("Wins: "+rs.getInt("wins"));
                            out.println("Losses: "+rs.getInt("losses"));
                            out.println("Games: "+rs.getInt("games"));
                            out.println();
                        }
                        break;
                    }
                    else if (elsoSorDarabok[1].startsWith("/score")) {
                        String token = getAuthorizationToken(in);

                        String username = "";
                        if(token != null && token.endsWith("-mtcgToken")){

                            int indexToken = token.indexOf("-mtcgToken");
                            username = token.substring(0, indexToken);

                        }

                        if (token != null) {
                            out.println("HTTP/1.1 200 OK");
                            out.println("Content-Type: text/plain"); //Plain format
                            out.println("Connection: close");
                            out.println();
                        } else {
                            out.println("HTTP/1.1 401 Unauthorized");
                            out.println("Content-Type: text/html");
                            out.println("");
                            out.println("<html><body><h1>401 Unauthorized</h1></body></html>");
                        }
                        //rest.showScoreboard();

                        List<User> userDatas = rest.getAll();
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
                        userDatas.sort(Comparator.comparing(User::getElo).reversed());
                        out.println("Scoreboard by biggest elo: \n");
                        for (User userData : userDatas) {
                            out.println("Username: " + userData.getUsername());
                            out.println("Wins: " + userData.getWins());

                        }



                        out.println();

                        break;
                    }
                    else if (elsoSorDarabok[1].startsWith("/tradings")) {
                        String token = getAuthorizationToken(in);

                        String username = "";
                        if(token != null && token.endsWith("-mtcgToken")){

                            int indexToken = token.indexOf("-mtcgToken");
                            username = token.substring(0, indexToken);

                        }
                        User authUser = new User(username, "1234");
                        PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE username = ?");
                        stmt.setString(1, authUser.getUsername());


                        switch (elsoSorDarabok[0]) {
                            case "GET":
                                if (token != null) {
                                    out.println("HTTP/1.1 200 OK");
                                    out.println("Content-Type: text/plain"); //Plain format
                                    out.println("Connection: close");
                                    out.println();
                                } else {
                                    out.println("HTTP/1.1 401 Unauthorized");
                                    out.println("Content-Type: text/html");
                                    out.println("");
                                    out.println("<html><body><h1>401 Unauthorized</h1></body></html>");
                                }
                                String output = rest.getTradings();
                                out.println(output);
                                out.println();

                                break;
                            case "POST":

                                String tradeIdString = elsoSorDarabok[1].substring(10);

                                if(tradeIdString == null){
                                    String tradeJson = getRequestBodyAsList(in);
                                    if(tradeJson != null){


                                        JSONArray jsonArray = new JSONArray(tradeJson);

                                        List<Trading> tradeElements = new ArrayList<>();
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject jsonObject = jsonObjectToLowerCase(jsonArray.getJSONObject(i));
                                            Trading trading = objectMapper.readValue(jsonObject.toString(), Trading.class);
                                            tradeElements.add(trading);
                                        }

                                        ResultSet rs = stmt.executeQuery();

                                        while (rs.next()) {
                                            if (token != null) {
                                                out.println("HTTP/1.1 200 OK");
                                                out.println("Content-Type: application/json"); //Plain format
                                                out.println("Connection: close");
                                                out.println();
                                            } else {
                                                out.println("HTTP/1.1 401 Unauthorized");
                                                out.println("Content-Type: text/html");
                                                out.println("");
                                                out.println("<html><body><h1>401 Unauthorized</h1></body></html>");
                                            }
                                            rest.postTradings(tradeElements,rs.getInt("id"));
                                            out.println("Trading deal created by "+username);
                                            out.println();
                                        }
                                    }
                                }
                                else{
                                    String tradeJson = getRequestBody(in);
                                    stmt.setString(1,username);
                                    ResultSet result = stmt.executeQuery();
                                    int userId = -1;
                                    if(result.next()) {
                                        userId = result.getInt("id");
                                    }

                                    PreparedStatement checkIfMyTrade = conn.prepareStatement("SELECT id, user_id FROM cards WHERE id = ?");
                                    checkIfMyTrade.setString(1,tradeJson);
                                    ResultSet result2 = checkIfMyTrade.executeQuery();
                                    if(result2.next()) {
                                        String id = result2.getString("id");
                                        int cardUserId = result2.getInt("user_id");
                                        if(id == tradeJson && cardUserId == userId) {
                                            // Perform trade
                                        } else {
                                            // Do not perform trade
                                        }
                                    }
                                    ResultSet rs2 = checkIfMyTrade.executeQuery();

                                    while (rs2.next()) {
                                        if (token != null) {
                                            out.println("HTTP/1.1 200 OK");
                                            out.println("Content-Type: application/json"); //Plain format
                                            out.println("Connection: close");
                                            out.println("");
                                            out.println();

                                        } else {
                                            out.println("HTTP/1.1 401 Unauthorized");
                                            out.println("Content-Type: text/html");
                                            out.println("");
                                            out.println("<html><body><h1>401 Unauthorized</h1></body></html>");
                                        }
                                        //rest.postTradings(tradeElements,rs2.getInt("id"));
                                        out.println("Trading deal created by "+username);
                                        out.println();
                                    }
                                }
                                break;
                            case "DELETE": // /tradings/
                                String tradeIdStr = elsoSorDarabok[1].substring(10);
                                rest.deleteTradings(tradeIdStr);
                                out.println("HTTP/1.1 204 No content");
                                out.println("Content-Type: application/json");
                                out.println("Trading deal ("+tradeIdStr+") deleted by "+ username);
                                out.println("");
                                break;
                        }

                    }
                    else if (elsoSorDarabok[1].startsWith("/users")) {

                        switch (elsoSorDarabok[0]) {
                            case "GET":
                                String token = getAuthorizationToken(in);

                                String username = "";
                                if(token != null && token.endsWith("-mtcgToken")){

                                    int indexToken = token.indexOf("-mtcgToken");
                                    username = token.substring(0, indexToken);

                                }

                                User authUser = new User(username, "1234");


                                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
                                stmt.setString(1, authUser.getUsername());

                                ResultSet rs = stmt.executeQuery();

                                while (rs.next()) {

                                    /*String urlname = elsoSorDarabok[1].substring(7);
                                    String tokenname = username;
                                    if(urlname != tokenname){
                                        out.println("HTTP/1.1 401 Unauthorized");
                                        out.println("Content-Type: text/html");
                                        out.println("");
                                        out.println("<html><body><h1>401 Unauthorized</h1></body></html>");
                                        out.println(elsoSorDarabok[1].substring(7));
                                        out.println(username);
                                        break;
                                    }*/ //Something is very wrong with the compiler

                                    if (token != null) {
                                        out.println("HTTP/1.1 200 OK");
                                        out.println("Content-Type: text/plain"); //Plain format
                                        out.println("Connection: close");
                                        out.println();
                                    } else {
                                        out.println("HTTP/1.1 401 Unauthorized");
                                        out.println("Content-Type: text/html");
                                        out.println("");
                                        out.println("<html><body><h1>401 Unauthorized</h1></body></html>");
                                    }
                                    out.println(rs.getString("username") + " data: ");
                                    out.println();
                                    out.println("Id: "+rs.getInt("id"));
                                    out.println("Coins: "+rs.getInt("coins"));
                                    out.println("Elo: "+rs.getInt("elo"));
                                    out.println("Wins: "+rs.getInt("wins"));
                                    out.println("Losses: "+rs.getInt("losses"));
                                    out.println("Games: "+rs.getInt("games"));
                                    out.println("Bio: "+rs.getString("bio"));
                                    out.println("Image: "+rs.getString("image"));
                                    if(rs.getString("name") != null){
                                        out.println("Name: "+rs.getString("name"));
                                    }
                                    out.println();
                                }
                                break;
                            case "POST":
                                String userJson = getRequestBody(in);
                                User user = objectMapper.readValue(userJson, User.class);
                                if (restUser.checkUser(user)) {
                                    out.println("HTTP/1.1 404 Not Found");
                                    out.println("Content-Type: text/html");
                                    out.println("");
                                    out.println("<html><body><h1>404 Not Found</h1></body></html>");
                                    out.println("User already exists");
                                } else {


                                    out.println("HTTP/1.1 200 OK");
                                    out.println("Content-Type: application/json");
                                    out.println("");


                                    String plainText = user.getPassword();

                                    String hashedPassword = BCrypt.hashpw(plainText, BCrypt.gensalt());

                                    //dehash
                                /*
                                String hashedPassword = rs.getString("password");
                                if(BCrypt.checkpw(password, hashedPassword)) {
                                    System.out.println("Login successful!");
                                }
                                 */

                                    user.setPassword(hashedPassword);
                                    out.println(objectMapper.writeValueAsString(rest.post(user)));
                                    out.println("User created");
                                }
                                break;

                            case "PUT":
                                String token2 = getAuthorizationToken(in);

                                String username2 = "";
                                if(token2 != null && token2.endsWith("-mtcgToken")){

                                    int indexToken = token2.indexOf("-mtcgToken");
                                    username2 = token2.substring(0, indexToken);

                                }
                                User authUser2 = new User(username2, "1234");
                                String userDataJson = getRequestBodyAsList(in);
                                PreparedStatement stmtPut = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
                                stmtPut.setString(1, authUser2.getUsername());

                                ResultSet rs2 = stmtPut.executeQuery();

                                JSONObject jsonObject = new JSONObject(userDataJson);
                                List<String> userDatas = new ArrayList<>();
                                Iterator<String> keys = jsonObject.keys();
                                while (keys.hasNext()) {
                                    String key = keys.next();
                                    userDatas.add(jsonObject.getString(key));
                                }


                                while (rs2.next()) {

                                    if (token2 != null) {
                                        out.println("HTTP/1.1 200 OK");
                                        out.println("Content-Type: application/json"); //Plain format
                                        out.println("Connection: close");


                                        out.println();
                                    } else {
                                        out.println("HTTP/1.1 401 Unauthorized");
                                        out.println("Content-Type: text/html");
                                        out.println("");
                                        out.println("<html><body><h1>401 Unauthorized</h1></body></html>");
                                    }

                                    out.println();
                                    rest.editUser(userDatas, authUser2);
                                    out.println(username2+" data edited.");
                                }


                                break;

                            case "DELETE": //egy adott user torlese. pelda: /users/1
                                String userIdStr = elsoSorDarabok[1].substring(7);
                                int userId = Integer.parseInt(userIdStr);
                                User deleted = rest.deleting(userId);
                                out.println("HTTP/1.1 204 No content");
                                out.println("Content-Type: application/json");
                                out.println("");

                                out.println(objectMapper.writeValueAsString(deleted));


                            default:
                                out.println("HTTP/1.1 400 Bad Request");
                                out.println("Content-Type: text/html");
                                out.println("");
                                out.println("<html><body><h1>400 Bad Request</h1></body></html>");
                                break;

                        }

                        out.flush();
                        out.close();
                        in.close();
                        clientSocket.close();
                    }

                }

                in.close();
                out.close();
                clientSocket.close();

            } catch (IOException | SQLException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private String jsonToLowerCase(String json) {
            try {
                Map<String, Object> map = objectMapper.readValue(json, Map.class);
                Map<String, Object> map2 = new HashMap<>();
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    map2.put(entry.getKey().toLowerCase(), entry.getValue());
                }
                return objectMapper.writeValueAsString(map2);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private JSONObject jsonObjectToLowerCase(JSONObject node){
            JSONObject out = new JSONObject();
            for (String key : node.keySet()) {
                out.put(key.toLowerCase(), node.get(key));
            }
            return out;
        }

        public String getAuthorizationToken(BufferedReader in) throws IOException {
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                if (line.startsWith("Authorization: Basic ")) {
                    return line.substring(21);
                }
                else {
                    //error
                    //System.out.println("Token not found");
                }
            }
            return line;
        }

        public String getRequestBody(BufferedReader in) throws IOException {
            String inputLine;
            while ((inputLine = in.readLine()) != null && !inputLine.isEmpty()) {

            }
            char[] buffer = new char[1024];
            int read = in.read(buffer);
            return jsonToLowerCase(new String(buffer, 0, read));
        }

        public String getRequestBodyAsList(BufferedReader in) throws IOException {
            String inputLine;
            while ((inputLine = in.readLine()) != null && !inputLine.isEmpty()) {

            }
            char[] buffer = new char[1024];
            int read = in.read(buffer);
            return new String(buffer, 0, read);
        }
    }

    public static void main(String[] args) {

        EchoMultiServer server = new EchoMultiServer();
        server.start(10001);

    }

}
