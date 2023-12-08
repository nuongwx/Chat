package server_src;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class MainServer {
    public static volatile ArrayList<server_src.Room> rooms = new ArrayList<server_src.Room>();
    public static volatile HashMap<String, ClientThread> clients = new HashMap<String, ClientThread>();
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(3200);
            System.out.println("Server is running on port 3200");
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