package server_src;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

public class ClientThread extends Thread {
    public Socket socket;
    public String username;
    public InputStream inputStream;
    public OutputStream outputStream;
    public BufferedReader receiver;
    public BufferedWriter sender;


    public ClientThread(Socket socket) throws IOException {
        this.socket = socket;
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        receiver = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        sender = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
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
                    if (!user.equals(clientThread.username)) {
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
                        case "quit": {
                            System.out.println("src.Client has left !");
                            break;
                        }
                        case "login": {
                            System.out.println("src.Client is logging in");
                            String username = receiver.readLine();
                            String password = receiver.readLine();

                            BufferedReader br = new BufferedReader(new FileReader("user.txt"));
                            String line;
                            boolean found = false;
                            while ((line = br.readLine()) != null) {
                                System.out.println(line);
                                if (line.equals(username + ";" + password)) {
                                    found = true;
                                    break;
                                }
                            }
                            br.close();
                            if (found) {
                                sender.write("login success");
                                sender.newLine();
                                sender.flush();
                            } else {
                                sender.write("login failed");
                                sender.newLine();
                                sender.flush();
                                break;
                            }
                            MainServer.clients.put(username, this);
                            this.username = username;
                            for (ClientThread clientThread : MainServer.clients.values()) {
                                clientThread.sender.write("online");
                                clientThread.sender.newLine();
                                clientThread.sender.write(String.valueOf(MainServer.clients.size() - 1));
                                clientThread.sender.newLine();
                                clientThread.sender.flush();
                                for (String user : MainServer.clients.keySet()) {
                                    if (!user.equals(clientThread.username)) {
                                        clientThread.sender.write(user);
                                        clientThread.sender.newLine();
                                        clientThread.sender.flush();
                                    }
                                }
                            }

                            break;
                        }
                        case "register": {
                            System.out.println("src.Client is registering");
                            String username = receiver.readLine();
                            String password = receiver.readLine();

                            System.out.println(username + " " + password);
                            BufferedReader br = new BufferedReader(new FileReader("user.txt"));
                            boolean found = false;
                            String line;
                            while ((line = br.readLine()) != null) {
                                if (line.startsWith(username + ";")) {
                                    found = true;
                                    break;
                                }
                            }
                            br.close();
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
                        }
                        case "message": {
                            Long roomId = Long.parseLong(receiver.readLine());
                            String senderName = username;
                            String message = receiver.readLine();
                            System.out.println("src.Message: " + message);

                            Room room = MainServer.rooms.stream().filter(r -> r.id.equals(roomId)).findFirst().orElse(null);
                            if (room == null) {
                                break;
                            }

                            Message newMessage = new Message(message, senderName);

                            room.messages.add(newMessage);
                            for (String user : room.users) {
                                SendMessage(MainServer.clients.get(user), room, newMessage);
                            }
                            break;
                        }
                        case "create room": {
                            String roomCreator = username;
                            String roomName = receiver.readLine();
                            boolean isPrivate = Boolean.parseBoolean(receiver.readLine());
                            int numberOfUsers = Integer.parseInt(receiver.readLine());
                            ArrayList<String> users = new ArrayList<>();
                            for (int i = 0; i < numberOfUsers; i++) {
                                users.add(receiver.readLine());
                            }
                            users.add(roomCreator);

                            Room newRoom = new Room(roomName, users, isPrivate);
                            MainServer.rooms.add(newRoom);
                            Message newMessage = new Message("Room created", roomCreator);
                            for (String user : users) {
                                SendMessage(MainServer.clients.get(user), newRoom, newMessage);
                            }
                            break;
                        }
                        case "upload": {
                            Long roomId = Long.parseLong(receiver.readLine());
                            String name = username;
                            String fileName = receiver.readLine();
                            long fileSize = Long.parseLong(receiver.readLine());
                            // ​ marker
                            Message messageUpload = new Message("\u200BFile uploaded: " + fileName + " (" + fileSize + " bytes)", name);
                            FileOutputStream fos = new FileOutputStream(messageUpload.id + "_" + fileName);
                            byte[] buffer = new byte[4096];
                            int length;
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
                            Room room = MainServer.rooms.stream().filter(r -> r.id.equals(roomId)).findFirst().orElse(null);
                            if (room == null) {
                                break;
                            }

                            room.messages.add(messageUpload);
                            for (String user : room.users) {
                                SendMessage(MainServer.clients.get(user), room, messageUpload);
                            }

                            break;
                        }
                        case "download": {
                            String id = receiver.readLine();
                            for (File file : Objects.requireNonNull(new File(".").listFiles())) {
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
                                    int lengthDownload;
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
                        }
                        case "invite": {
                            String inviter = username;
                            Long roomId = Long.parseLong(receiver.readLine());
                            int numberOfInvitees = Integer.parseInt(receiver.readLine());
                            ArrayList<String> invitees = new ArrayList<>();
                            for (int i = 0; i < numberOfInvitees; i++) {
                                invitees.add(receiver.readLine());
                            }
                            Room room = MainServer.rooms.stream().filter(r -> r.id.equals(roomId)).findFirst().orElse(null);
                            if (room == null) {
                                break;
                            }
                            room.users.addAll(invitees);
                            Message newMessage = new Message(inviter + " has invited " + invitees.stream().reduce((a, b) -> a + ", " + b).orElse("") + " to join the room", inviter);
                            for (String member : room.users) {
                                SendMessage(MainServer.clients.get(member), room, newMessage);
                            }

                            break;
                        }
                        case "sync": {
                            String usernameSync = username;
                            for (Room room : MainServer.rooms) {
                                System.out.println(room.id);
                                if (room.users.contains(usernameSync)) {
                                    for (Message msg : room.messages) {
                                        System.out.println(msg.id);
                                        SendMessage(this, room, msg);
                                    }
                                }
                            }
                            break;
                        }
                        case "online": {
                            String usernameOnline = username;
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
                        }
                        case "fetch rooms": {
                            String usernameFetch = username;
                            sender.write("rooms");
                            sender.newLine();
                            sender.write(String.valueOf(MainServer.rooms.stream().filter(room -> room.users.contains(usernameFetch)).count()));
                            sender.newLine();
                            sender.flush();
                            for (Room room : MainServer.rooms) {
                                if (room.users.contains(usernameFetch)) {
                                    sender.write(room.id.toString());
                                    sender.newLine();
                                    sender.write(room.name);
                                    sender.newLine();
                                    sender.write(String.valueOf(room.isPrivate));
                                    sender.newLine();
                                    sender.flush();
                                }
                            }
                            break;
                        }
                        case "fetch members": {
                            Long roomId = Long.parseLong(receiver.readLine());
                            Room room = MainServer.rooms.stream().filter(r -> r.id.equals(roomId)).findFirst().orElse(null);
                            if (room == null) {
                                break;
                            }
                            sender.write("members");
                            sender.newLine();
                            sender.write(roomId.toString());
                            sender.newLine();
                            sender.write(String.valueOf(room.users.size()));
                            sender.newLine();
                            sender.flush();
                            for (String user : room.users) {
                                sender.write(user);
                                sender.newLine();
                                sender.flush();
                            }
                            break;
                        }
                        case "fetch messages": {
                            Long roomId = Long.parseLong(receiver.readLine());
                            Room room = MainServer.rooms.stream().filter(r -> r.id.equals(roomId)).findFirst().orElse(null);
                            if (room == null) {
                                break;
                            }
                            sender.write("messages");
                            sender.newLine();
                            sender.write(String.valueOf(room.messages.size()));
                            sender.newLine();
                            for (Message msg : room.messages) {
                                SendMessage(this, room, msg);
                            }
                            sender.flush();
                            break;
                        }
                        case "delete message": {
                            Long roomId = Long.parseLong(receiver.readLine());
                            System.out.println("Room id" + roomId);
                            Long messageId = Long.parseLong(receiver.readLine());
                            System.out.println("Message id" + messageId);

                            Room room = MainServer.rooms.stream().filter(r -> r.id.equals(roomId)).findFirst().orElse(null);
                            if (room == null) {
                                break;
                            }
                            Message msg = room.messages.stream().filter(m -> m.id.equals(messageId)).findFirst().orElse(null);
                            if (msg == null) {
                                break;
                            }
                            msg.text = "deleted";
                            for (String user : room.users) {
                                SendMessage(MainServer.clients.get(user), room, msg);
                            }
                            room.messages.remove(msg);

                            break;
                        }

                        default:
                            System.out.println("Received : " + rp);
                            break;
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }


            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            MainServer.clients.remove(username);
            onlineUsers();
        }
    }
}
