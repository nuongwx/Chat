package server_src;

import client_src.MainClient;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class ClientThread extends Thread {
    public Socket socket;

    public InputStream inputStream;
    public OutputStream outputStream;
    public BufferedReader receiver;
    public BufferedWriter sender;

//    public DataInputStream dis;
//    public DataOutputStream dos;

    public ClientThread(Socket socket) throws IOException {
        this.socket = socket;
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        receiver = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        sender = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
//        dis = new DataInputStream(socket.getInputStream());
//        dos = new DataOutputStream(socket.getOutputStream());
    }

    public static void SendMessage(ClientThread clientThread, Room room, Message message) {
        if (clientThread == null) {
            return;
        }
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
        if (!room.messages.contains(message)) {
            room.messages.add(message);
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
                                    room.messages.add(newMessage);
                                    for (String user : room.users) {
//                                        if (!user.equals(senderName)) {
                                        SendMessage(MainServer.clients.get(user), room, newMessage);
//                                        }
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

                            Room newRoom = new Room(roomName, users);
                            MainServer.rooms.add(newRoom);
                            newMessage = new Message("Room created", roomCreator);
                            for (String user : users) {
                                SendMessage(MainServer.clients.get(user), newRoom, newMessage);
                            }
                            break;
                        case "upload":
                            roomId = Long.parseLong(receiver.readLine());
                            String name = MainServer.clients.entrySet().stream().filter(entry -> entry.getValue().equals(this)).findFirst().get().getKey();
                            String fileName = receiver.readLine();
                            Long fileSize = Long.parseLong(receiver.readLine());
                            // ​ marker
                            Message messageUpload = new Message("\u200BFile uploaded: " + fileName + " (" + fileSize + " bytes)", name);
                            FileOutputStream fos = new FileOutputStream(messageUpload.id + "_" + fileName);
                            byte[] buffer = new byte[4096];
                            int length = 0;
                            if (fileSize > 0) {
                                while ((length = inputStream.read(buffer)) > 0) {
                                    fos.write(buffer, 0, length);
                                    fileSize -= length;
                                    if (fileSize == 0) {
                                        break;
                                    }
                                }
                            }
                            System.out.println("File uploaded: " + fileName);
                            fos.close();
                            for (server_src.Room room : MainServer.rooms) {
                                if (room.id.equals(roomId)) {
                                    room.messages.add(messageUpload);
                                    for (String user : room.users) {
                                        SendMessage(MainServer.clients.get(user), room, messageUpload);
                                    }
                                    break;
                                }
                            }
                            receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
                                    sender.flush();
                                    Thread.sleep(150);
                                    if (!receiver.readLine().equalsIgnoreCase("ok")) {
                                        System.out.println("Download cancelled");
                                        break;
                                    }
                                    FileInputStream fis = new FileInputStream(file);
                                    byte[] bufferDownload = new byte[4096];
                                    int lengthDownload = 0;
                                    if (file.length() > 0) {
                                        while ((lengthDownload = fis.read(bufferDownload)) > 0) {
                                            outputStream.write(bufferDownload, 0, lengthDownload);
                                        }
                                    }
                                    outputStream.flush();
                                    fis.close();

                                    break;
                                }
                            }
                            break;
                        case "invite":
                            String inviter = MainServer.clients.entrySet().stream().filter(entry -> entry.getValue().equals(this)).findFirst().get().getKey();
                            roomId = Long.parseLong(receiver.readLine());
                            Integer numberOfInvitees = Integer.parseInt(receiver.readLine());
                            ArrayList<String> invitees = new ArrayList<String>();
                            for (int i = 0; i < numberOfInvitees; i++) {
                                invitees.add(receiver.readLine());
                            }

                            for (String invitee : invitees) {
                                Room room = MainServer.rooms.stream().filter(r -> r.id.equals(roomId)).findFirst().get();
                                room.users.add(invitee);
//                                SendMessage(MainServer.clients.get(invitee), room, new Message(inviter + " has invited you to join the room", inviter));
                            }
//                            SendMessage(this, MainServer.rooms.stream().filter(r -> r.id.equals(roomId)).findFirst().get(), new Message("Invitation sent", inviter));
                            newMessage = new Message(inviter + " has invited " + invitees.stream().reduce((a, b) -> a + ", " + b).get() + " to join the room", inviter);
                            for (String member : MainServer.rooms.stream().filter(r -> r.id.equals(roomId)).findFirst().get().users) {
                                SendMessage(MainServer.clients.get(member), MainServer.rooms.stream().filter(r -> r.id.equals(roomId)).findFirst().get(), newMessage);
                            }

                            break;

//                            for (server_src.Room room : MainServer.rooms) {
//                                if (room.id.equals(roomId)) {
//                                    room.users.add(invitee);
//                                    for (String user : room.users) {
////                                        if (!user.equals(inviter)) {
//
//                                          if (MainServer.clients.containsKey(user)) {
//                                              SendMessage(MainServer.clients.get(user), room, new Message(invitee + " has joined the room", inviter));
//                                          }
////                                        }
//                                    }
//                                    break;
//                                }
//                            }
//                            break;
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
                        case "fetch members":
                            roomId = Long.parseLong(receiver.readLine());
                            sender.write("members");
                            sender.newLine();
                            sender.write(roomId.toString());
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
                        case "delete message":
                            roomId = Long.parseLong(receiver.readLine());
                            System.out.println("Room id" + roomId.toString());
                            Long messageId = Long.parseLong(receiver.readLine());
                            System.out.println("Message id" + messageId.toString());
                            for (server_src.Room room : MainServer.rooms) {
                                if (room.id.equals(roomId)) {
                                    for (Message msg : room.messages) {
                                        if (msg.id.equals(messageId)) {
                                            msg.text = "deleted";
                                            for(String user : room.users) {
                                                SendMessage(MainServer.clients.get(user), room, msg);
                                            }
//                                            SendMessage(this, room, msg);
//                                          delete the message from the server
                                            room.messages.remove(msg);

                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
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
