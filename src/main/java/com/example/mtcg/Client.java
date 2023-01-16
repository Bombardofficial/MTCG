package com.example.mtcg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

import com.example.mtcg.card.Card;
import org.mindrot.jbcrypt.BCrypt;

import static java.lang.Thread.sleep;

public class Client {

    public static void runAwayPrompt() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Are you sure you want to run away from being the Monster Card Trading Champion?");
        System.out.println("1. Yes");
        System.out.println("2. No");
        System.out.print("> ");
        int choice2 = sc.nextInt();
        if(choice2 == 1) {
            System.out.println("Understandable, have a great day!");
            System.exit(0);
        }
        else if(choice2 == 2) {
            System.out.println("You are a true champion! Get back in there!");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // handle the exception
            }
        }
        else {
            System.out.println("Invalid input!");
        }
    }


    public static void main(String[] args) throws SQLException {

        String host = "localhost";
        int port = 10001;

        // Set the POSTGRESQL database credentials
        String url = "jdbc:postgresql://localhost:5432/postgres";

        Connection conn = DriverManager.getConnection(url, "postgres", "Bazisugrokatica11");
        System.out.println("Welcome to the Monster Trading Card Game!");
        System.out.println("------------------------------------------");
        System.out.println("If you want to exit the log in or registering: write 'exit'\n\n");
        String username = "";

        int choice;
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit\n\n");
        System.out.print("> ");
        Scanner sc = new Scanner(System.in);
        choice = sc.nextInt();

        try(Socket socket = new Socket(host,port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {



            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            ///////////////MENU/////////////////////
            while(true){
                System.out.println("Username: ");

                username = stdIn.readLine();
                System.out.println("Password: ");
                String password = stdIn.readLine();
                if(username.equals("exit") || password.equals("exit")){
                    break;
                }



                if(choice == 2) {

                    //hash the password
                    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

                    PreparedStatement register = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
                    register.setString(1, username);
                    register.setString(2, hashedPassword);
                    register.executeUpdate();
                    System.out.println("Registration successful!");
                    break;
                }

                else if(choice == 1) {

                    //dehash the password and check if it matches
                    PreparedStatement login = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
                    login.setString(1, username);
                    ResultSet rs = login.executeQuery();
                    if(rs.next()) {
                        String hashedPassword = rs.getString("password");
                        if(BCrypt.checkpw(password, hashedPassword)) {
                            System.out.println("Login successful!");
                            break;
                        }
                        else {
                            System.out.println("Wrong password!");
                        }
                    }
                    else {
                        System.out.println("User does not exist!");
                    }
                }
                else if(choice == 3) {
                    runAwayPrompt();
                }
                else {
                    System.out.println("Invalid input!");
                }
            }
            ////////////////////////////////////////
            ///////////////INGAME MENU/////////////////////
            int choice2;
            User user = new User();

            System.out.println("1. Play");
            System.out.println("2. My Collection");
            System.out.println("3. Shop");
            System.out.println("4. My Profile");
            System.out.println("4. Leaderboard");
            System.out.println("5. Exit\n\n");
            System.out.print("> ");

            choice2 = sc.nextInt();
            if(choice2 == 1){
                // code for play option
                Socket socketclient = new Socket("localhost",10001);
                System.out.println("Looking for a worthy opponent...");
                //
                System.out.println("Opponent found!");
                BufferedReader inplay = new BufferedReader(new InputStreamReader(socketclient.getInputStream()));
                PrintWriter outplay = new PrintWriter(socketclient.getOutputStream(), true);
                outplay.println("GET /opponent HTTP/1.1");
                outplay.println("Authorization: Basic admin-mctgToken");
                outplay.println();
                outplay.println("{ \"user\": ");
                outplay.close();

                System.out.println("You are now playing against " + inplay.readLine());
                System.out.println("You have 4 cards in your hand: ");
                socketclient.close();

            }
            else if(choice2 == 2){
                // code for my collection option
                int choice3;
                System.out.println("1. Collection");
                System.out.println("2. Deck");
                System.out.println("3. Go back\n\n");
                System.out.print("> ");
                choice3 = sc.nextInt();
                if(choice3 == 1){
                    // code for collection option
                    System.out.println("Your collection: ");
                    PreparedStatement collection = conn.prepareStatement("SELECT * FROM cards WHERE user_id = ?");
                    collection.setString(1, username);
                    ResultSet rs = collection.executeQuery();
                    while(rs.next()) {
                        System.out.println("Id: "+rs.getString("id"));
                        System.out.println("Name: "+rs.getString("name"));
                        System.out.println("Type: "+rs.getString("type"));
                        System.out.println("Element: "+rs.getString("element"));
                        System.out.println("Damage: "+rs.getString("damage"));
                    }
                    int choicetrade;
                    System.out.println("1. Trade");
                    System.out.println("2. Go back\n\n");
                    System.out.print("> ");
                    choicetrade = sc.nextInt();
                    if(choicetrade == 1){
                        // code for trade option
                        System.out.println("Enter the id of the card you want to trade: ");
                        int cardid = sc.nextInt();
                        PreparedStatement trade = conn.prepareStatement("UPDATE cards SET tradable = 1 WHERE id = ?;");
                        trade.setInt(1, cardid);
                        trade.executeUpdate();
                        System.out.println("Card successfully sent to the market!");
                    }
                    else if(choicetrade == 2){
                        // code for go back option
                        System.out.println("Going back...");
                    }
                    else{
                        System.out.println("Invalid input!");
                    }
                }
                else if(choice3 == 2){
                    // code for deck option
                    System.out.println("Your deck: ");
                    PreparedStatement deck = conn.prepareStatement("SELECT * FROM cards WHERE user_id = ? AND in_deck = true");
                    deck.setString(1, username);
                    ResultSet rs = deck.executeQuery();
                    while(rs.next()) {
                        System.out.println("Name: "+rs.getString("name"));
                        System.out.println("Type: "+rs.getString("type"));
                        System.out.println("Element: "+rs.getString("element"));
                        System.out.println("Damage: "+rs.getString("damage"));
                    }
                    int choicedeck;
                    System.out.println("1. Reset Deck");
                    System.out.println("2. Go back\n\n");
                    System.out.print("> ");
                    choicedeck = sc.nextInt();
                    if(choicedeck == 1){
                        // code for trade option
                        PreparedStatement resetdeck = conn.prepareStatement("UPDATE cards SET in_deck = false WHERE user_id = ?;");
                        resetdeck.setString(1, username);
                        resetdeck.executeUpdate();
                        System.out.println("Deck successfully reset!");
                        System.out.println("Configure your deck: ");
                        PreparedStatement deck2 = conn.prepareStatement("SELECT * FROM cards WHERE user_id = ? AND in_deck = false");
                        deck2.setString(1, username);
                        ResultSet rs2 = deck2.executeQuery();
                        while(rs2.next()) {
                            System.out.println("Id: "+rs2.getString("id"));
                            System.out.println("Name: "+rs2.getString("name"));
                            System.out.println("Type: "+rs2.getString("type"));
                            System.out.println("Element: "+rs2.getString("element"));
                            System.out.println("Damage: "+rs2.getString("damage"));
                        }
                        System.out.println("Enter the id of the card you want to add to your deck: ");
                        int cardid = sc.nextInt(); // while till its not full
                        PreparedStatement addtodeck = conn.prepareStatement("UPDATE cards SET in_deck = true WHERE id = ?;");
                        addtodeck.setInt(1, cardid);

                    }
                    else if(choicedeck == 2){
                        // code for go back option
                        System.out.println("Going back...");
                    }
                    else{
                        System.out.println("Invalid input!");
                    }

                }
                else if(choice3 == 3){
                    // code for go back option
                    System.out.println("Going back...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // handle the exception
                    }
                }
                else {
                    System.out.println("Invalid input!");
                }


            }
            else if(choice2 == 3){
                // code for shop option
                int choice3;
                System.out.println("1. Buy");
                System.out.println("2. Trade");
                System.out.println("3. Go back\n\n");
                System.out.print("> ");
                choice3 = sc.nextInt();
                if(choice3 == 1){
                    // code for buy option
                    int choice4;
                    System.out.println("1. Package");
                    System.out.println("2. Cards");
                    System.out.println("3. Go back\n\n");
                    System.out.print("> ");
                    choice4 = sc.nextInt();
                    if(choice4 == 1){
                        // code for package option
                        System.out.println("How many packages do you want to buy?");
                        System.out.print("> ");
                        int packages = sc.nextInt();
                        if(user.getCoins() >= packages*5){
                            user.setCoins(user.getCoins() - packages*5);
                            Card[] cards = new Card[packages * 5];
                            for(int i = 0; i < packages * 5; i++) {
                                cards[i] = Card.generateCard();
                                String sql = "INSERT INTO cards (user_id, in_deck, name, damage, element, type) VALUES (?, ?, ?, ?, ?, ?)";


                            }

                            System.out.println("You bought " + packages + " packages!");
                            System.out.println("You now have " + user.getCoins() + " coins!");
                        }
                        else{
                            System.out.println("You don't have enough coins!");
                        }

                        System.out.println("Do you want to open the packages now?");
                        System.out.println("1. Yes");
                        System.out.println("2. No");
                        System.out.print("> ");
                        int choice5 = sc.nextInt();
                        if(choice5 == 1){
                            // code for opening packages
                            for(int i = 0; i < packages; i++){
                                System.out.println("Package: "+(i+1));
                                for(int j = 0; j < 5; j++){
                                    System.out.println("Card "+(j+1)+": ");
                                    //RestUser restUser = new RestUser(username);
                                    //restUser.buyPackage();
                                }
                            }
                        }
                        else if(choice5 == 2){
                            // code for go back option
                        }
                        else{
                            System.out.println("Invalid input!");
                        }


                    }
                    else if(choice4 == 2){
                        // code for cards option
                    }
                    else if(choice4 == 3){
                        // code for go back option
                    }
                    else{
                        System.out.println("Invalid input!");
                    }

                }
                else if(choice3 == 2){
                    // code for trade option
                }
                else if(choice3 == 3){
                    // code for go back option

                }
                else {
                    System.out.println("Invalid input!");
                }

            }
            else if(choice2 == 4){
                int choice3;
                System.out.println("1. Change Username");
                System.out.println("2. Change Password");
                System.out.println("3. Go back\n\n");
                System.out.print("> ");
                choice3 = sc.nextInt();
                if(choice3 == 1){
                    System.out.println("Enter your new username: ");
                    String newusername = sc.next();
                    PreparedStatement changeusername = conn.prepareStatement("UPDATE users SET username = ? WHERE username = ?;");
                    changeusername.setString(1, newusername);
                    changeusername.setString(2, username);
                    changeusername.executeUpdate();
                    System.out.println("Username successfully changed!");
                    username = newusername;

                }
                else if(choice3 == 2){
                    System.out.println("Enter your new password: ");
                    String newpassword = sc.next();
                    String hashedPassword = BCrypt.hashpw(newpassword, BCrypt.gensalt());
                    PreparedStatement changepassword = conn.prepareStatement("UPDATE users SET password = ? WHERE username = ?;");
                    changepassword.setString(1, hashedPassword);
                    changepassword.setString(2, username);
                    changepassword.executeUpdate();
                    System.out.println("Password successfully changed!");
                }
                else if(choice3 == 3){
                    // code for go back option
                    System.out.println("Going back...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // handle the exception
                    }
                }
                else {
                    System.out.println("Invalid input!");
                }
            }
            else if(choice2 == 5){
                // code for leaderboard option
                System.out.println("Leaderboard:");

                System.out.println("Most games won:");
                String sql = "SELECT username, wins FROM user ORDER BY wins DESC";
                PreparedStatement statement = conn.prepareStatement(sql);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String wusername = rs.getString("username");
                    int gamesWon = rs.getInt("wins");
                    System.out.println(wusername + ": " + gamesWon);
                }
                System.out.println("Best win ratio: ");
                String sql2 = "SELECT username, wins, losses FROM user ORDER BY wins/losses DESC";
                PreparedStatement statement2 = conn.prepareStatement(sql2);
                ResultSet rs2 = statement2.executeQuery();
                while (rs.next()) {
                    String wusername = rs.getString("username");
                    int gamesWon = rs.getInt("wins");
                    int gamesLost = rs.getInt("losses");
                    System.out.println(wusername + ": " + gamesWon/gamesLost+"%");
                }

            }
            else if(choice2 == 5){
                runAwayPrompt();
            }
            else {
                System.out.println("Invalid input!");
            }



        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
