package com.example.mtcg;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;


import com.example.mtcg.card.Card;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

import javax.crypto.SecretKey;

public class EchoMultiServer {

    //private static final Logger LOG = LoggerFactory.getLogger(EchoMultiServer.class);

    private ServerSocket serverSocket;

    static Cipher cipher;

    public void start(int port) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "Bazisugrokatica11");
            serverSocket = new ServerSocket(port);
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

        public void httpGetTest(int usernumber) throws IOException {
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/html"); // application/json
            out.println("Connection: close");
            out.println("");
            if (usernumber == 12) {
                out.println("<html><body>\r\n[{\"id\":1,\"name\":\"John Doe\"}]");
                out.println("<h1>Adatatvitel sikeres.</h1></body></html>");

                out.println();
                out.flush();
                out.close();
                in.close();
            } else if (usernumber == 2) {
                out.println("<html><body>\r\n[{\"id\":2,\"name\":\"Jane Doe\"}]");
                out.println("<h1>Adatatvitel sikeres.</h1></body></html>");
                out.println();
                out.flush();
                out.close();
                in.close();
            } else {
                out.println("HTTP/1.1 404 Not Found");
                out.println("Content-Type: text/html");
                out.println("");
                out.println("<html><body><h1>404 Not Found</h1></body></html>");
            }
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
                        // + userId
                        // meghívjuk a generateCardsForUser(userId) metódust (amit a mainben írtam le)
                        // visszakapjuk a játékost a deckjével együtt
                        // visszaküldjük a kliensnek (objectMapper segítségével JSON-be alakítva)
                        User usercards = rest.generateCard(Integer.parseInt(elsoSorDarabok[1].substring(18)));
                        out.println("HTTP/1.1 200 OK");
                        out.println("Content-Type: application/json");
                        out.println("Connection: close");
                        out.println("");
                        out.println(objectMapper.writeValueAsString(usercards));
                        out.println();
                        break;

                    } else if (elsoSorDarabok[1].startsWith("/users")) {
                        switch (elsoSorDarabok[0]) {
                            case "GET":
                                if (elsoSorDarabok[1].equals("/users/12")) {
                                    httpGetTest(12);
                                    //String userJson = rest.get(1);

                                }
                                List<User> users = restUser.getAll(); // van egy listánk a DB-ben szereplő, és onnan lekérdezett userekről
                                String jsonUsers = objectMapper.writeValueAsString(users); // itt alakítja JSON-formátumba a listát
                                out.println("HTTP/1.1 200 OK");
                                out.println("Content-Type: application/json"); // application/json
                                out.println("Connection: close");
                                out.println("");
                                out.println(jsonUsers);
                                break;
                            case "POST":
                                while ((inputLine = in.readLine()) != null && !inputLine.isEmpty()) {

                                }
                                String userAsJson = in.readLine();
                                User user = objectMapper.readValue(userAsJson, User.class);
                                out.println("HTTP/1.1 200 OK");
                                out.println("Content-Type: application/json");
                                out.println("");

                                //out.println("<html><body><h1>POST has been successful</h1></body></html>");

                                KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
                                keyGenerator.init(128); // block size is 128bits
                                SecretKey secretKey = keyGenerator.generateKey();
                                cipher = Cipher.getInstance("AES");

                                String plainText = user.getPassword();
                               // out.println("Plain Text Before Encryption: " + plainText);


                                String encryptedText = encrypt(plainText, secretKey);

                                //out.println("Encrypted Text After Encryption: " + encryptedText);

                                //String decryptedText = decrypt(encryptedText, secretKey);

                               // out.println("Decrypted Text After Decryption: " + decryptedText);

                                //User user = new User("Gipsz Jakab", encryptedText);
                                user.setPassword(encryptedText);
                                out.println(objectMapper.writeValueAsString(rest.post(user)));

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
    }


    public static String encrypt(String plainText, SecretKey secretKey) throws Exception {
        byte[] plainTextByte = plainText.getBytes();
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedByte = cipher.doFinal(plainTextByte);
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(encryptedByte);
    }

    public static String decrypt(String encryptedText, SecretKey secretKey) throws Exception {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] encryptedTextByte = decoder.decode(encryptedText);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedByte = cipher.doFinal(encryptedTextByte);
        return new String(decryptedByte);
    }

    //generateCard


    public static void main(String[] args) {
        // kell egy metódus, ami innen hivva ezt csinálja:
        // 1. paraméterként kapja a játékos id-ját
        // 2. az összes olyan kártyánál, ami a játékosnál van (user_id = játékos id), az in_deck-et átállítja false-ra -> UPDATE cards SET in_deck = false WHERE user_id = játékos id
        // 3. létrehoz 4 kártyát random úgy, hogy a user_id = játékosé és az in_deck = true
            // Card uj = Card.generateRandom()
            // INSERT INTO cards (user_id, in_deck, name, damage, type) VALUES (játékos id, true, uj.getName(), uj.getDamage(), uj.getType())

        // 4. visszaadja a játékost (és a 4 új kártyát)

        EchoMultiServer server = new EchoMultiServer();
        server.start(5555);

    }

}
