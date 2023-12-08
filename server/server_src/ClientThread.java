package server_src;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientThread extends Thread {
    public Socket socket;
    public BufferedReader receiver;
    public BufferedWriter sender;

    public ClientThread(Socket socket) throws IOException {
        this.socket = socket;
        receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        sender = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public static void SendMessage(ClientThread clientThread, Room room, Message message) {
        try {
            clientThread.sender.write("message");
            clientThread.sender.newLine();
            clientThread.sender.write(room.id.toString());
            clientThread.sender.newLine();
            clientThread.sender.write(message.id.toString());
            clientThread.sender.newLine();
            clientThread.sender.write(message.author);
            clientThread.sender.newLine();
            clientThread.sender.write(message.text);
            clientThread.sender.newLine();
            clientThread.sender.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void onlineUsers() {
        try {
            for (ClientThread clientThread : MainServer.clients.values()) {
                clientThread.sender.write("online");
                clientThread.sender.newLine();
                clientThread.sender.write(String.valueOf(MainServer.clients.size() - 1));
                System.out.println(MainServer.clients.size() - 1);
                clientThread.sender.newLine();
                clientThread.sender.flush();
                for (String user : MainServer.clients.keySet()) {
                    if (!user.equals(MainServer.clients.entrySet().stream().filter(entry -> entry.getValue().equals(clientThread)).findFirst().get().getKey())) {
                        clientThread.sender.write(user);
                        System.out.println(user);
                        clientThread.sender.newLine();
                        clientThread.sender.flush();
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void run() {
        try {
            while (true) {
                String rp = receiver.readLine();

                if (rp == null) {
                    break;
                }

                System.out.println(rp);
                try {
                    switch (rp) {
                        case "quit":
                            System.out.println("src.Client has left !");
                            break;
                        case "login":
                            System.out.println("src.Client is logging in");
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
                                sender.write("login success");
                            } else {
                                sender.write("login failed");
                            }
                            sender.newLine();
                            sender.flush();
                            MainServer.clients.put(username, this);
                            for (ClientThread clientThread : MainServer.clients.values()) {
                                clientThread.sender.write("online");
                                clientThread.sender.newLine();
                                clientThread.sender.write(String.valueOf(MainServer.clients.size() - 1));
                                clientThread.sender.newLine();
                                clientThread.sender.flush();
                                for (String user : MainServer.clients.keySet()) {
                                    if (!user.equals(MainServer.clients.entrySet().stream().filter(entry -> entry.getValue().equals(clientThread)).findFirst().get().getKey())) {
                                        clientThread.sender.write(user);
                                        clientThread.sender.newLine();
                                        clientThread.sender.flush();
                                    }
                                }
                            }

                            break;
                        case "register":
                            System.out.println("src.Client is registering");
                            username = receiver.readLine();
                            password = receiver.readLine();

                            System.out.println(username + " " + password);
                            br = new BufferedReader(new FileReader("user.txt"));
                            found = false;
                            while ((line = br.readLine()) != null) {
                                if (line.startsWith(username + ";")) {
                                    found = true;
                                    break;
                                }
                            }
                            if (found) {
                                sender.write("register failed");
                            } else {
                                sender.write("register success");
                                BufferedWriter bw = new BufferedWriter(new FileWriter("user.txt", true));
                                bw.write(username + ";" + password);
                                bw.newLine();
                                bw.flush();
                                bw.close();
                            }
                            sender.newLine();
                            sender.flush();
                            break;
                        case "message":
                            Long roomId = Long.parseLong(receiver.readLine());
                            String senderName = MainServer.clients.entrySet().stream().filter(entry -> entry.getValue().equals(this)).findFirst().get().getKey();
                            String message = receiver.readLine();
//                        while(receiver.ready()) {
//                            message += "\n" + receiver.readLine();
//                        }
                            System.out.println("src.Message: " + message);

                            Message newMessage = new Message(message, senderName);

                            for (server_src.Room room : MainServer.rooms) {
                                if (room.id.equals(roomId)) {
                                    room.messages.add(new Message(message, senderName));
                                    for (String user : room.users) {
                                        if (!user.equals(senderName)) {
                                            SendMessage(MainServer.clients.get(user), room, newMessage);
                                        }
                                    }
                                    break;
                                }
                            }
                            break;
                        case "create room":
                            String roomCreator = MainServer.clients.entrySet().stream().filter(entry -> entry.getValue().equals(this)).findFirst().get().getKey();
                            String roomName = receiver.readLine();
                            Integer numberOfUsers = Integer.parseInt(receiver.readLine());
                            ArrayList<String> users = new ArrayList<String>();
                            for (int i = 0; i < numberOfUsers; i++) {
                                users.add(receiver.readLine());
                            }
                            users.add(roomCreator);

                            server_src.Room newRoom = new server_src.Room(roomName, users);
                            MainServer.rooms.add(newRoom);
                            for (String user : users) {
                                SendMessage(MainServer.clients.get(user), newRoom, new Message("Room created", roomCreator));
                            }
                            break;
                        case "upload":
                            roomId = Long.parseLong(receiver.readLine());
                            senderName = MainServer.clients.entrySet().stream().filter(entry -> entry.getValue().equals(this)).findFirst().get().getKey();
                            String fileName = receiver.readLine();
                            Long fileSize = Long.parseLong(receiver.readLine());
                            Message messageUpload = new Message("File uploaded: " + fileName + " (" + fileSize + " bytes)", senderName);
                            FileOutputStream fos = new FileOutputStream(messageUpload.id.toString() + " " + fileName);
                            byte[] buffer = new byte[1024];
                            int length;
                            while (fileSize > 0 && (length = socket.getInputStream().read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                                fos.write(buffer, 0, length);
                                fileSize -= length;
                            }
                            fos.close();
                            for (server_src.Room room : MainServer.rooms) {
                                if (room.id.equals(roomId)) {
                                    room.messages.add(messageUpload);
                                    for (String user : room.users) {
                                        if (!user.equals(senderName)) {
                                            SendMessage(MainServer.clients.get(user), room, messageUpload);
                                        }
                                    }
                                    break;
                                }
                            }
                            break;
                        case "download":
                            String id = receiver.readLine();
                            for (File file : new File(".").listFiles()) {
                                if (file.getName().startsWith(id)) {
                                    System.out.println("Sending file " + file.getName());
                                    sender.write("download");
                                    sender.newLine();
                                    sender.write(file.getName());
                                    sender.newLine();
                                    sender.write(String.valueOf(file.length()));
                                    sender.newLine();
                                    sender.flush();
                                    FileInputStream fis = new FileInputStream(file);
                                    buffer = new byte[1024];
                                    while ((length = fis.read(buffer)) > 0) {
                                        socket.getOutputStream().write(buffer, 0, length);
                                    }
                                    socket.getOutputStream().flush();
                                    fis.close();
                                    break;
                                }
                            }
                            break;
                        case "invite":
                            String inviter = MainServer.clients.entrySet().stream().filter(entry -> entry.getValue().equals(this)).findFirst().get().getKey();
                            String invitee = receiver.readLine();
                            roomId = Long.parseLong(receiver.readLine());
                            for (server_src.Room room : MainServer.rooms) {
                                if (room.id.equals(roomId)) {
                                    room.users.add(invitee);
                                    for (String user : room.users) {
                                        if (!user.equals(inviter)) {
                                            SendMessage(MainServer.clients.get(user), room, new Message(invitee + " has joined the room", inviter));
                                        }
                                    }
                                    break;
                                }
                            }
                            break;
                        case "sync":
                            String usernameSync = MainServer.clients.entrySet().stream().filter(entry -> entry.getValue().equals(this)).findFirst().get().getKey();
                            for (server_src.Room room : MainServer.rooms) {
                                System.out.println(room.id);
                                if (room.users.contains(usernameSync)) {
                                    for (Message msg : room.messages) {
                                        System.out.println(msg.id);
                                        SendMessage(this, room, msg);
                                    }
                                }
                            }
                            break;
                        case "online":
                            String usernameOnline = MainServer.clients.entrySet().stream().filter(entry -> entry.getValue().equals(this)).findFirst().get().getKey();
                            sender.write("online");
                            sender.newLine();
                            sender.write(String.valueOf(MainServer.clients.size() - 1));
                            sender.newLine();
                            sender.flush();
                            for (String user : MainServer.clients.keySet()) {
                                if (!user.equals(usernameOnline)) {
                                    sender.write(user);
                                    sender.newLine();
                                    sender.flush();
                                }
                            }
                            break;
                        case "fetch rooms":
                            String usernameFetch = MainServer.clients.entrySet().stream().filter(entry -> entry.getValue().equals(this)).findFirst().get().getKey();
                            sender.write("rooms");
                            sender.newLine();
                            sender.write(String.valueOf(MainServer.rooms.stream().filter(room -> room.users.contains(usernameFetch)).count()));
                            sender.newLine();
                            sender.flush();
                            for (server_src.Room room : MainServer.rooms) {
                                if (room.users.contains(usernameFetch)) {
                                    sender.write(room.id.toString());
                                    sender.newLine();
                                    sender.write(room.name);
                                    sender.newLine();
                                    sender.flush();
                                }
                            }
                            break;
                        case "fetch users":
                            roomId = Long.parseLong(receiver.readLine());
                            sender.write("users");
                            sender.newLine();
                            sender.write(String.valueOf(MainServer.rooms.stream().filter(room -> room.id.equals(roomId)).findFirst().get().users.size()));
                            sender.newLine();
                            sender.flush();
                            for (server_src.Room room : MainServer.rooms) {
                                if (room.id.equals(roomId)) {
                                    for (String user : room.users) {
                                        sender.write(user);
                                        sender.newLine();
                                        sender.flush();
                                    }
                                    break;
                                }
                            }
                            break;
                        case "fetch messages":
                            roomId = Long.parseLong(receiver.readLine());
                            sender.write("messages");
                            sender.newLine();
                            sender.write(String.valueOf(MainServer.rooms.stream().filter(room -> room.id.equals(roomId)).findFirst().get().messages.size()));
                            sender.newLine();
                            for (server_src.Room room : MainServer.rooms) {
                                if (room.id.equals(roomId)) {
                                    for (Message msg : room.messages) {
                                        SendMessage(this, room, msg);
                                    }
                                    break;
                                }
                            }
                            sender.flush();
                            break;

                        default:
                            System.out.println("Received : " + rp);
                            break;
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }


            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            MainServer.clients.remove(MainServer.clients.entrySet().stream().filter(entry -> entry.getValue().equals(this)).findFirst().get().getKey());
            onlineUsers();
        }
    }
}
