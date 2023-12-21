package server_src;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class MainServer {
    public static volatile ArrayList<server_src.Room> rooms = new ArrayList<>();
    public static volatile HashMap<String, ClientThread> clients = new HashMap<>();
    public static ServerSocket serverSocket;
    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(3200);
            System.out.println("Server is running on port 3200");
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