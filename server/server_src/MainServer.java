package server_src;

import java.io.File;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class MainServer {
    public static volatile ArrayList<server_src.Room> rooms = new ArrayList<>();
    public static volatile HashMap<String, ClientThread> clients = new HashMap<>();
    public static ServerSocket serverSocket;

    public static void save() {
        // save rooms as object
        ObjectOutputStream roomsObjectOutputStream = null;
        try {
            roomsObjectOutputStream = new ObjectOutputStream(new java.io.FileOutputStream("rooms.dat"));
            roomsObjectOutputStream.writeObject(rooms);
            roomsObjectOutputStream.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void load() {
        // load rooms from object
        try {
            java.io.FileInputStream fileInputStream = new java.io.FileInputStream("rooms.dat");
            java.io.ObjectInputStream objectInputStream = new java.io.ObjectInputStream(fileInputStream);
            rooms = (ArrayList<server_src.Room>) objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            File userFile = new File("user.txt");
            userFile.createNewFile();
            userFile.setWritable(true);

            load();

            serverSocket = new ServerSocket(3200);
            System.out.println("Server is running on port 3200");
            // create a thread that saves the rooms every 3 seconds
            Thread saveThread = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(3000);
                        save();
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
            });
            saveThread.start();

            //noinspection InfiniteLoopStatement
            while (true) {
                Socket socket = serverSocket.accept();
                ClientThread clientThread = new ClientThread(socket);
                clientThread.start();
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}