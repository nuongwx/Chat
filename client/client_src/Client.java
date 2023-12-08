package client_src;

import server_src.Room;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class Client extends Thread {
    public Socket socket;
    public BufferedReader receiver;
    public BufferedWriter sender;

    public String username;

    public List<Room> rooms = new ArrayList<Room>();
    public List<String> users = new ArrayList<String>();

    public Client() {
        try {
            socket = new Socket("localhost", 1234);
            receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            sender = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void run() {
        try {
            while (true) {
                String rp = null;
                try {
                    rp = receiver.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (rp == null) {
                    break;
                }

                switch (rp) {
                    case "quit":
                        System.out.println("src.Client has left !");
                        break;
                    case "login":
                        System.out.println("src.Client is logging in");
//                        break;
                        String username = receiver.readLine();
                        String password = receiver.readLine();

                        System.out.println(username + " " + password);

                        BufferedReader br = new BufferedReader(new FileReader("user.txt"));
                        String line;
                        boolean found = false;
                        while ((line = br.readLine()) != null) {
                            if (line.equals(username + ";" + password)) {
                                found = true;
                                break;
                            }
                        }

                        if (found) {
                            System.out.println("Login success");
                            this.username = username;
                            sender.write("login success");
                            sender.newLine();
                            sender.flush();
                        } else {
                            System.out.println("Login failed");
                            sender.write("login failed");
                            sender.newLine();
                            sender.flush();
                        }
                        break;
                    case "register":
                        System.out.println("src.Client is registering");
                        String username2 = receiver.readLine();
                        String password2 = receiver.readLine();

                        System.out.println(username2 + " " + password2);

                        BufferedReader br2 = new BufferedReader(new FileReader("user.txt"));
                        String line2;
                        boolean found2 = false;
                        while ((line2 = br2.readLine()) != null) {
                            if (line2.equals(username2 + ";" + password2)) {
                                found2 = true;
                                break;
                            }
                        }

                        if (found2) {
                            System.out.println("Register failed");
                            sender.write("register failed");
                            sender.newLine();
                            sender.flush();
                        } else {
                            System.out.println("Register success");
                            sender.write("register success");
                            sender.newLine();
                            sender.flush();

                            BufferedWriter bw = new BufferedWriter(new FileWriter("user.txt", true));
                            bw.write(username2 + ";" + password2);
                            bw.newLine();
                            bw.flush();
                        }
                        break;
                    case "create room":
                        System.out.println("src.Client is creating room");
                        String roomName = receiver.readLine();
                        System.out.println(roomName);

                        sender.write("create room");
                        sender.newLine();
                        sender.flush();

                        sender.write(roomName);
                        sender.newLine();
                        sender.flush();

                        String response = receiver.readLine();
                        if (response.equals("create room success")) {
                            System.out.println("Create room success");
                        } else {
                            System.out.println("Create room failed");
                        }
                        break;
                    case "join room":
                        System.out.println("src.Client is joining room");
                        String roomId = receiver.readLine();
                        System.out.println(roomId);

                        sender.write("join room");
                        sender.newLine();
                        sender.flush();
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };
}