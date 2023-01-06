package com.example.mtcg;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;


import com.example.mtcg.card.Card;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EchoMultiServer {

    //private static final Logger LOG = LoggerFactory.getLogger(EchoMultiServer.class);

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
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);

                    String[] elsoSorDarabok = inputLine.split(" ");
                    //int index = elsoSorDarabok[1].substring(1).indexOf("/"); // /users/1 vagy /users
                    int index = 0;
                    // elsoSorDarabok[1].substring(1) -> users/1 vagy users
                    // elsoSorDarabok[1].substring(1).indexOf("/") -> 5 vagy -1
                    //String path = (index == -1) ? elsoSorDarabok[1] : elsoSorDarabok[1].substring(0, index+1);
                    String path = "/";
                    // path -> /users vagy /users
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
                                out.println("Package created");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
/*                            System.out.println(" Card 1: " + cards.get(0));
                            System.out.println(" Card 2: " + cards.get(1));
                            System.out.println(" Card 3: " + cards.get(2));*/

                            /*for (int i = 0; i < array.length(); i++) {
                                String jsonString = array.getJSONObject(i).toString();
                                Card card = objectMapper.readValue(jsonToLowerCase(jsonString), Card.class);
                                rest.createPackage(card); //idx in card table is integer, but in json it is string, fix it
                                System.out.println("card : " + card);
                            }*/
                        }
                        break;
                    } else if (elsoSorDarabok[1].startsWith("/transactions/packages")) {
                        String token = getAuthorizationToken(in);
                        String userJson = getRequestBody(in);
                        User user = objectMapper.readValue(userJson, User.class);
                        System.out.println(userJson);
                        User authUser = restUser.login(user.getUsername(), user.getPassword());
                        if (authUser == null){
                            out.println("HTTP/1.1 401 Unauthorized");
                            out.println("Content-Type: text/html");
                            out.println("");
                            out.println("<html><body><h1>401 Unauthorized</h1></body></html>");
                        }
                        if(authUser.getCoins() < 5) {
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
                            out.println("Authorization: "+authUser.getUsername()+"-mtcgToken");
                            out.println();
                            rest.buyPackage(authUser);
                            //nincs kesz
                            out.println("Transaction successful.");
                        }


                        break;


                    } else if (elsoSorDarabok[1].startsWith("/cards")) {
                        String token = getAuthorizationToken(in);
                        String userJson = getRequestBody(in);
                        User user = objectMapper.readValue(userJson, User.class);
                        System.out.println(userJson);
                        User authUser = restUser.login(user.getUsername(), user.getPassword());
                        if (authUser != null) {
                            out.println("HTTP/1.1 200 OK");
                            out.println("Content-Type: application/json");
                            out.println("Connection: close");
                            out.println("");
                            out.println("Authorization: Basic "+authUser.getUsername()+"-mtcgToken");
                            out.println();
                        } else {
                            out.println("HTTP/1.1 401 Unauthorized");
                            out.println("Content-Type: text/html");
                            out.println("");
                            out.println("<html><body><h1>401 Unauthorized</h1></body></html>");
                        }

                        User usercards = rest.getCards(Integer.parseInt(elsoSorDarabok[1].substring(7)));
                        out.println(objectMapper.writeValueAsString(usercards));
                        out.println();
                        break;

                    } else if (elsoSorDarabok[1].startsWith("/deck")) { //hianyzik a deck konfiguralasa es konfiguralt deck lekerese

                        String userJson = getRequestBody(in);
                        User user = objectMapper.readValue(userJson, User.class);
                        System.out.println(userJson);
                        User authUser = restUser.login(user.getUsername(), user.getPassword());

                        if (authUser != null) {
                            out.println("HTTP/1.1 200 OK");
                            out.println("Content-Type: application/json");
                            out.println("Connection: close");
                            out.println("");
                            out.println("Authorization: Basic "+authUser.getUsername()+"-mtcgToken");
                            out.println();
                        } else {
                            out.println("HTTP/1.1 401 Unauthorized");
                            out.println("Content-Type: text/html");
                            out.println("");
                            out.println("<html><body><h1>401 Unauthorized</h1></body></html>");
                        }
                        User deck = rest.getDeck(Integer.parseInt(elsoSorDarabok[1].substring(6)));
                        out.println(objectMapper.writeValueAsString(deck));
                        out.println();
                        break;

                    } else if (elsoSorDarabok[1].startsWith("/users")) {
                        switch (elsoSorDarabok[0]) {
                            case "GET":
                                List<User> users = restUser.getAll(); // van egy listánk a DB-ben szereplő, és onnan lekérdezett userekről
                                String jsonUsers = objectMapper.writeValueAsString(users); // itt alakítja JSON-formátumba a listát
                                out.println("HTTP/1.1 200 OK");
                                out.println("Content-Type: application/json"); // application/json
                                out.println("Connection: close");
                                out.println("");
                                out.println(jsonUsers);
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

                            case "PUT": //mint a post, csak specifikusabb. Egy adott user felulirasa. pelda: /users/1


                                out.println("HTTP/1.1 200 OK");
                                out.println("Content-Type: text/html");
                                out.println("");

                                out.println("<html><body><h1>PUT has been successful</h1></body></html>");
                                //User user = new User();

                                User user2 = new User("Gipsz Jakab", "123456789");
                                out.println(user2.getUsername()); //Gipsz Jakab
                                out.println(rest.put(user2)); //null


                                break;

                            case "DELETE": //egy adott user torlese. pelda: /users/1
                                String userIdStr = elsoSorDarabok[1].substring(7);
                                int userId = Integer.parseInt(userIdStr);
                                User deleted = rest.deleting(userId);
                                out.println("HTTP/1.1 204 No content");
                                out.println("Content-Type: application/json");
                                out.println("");

                                /*out.println("<html><body><h1>DELETE has been successful</h1></body></html>");
                                User user1 = new User("Gipsz Jakab", "1234");
                                out.println(user1.getUsername()); //Gipsz Jakab
                                out.println(rest.deleting(userId)); //ide egy ID kellene
                                 */
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




                    //String path = "/";
                    // path -> /users vagy /users
                    //Rest<User> rest = endPoints.get("/users");




                    //Thread.sleep(5000);
                    /*if (".".equals(inputLine)) {
                        out.println("bye");
                        break;
                    }
                    out.println(inputLine);*/
                }

                in.close();
                out.close();
                clientSocket.close();

            } catch (IOException | SQLException e) {
                //LOG.debug(e.getMessage());
                System.out.println(e.getMessage());
                // } catch (InterruptedException e) {
                //     System.out.println(e.getMessage());
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


  /*                              while ((inputLine = in.readLine()) != null && !inputLine.isEmpty()) {

    }
    char[] buffer = new char[1024];
    int read = in.read(buffer);
    String userAsJson = new String(buffer, 0, read);
    userAsJson = jsonToLowerCase(userAsJson);
    //String userAsJson = in.readLine();
    User user = objectMapper.readValue(userAsJson, User.class);
*/


    //generateToken




    public static void main(String[] args) {
        // kell egy metódus, ami innen hivva ezt csinálja:
        // 1. paraméterként kapja a játékos id-ját
        // 2. az összes olyan kártyánál, ami a játékosnál van (user_id = játékos id), az in_deck-et átállítja false-ra -> UPDATE cards SET in_deck = false WHERE user_id = játékos id
        // 3. létrehoz 4 kártyát random úgy, hogy a user_id = játékosé és az in_deck = true
            // Card uj = Card.generateRandom()
            // INSERT INTO cards (user_id, in_deck, name, damage, type) VALUES (játékos id, true, uj.getName(), uj.getDamage(), uj.getType())

        // 4. visszaadja a játékost (és a 4 új kártyát)

        EchoMultiServer server = new EchoMultiServer();
        server.start(10001);

    }

}
