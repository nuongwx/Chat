package client_src;

import com.sun.tools.javac.Main;

import java.io.*;
import java.net.*;

public class MainClient
{
    public static Socket s;
    public static InputStream is;
    public static BufferedReader br;
    public static OutputStream os;
    public static BufferedWriter bw;
    
    public static void main(String arg[])
    {
        try
        {
            s = new Socket("localhost",3200);
            System.out.println(s.getPort());

            is=s.getInputStream();
            br=new BufferedReader(new InputStreamReader(is));

            os=s.getOutputStream();
            bw = new BufferedWriter(new OutputStreamWriter(os));

            String sentMessage="";

     		System.out.println("Talking to Server");

             LogonScreen logonScreen = new LogonScreen();

             MainScreen mainScreen = new MainScreen();

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true) {
                        try {
                            if (br.ready()) {
                                String receivedMessage = br.readLine();
                                System.out.println(">" + receivedMessage);
                                if(receivedMessage.equalsIgnoreCase("download")) {
                                    String fileName = br.readLine();
                                    String fileSize = br.readLine();
                                    System.out.println("Downloading file " + fileName + " with size " + fileSize);
                                    FileOutputStream fos = new FileOutputStream(fileName);
                                    if(!fileSize.equals("0"))
                                    {
                                    byte[] buffer = new byte[1024];
                                    int length;
                                    while ((length = is.read(buffer)) > 0) {
                                        System.out.println(length);
                                        fos.write(buffer, 0, length);
                                        if(length < 1024) break;
                                    }
                                    }
                                    fos.flush();
                                    fos.close();
                                    System.out.println("Downloaded file " + fileName);
                                }
                                if(receivedMessage.equalsIgnoreCase("message")) {
                                    String roomId = br.readLine();
                                    String messageId = br.readLine();
                                    String author = br.readLine();
                                    String text = br.readLine();
                                    System.out.println(roomId + " " + messageId + " " + author + " " + text);

                                    if(MainScreen.rooms.stream().filter(room -> room.id.equals(Long.parseLong(roomId))).count() == 0) {
                                        MainClient.bw.write("fetch rooms\n");
                                        MainClient.bw.flush();

                                    }
                                }
                                if(receivedMessage.equalsIgnoreCase("messages")) {
                                    Integer n = Integer.parseInt(br.readLine());
                                    for(int i = 0; i < n; i++) {
                                        String header = br.readLine();
                                        String roomId = br.readLine();
                                        String messageId = br.readLine();
                                        String author = br.readLine();
                                        String text = br.readLine();
//                                        mainScreen.roomsPanel.addMessage(new Message(Long.parseLong(messageId), text, author));
                                        System.out.println(header + " " + roomId + " " + messageId + " " + author + " " + text);
                                    }
                                }
                                if(receivedMessage.equalsIgnoreCase("online")) {
                                    Integer n = Integer.parseInt(br.readLine());
                                    MainScreen.onlineUsersListModel.clear();
                                    System.out.println(n);
                                    for(int i = 0; i < n; i++) {
                                        String username = br.readLine();
                                        System.out.println(username);
                                        MainScreen.onlineUsers.add(username);
                                        MainScreen.onlineUsersListModel.addElement(username);
                                    }

                                }
                                if(receivedMessage.equalsIgnoreCase("rooms")) {
                                    Integer n = Integer.parseInt(br.readLine());
                                    MainScreen.rooms.clear();
                                    for(int i = 0; i < n; i++) {
                                        String roomId = br.readLine();
                                        String roomName = br.readLine();
                                        System.out.println(roomId + " " + roomName);
                                        MainScreen.rooms.add(new Room(Long.parseLong(roomId), roomName));
                                        MainScreen.roomsListModel.addElement(roomName);
                                    }
                                }
//                                else if(receivedMessage.equalsIgnoreCase("rooms")) {
//                                    Integer n = Integer.parseInt(br.readLine());
//                                    for(int i = 0; i < n; i++) {
//                                        String roomId = br.readLine();
//                                        String roomName = br.readLine();
//                                        System.out.println(roomId + " " + roomName);
//                                        mainScreen.rooms.add(new Room(Long.parseLong(roomId), roomName));
//                                    }
//                                    mainScreen.refresh();
//                                }
//                                else if(receivedMessage.equalsIgnoreCase("room")) {
//                                    String roomId = br.readLine();
//                                    String roomName = br.readLine();
//                                    System.out.println(roomId + " " + roomName);
//                                    mainScreen.rooms.add(new Room(Long.parseLong(roomId), roomName));
//                                    mainScreen.refresh();
//                                }
//                                else if(receivedMessage.equalsIgnoreCase("message")) {
//                                    String roomId = br.readLine();
//                                    String messageId = br.readLine();
//                                    String author = br.readLine();
//                                    String text = br.readLine();
//                                    System.out.println(roomId + " " + messageId + " " + author + " " + text);
//                                    mainScreen.roomsPanel.addMessage(new Message(Long.parseLong(messageId), text, author));
//                                }
//                                else if(receivedMessage.equalsIgnoreCase("roomusers")) {
//                                    String roomId = br.readLine();
//                                    Integer n = Integer.parseInt(br.readLine());
//                                    for(int i = 0; i < n; i++) {
//                                        String username = br.readLine();
//                                        System.out.println(username);
//                                        mainScreen.roomsPanel.onlineUsers.add(username);
//                                    }
//                                    mainScreen.roomsPanel.refresh();
//                                }
//                                else if(receivedMessage.equalsIgnoreCase("roomuser")) {
//                                    String roomId = br.readLine();
//                                    String username = br.readLine();
//                                    System.out.println(username);
//                                    mainScreen.roomsPanel.onlineUsers.add(username);
//                                    mainScreen.roomsPanel.refresh();
//                                }
//                                else if(receivedMessage.equalsIgnoreCase("roommessage")) {
//                                    String roomId = br.readLine();
//                                    String messageId = br.readLine();
//                                    String author = br.readLine();
//                                    String text = br.readLine();
//                                    System.out.println(roomId + " " + messageId + " " + author + " " + text);
//                                    mainScreen.roomsPanel.addMessage(new Message(Long.parseLong(messageId), text, author));
//                                }
//                                else if(receivedMessage.equalsIgnoreCase("roomfile")) {
//                                    String roomId = br.readLine
//                                }
                            }
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
            });

            t.start();


            do
            {
                DataInputStream din=new DataInputStream(System.in);
                sentMessage=din.readLine();

                if (sentMessage.equalsIgnoreCase("quit")) {
                    break;
                } else if (sentMessage.equalsIgnoreCase("upload")) {
                    System.out.println("src.Room id: ");
                    String roomId = new BufferedReader(new InputStreamReader(System.in)).readLine();
                    System.out.print("Enter file name: ");
                    String fileName = new BufferedReader(new InputStreamReader(System.in)).readLine();
                    File file = new File(fileName);
                    if (!file.exists()) {
                        System.out.println("File not found");
                        continue;
                    }
                    bw.write("upload");
                    bw.newLine();
                    bw.write(roomId);
                    bw.newLine();
                    bw.write(file.getName());
                    bw.newLine();
                    bw.write(String.valueOf(file.length()));
                    bw.newLine();
                    bw.flush();
                    FileInputStream fis = new FileInputStream(file);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                    }
                    os.flush();
                    fis.close();
                }
                else {
                    bw.write(sentMessage);
                    bw.newLine();
                    bw.flush();
                }

            }
            while(true);

            bw.close();
            br.close();
        }
        catch(IOException e)
        {
            System.out.println("There're some error");
        }
    }
}

