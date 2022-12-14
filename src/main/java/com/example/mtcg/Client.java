package com.example.mtcg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;
import org.mindrot.jbcrypt.BCrypt;

import static java.lang.Thread.sleep;

public class Client {


    public static void main(String[] args) {

        String host = "localhost";
        int port = 5432;

        // Set the POSTGRESQL database credentials
        String url = "jdbc:postgresql://localhost:5432/mtcg";
        String dbUsername = "postgres";
        String dbPassword = "postgres";

        System.out.println("Welcome to the Monster Trading Card Game!");
        System.out.println("------------------------------------------\n\n");

        //menu for the user
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
            System.out.println("Username: ");
            String username = stdIn.readLine();
            System.out.println("Password: ");
            String password = stdIn.readLine();

            Connection conn = DriverManager.getConnection(url, "postgres", "postgres");

            if(choice == 2) {

                //hash the password
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

                PreparedStatement register = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
                register.setString(1, username);
                register.setString(2, hashedPassword);
                register.executeUpdate();
                System.out.println("Registration successful!");
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
                    sleep(1000);
                }
                else {
                    System.out.println("Invalid input!");
                }
            }
            else {
                System.out.println("Invalid input!");
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
