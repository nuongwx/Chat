package client_src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainScreen extends JFrame {
    public static ArrayList<String> onlineUsers = new ArrayList<>();
    public static ArrayList<Room> rooms = new ArrayList<>();

    public static JPanel leftPanel = new JPanel();
    public static JPanel rightPanel = new JPanel();

    public static DefaultListModel<String> onlineUsersListModel = new DefaultListModel<>();
    public static DefaultListModel<Room> roomsListModel = new DefaultListModel<>();
    public static JList<String> onlineUsersList = new JList<>(onlineUsersListModel);
    public static JList<Room> roomsList = new JList<>(roomsListModel);

    public static DefaultListModel<String> roomMembersListModel = new DefaultListModel<>();
    public static JList<String> roomMembersList = new JList<>(roomMembersListModel);
    public static JTabbedPane tabbedPane = new JTabbedPane();
    public static JPanel rightMemberPanel;

    JTextField textField = new JTextField();
    JButton fileButton = new JButton("✚");
    JButton sendButton = new JButton("Send");
    JButton inviteButton = new JButton("Invite");
    public static JFileChooser fileChooser = new JFileChooser();


    public MainScreen() {
        super("Chat Application");


        JSplitPane leftSplitPane = new JSplitPane();
        leftSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        leftSplitPane.setDividerLocation(200);

        JPanel leftTopPanel = new JPanel();
        leftTopPanel.setLayout(new BoxLayout(leftTopPanel, BoxLayout.Y_AXIS));
        leftTopPanel.add(new JLabel("Online Users"));
        leftTopPanel.add(onlineUsersList);
        onlineUsersList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    String username = onlineUsersList.getSelectedValue();
                    boolean found = false;
                    for (int i = 0; i < roomsListModel.size(); i++) {
                        if (roomsListModel.get(i).name.equals(username + " - " + MainClient.username) || roomsListModel.get(i).name.equals(MainClient.username + " - " + username)) {
                            roomsList.setSelectedIndex(i);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        try {
                            MainClient.bw.write("create room\n");
                            MainClient.bw.write(MainClient.username + " - " + username + "\n");
                            MainClient.bw.write("true\n");
                            MainClient.bw.write("1\n");
                            MainClient.bw.write(username + "\n");
                            MainClient.bw.flush();
                            MainClient.bw.write("fetch rooms\n");
                            MainClient.bw.flush();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
        leftSplitPane.setTopComponent(leftTopPanel);

        JPanel leftBottomPanel = new JPanel();
        leftBottomPanel.setLayout(new BoxLayout(leftBottomPanel, BoxLayout.Y_AXIS));
        leftBottomPanel.add(new JLabel("Rooms"));
        leftBottomPanel.add(roomsList);
        roomsList.addListSelectionListener(evt -> {
            int index = roomsList.getSelectedIndex();
            RoomMsgPanel roomMsgPanel = (MainScreen.rooms.get(index)).roomMsgPanel;

            boolean found = false;

            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                if (tabbedPane.getComponentAt(i) == roomMsgPanel) {
                    tabbedPane.setSelectedIndex(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                tabbedPane.addTab(MainScreen.rooms.get(index).name, roomMsgPanel);
                tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
            }

            roomMembersListModel.removeAllElements();
            for (String member : roomMsgPanel.room.members) {
                roomMembersListModel.addElement(member);
            }


            textField.setEnabled(true);
            sendButton.setEnabled(true);
            inviteButton.setEnabled(true);
            fileButton.setEnabled(true);
        });

        JButton createRoomButton = new JButton("Create Room");
        createRoomButton.addActionListener(evt -> {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            JTextField roomNameField = new JTextField();
            panel.add(new JLabel("Room Name"));
            panel.add(roomNameField);
            JList<String> membersList = new JList<>(onlineUsersListModel);
            panel.add(new JLabel("Members"));
            panel.add(membersList);
            JCheckBox isPrivateCheckBox = new JCheckBox("Private");
            panel.add(isPrivateCheckBox);
            JOptionPane.showMessageDialog(null, panel);
            String roomName = roomNameField.getText();
            if (roomName != null && !roomName.isBlank()) {
                try {
                    MainClient.bw.write("create room\n");
                    MainClient.bw.write(roomName + "\n");
                    MainClient.bw.write(isPrivateCheckBox.isSelected() + "\n");
                    MainClient.bw.write(membersList.getSelectedValuesList().size() + "\n");
                    for (Object member : membersList.getSelectedValuesList()) {
                        MainClient.bw.write(member.toString() + "\n");
                    }
                    MainClient.bw.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Room name cannot be empty");
            }
        });
        leftBottomPanel.add(Box.createVerticalGlue());
        leftBottomPanel.add(createRoomButton);

        leftSplitPane.setBottomComponent(leftBottomPanel);

        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.add(leftSplitPane);

        JSplitPane rightSplitPane = new JSplitPane();
        rightSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        rightSplitPane.setDividerLocation(500);

        JPanel rightMiddlePane = new JPanel();
        rightMiddlePane.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        tabbedPane.addChangeListener(evt -> {
            int index = tabbedPane.getSelectedIndex();
            roomMembersListModel.removeAllElements();
            RoomMsgPanel roomMsgPanel = (RoomMsgPanel) tabbedPane.getComponentAt(index);
            try {
                MainScreen.rooms.get(index).clearMessages();
                MainClient.bw.write("fetch messages\n");
                MainClient.bw.write(roomMsgPanel.room.id + "\n");
                MainClient.bw.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                MainClient.bw.write("fetch members\n");
                MainClient.bw.write(roomMsgPanel.room.id + "\n");
                MainClient.bw.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            for (String member : roomMsgPanel.room.members) {
                roomMembersListModel.addElement(member);
            }

            if (roomMsgPanel.room.isPrivate) {
                rightMemberPanel.remove(inviteButton);
                rightMemberPanel.revalidate();
                rightMemberPanel.repaint();
            } else if (rightMemberPanel.getComponents()[rightMemberPanel.getComponents().length - 1] != inviteButton) {
                rightMemberPanel.add(inviteButton);
                rightMemberPanel.revalidate();
                rightMemberPanel.repaint();
            }
            textField.setEnabled(true);
            sendButton.setEnabled(true);
            fileButton.setEnabled(true);
        });
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        rightMiddlePane.add(tabbedPane, gbc);

        fileButton.setEnabled(false);
        fileButton.addActionListener(evt -> {
            int returnVal = fileChooser.showOpenDialog(MainScreen.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    MainClient.bw.write("upload\n");
                    MainClient.bw.write(((RoomMsgPanel) tabbedPane.getSelectedComponent()).room.id + "\n");
                    MainClient.bw.write(file.getName() + "\n");
                    MainClient.bw.write(file.length() + "\n");
                    MainClient.bw.flush();
                    MainClient.bw.flush();

                    // blocks server-side bufferedReader buffer-reading thing
                    Thread.sleep(150); // i love concurrency

                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                    byte[] buffer = new byte[1024];
                    int count;
                    if (file.length() > 0) {
                        while ((count = bis.read(buffer)) > 0) {
                            MainClient.os.write(buffer, 0, count);
                        }
                    }
                    bis.close();
                    MainClient.os.flush();
                    System.out.println("File sent");
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                System.out.println("Open command cancelled by user.");
            }
        });
        textField = new JTextField();
        sendButton = new JButton("Send");
        sendButton.setEnabled(false);
        textField.setEnabled(false);

        textField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    String message = textField.getText();
                    if (message.isBlank()) {
                        return;
                    }
                    sendButton.doClick();
                }
            }
        });
        sendButton.addActionListener(evt -> {
            String message = textField.getText();
            if (message.isBlank()) {
                return;
            }
            try {
                MainClient.bw.write("message\n");
                MainClient.bw.write(((RoomMsgPanel) tabbedPane.getSelectedComponent()).room.id + "\n");
                MainClient.bw.write(message + "\n");
                MainClient.bw.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            textField.setText("");
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(fileButton);
        buttonPanel.add(textField);
        buttonPanel.add(sendButton);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.PAGE_END;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        rightMiddlePane.add(buttonPanel, gbc);
        rightMiddlePane.setMinimumSize(new Dimension(200, 200));

        rightMemberPanel = new JPanel();
        rightMemberPanel.setLayout(new BoxLayout(rightMemberPanel, BoxLayout.Y_AXIS));
        rightMemberPanel.add(new JLabel("Members"));
        rightMemberPanel.add(roomMembersList);

        inviteButton = new JButton("Invite");
        inviteButton.setEnabled(false);
        inviteButton.addActionListener(evt -> {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            DefaultListModel<String> onlineUsersInviteListModel = new DefaultListModel<>();
            for (String user : onlineUsers) {
                if (!((RoomMsgPanel) tabbedPane.getSelectedComponent()).room.members.contains(user)) {
                    onlineUsersInviteListModel.addElement(user);
                }
            }
            JList<String> membersList = new JList<>(onlineUsersInviteListModel);
            panel.add(new JLabel("Members"));
            panel.add(membersList);
            JOptionPane.showMessageDialog(null, panel);

            try {
                if(membersList.getSelectedValuesList().isEmpty()) {
                    return;
                }
                MainClient.bw.write("invite\n");
                MainClient.bw.write(((RoomMsgPanel) tabbedPane.getSelectedComponent()).room.id + "\n");
                MainClient.bw.write(membersList.getSelectedValuesList().size() + "\n");
                for (Object member : membersList.getSelectedValuesList()) {
                    MainClient.bw.write(member.toString() + "\n");
                }
                MainClient.bw.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        rightMemberPanel.add(Box.createVerticalGlue());
        rightMemberPanel.add(inviteButton);


        rightSplitPane.setRightComponent(rightMemberPanel);

        rightSplitPane.setLeftComponent(rightMiddlePane);
        rightSplitPane.setResizeWeight(1);

        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(rightSplitPane);


        JSplitPane splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(150);
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);

        setSize(800, 600);


        add(splitPane);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

}
