package client_src;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class MainClient {
    public static Socket s;
    public static InputStream is;
    public static BufferedReader br;
    public static OutputStream os;
    public static BufferedWriter bw;
    public static Thread t;
    public static String username;
    public static LogonScreen logonScreen;
    public static MainScreen mainScreen;

    public static void main(String[] arg) {
        try {
            logonScreen = new LogonScreen();

            t = new Thread(() -> {
                while (true) {
                    try {
                        if (br.ready()) {
                            String receivedMessage = br.readLine();
                            System.out.println(">" + receivedMessage);
                            switch (receivedMessage) {
                                case "download": {
                                    // split at first space
                                    String fileName = br.readLine().split("_", 2)[1];
                                    long fileSize = Long.parseLong(br.readLine());
                                    System.out.println("Downloading file " + fileName + " with size " + fileSize);
                                    File file = new File(fileName);
                                    MainScreen.fileChooser.setSelectedFile(file);
                                    int ret = MainScreen.fileChooser.showSaveDialog(mainScreen);
                                    if (ret != JFileChooser.APPROVE_OPTION) {
                                        MainClient.bw.write("cancel download\n");
                                        MainClient.bw.flush();
                                        continue;
                                    } else {
                                        MainClient.bw.write("ok\n");
                                        MainClient.bw.flush();
                                    }
                                    Thread.sleep(150);
                                    file = MainScreen.fileChooser.getSelectedFile();
                                    if (file == null) {
                                        continue;
                                    }
                                    FileOutputStream fos = new FileOutputStream(file);
                                    // purposely rate limit the download
                                    byte[] buffer = new byte[2048];
                                    int length;
                                    while ((length = is.read(buffer)) > 0) {
                                        fos.write(buffer, 0, length);
                                        if (length < 2048) {
                                            break;
                                        }
                                    }
                                    fos.flush();
                                    fos.close();

                                    System.out.println("Downloaded file " + fileName);
                                    break;
                                }
                                case "message": {
                                    String roomId = br.readLine();
                                    String messageId = br.readLine();
                                    String author = br.readLine();
                                    String text = br.readLine();
                                    System.out.println(roomId + " " + messageId + " " + author + " " + text);
                                    Message message = new Message(Long.parseLong(messageId), text, author);

                                    Room room = MainScreen.rooms.stream().filter(r -> r.id.equals(Long.parseLong(roomId))).findFirst().orElse(null);

                                    if (room == null) {
                                        room = new Room(Long.parseLong(roomId), roomId);
                                        MainClient.bw.write("fetch rooms\n");
                                        MainClient.bw.flush();
                                    }
                                    room.addMessage(message);

                                    break;
                                }
                                case "messages": {
                                    int n = Integer.parseInt(br.readLine());
                                    for (int i = 0; i < n; i++) {
                                        String header = br.readLine();
                                        String roomId = br.readLine();
                                        String messageId = br.readLine();
                                        String author = br.readLine();
                                        String text = br.readLine();
                                        System.out.println(header + " " + roomId + " " + messageId + " " + author + " " + text);
                                        Message message = new Message(Long.parseLong(messageId), text, author);
                                        Room room = MainScreen.rooms.stream().filter(r -> r.id.equals(Long.parseLong(roomId))).findFirst().orElse(null);
                                        if (room == null) {
                                            room = new Room(Long.parseLong(roomId), roomId);
                                            MainClient.bw.write("fetch rooms\n");
                                            MainClient.bw.flush();
                                        }
                                        room.addMessage(message);
                                    }
                                    break;
                                }
                                case "online": {
                                    Integer n = Integer.parseInt(br.readLine());
                                    MainScreen.onlineUsersListModel.clear();
                                    MainScreen.onlineUsers.clear();
                                    System.out.println(n);
                                    for (int i = 0; i < n; i++) {
                                        String username = br.readLine();
                                        System.out.println(username);
                                        MainScreen.onlineUsers.add(username);
                                        MainScreen.onlineUsersListModel.addElement(username);
                                    }
                                    break;

                                }
                                case "rooms": {
                                    int n = Integer.parseInt(br.readLine());

                                    for (int i = 0; i < n; i++) {
                                        String roomId = br.readLine();
                                        String roomName = br.readLine();
                                        boolean isPrivate = Boolean.parseBoolean(br.readLine());
                                        System.out.println(roomId + " " + roomName);
                                        Room room = MainScreen.rooms.stream().filter(r -> r.id.equals(Long.parseLong(roomId))).findFirst().orElse(null);
                                        if (room == null) {
                                            room = new Room(Long.parseLong(roomId), roomName, isPrivate);
                                        }
                                        room.setName(roomName);
                                        room.isPrivate = isPrivate;
                                        MainClient.bw.write("fetch members\n");
                                        MainClient.bw.write(roomId + "\n");
                                        MainClient.bw.flush();
                                    }
                                    break;
                                }
                                case "members": {
                                    String roomId = br.readLine();
                                    Room room = MainScreen.rooms.stream().filter(r -> r.id.equals(Long.parseLong(roomId))).findFirst().orElse(null);
                                    if (room == null) {
                                        room = new Room(Long.parseLong(roomId), roomId);
                                    }
                                    int n = Integer.parseInt(br.readLine());
                                    for (int i = 0; i < n; i++) {
                                        String username = br.readLine();
                                        System.out.println(roomId + " " + username);
                                        room.addMember(username);
                                    }
                                    break;
                                }
                            }
                        }
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

