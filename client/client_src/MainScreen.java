package client_src;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;

public class MainScreen extends JFrame {
    public static ArrayList<String> onlineUsers = new ArrayList<>();
    public static ArrayList<Room> rooms = new ArrayList<>();

    public static JPanel leftPanel = new JPanel();
    public static JPanel rightPanel = new JPanel();

    public static DefaultListModel<String> onlineUsersListModel = new DefaultListModel<String>();
    public static DefaultListModel<Room> roomsListModel = new DefaultListModel<Room>();
    public static JList<String> onlineUsersList = new JList<String>(onlineUsersListModel);
    public static JList<Room> roomsList = new JList<Room>(roomsListModel);

    public static DefaultListModel<String> roomMembersListModel = new DefaultListModel<String>();
    public static JList<String> roomMembersList = new JList<String>(roomMembersListModel);
    public static JTabbedPane tabbedPane = new JTabbedPane();

    public MainScreen() {
        super("Chat Application");


        JSplitPane leftSplitPane = new JSplitPane();
        leftSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

        JPanel leftTopPanel = new JPanel();
        leftTopPanel.setLayout(new BoxLayout(leftTopPanel, BoxLayout.Y_AXIS));
        leftTopPanel.add(new JLabel("Online Users"));
        leftTopPanel.add(onlineUsersList);
        leftSplitPane.setTopComponent(leftTopPanel);

        JPanel leftBottomPanel = new JPanel();
        leftBottomPanel.setLayout(new BoxLayout(leftBottomPanel, BoxLayout.Y_AXIS));
        leftBottomPanel.add(new JLabel("Rooms"));
        leftBottomPanel.add(roomsList);
        roomsList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
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

                try {
                    MainClient.bw.write("fetch messages\n");
                    MainClient.bw.write(MainScreen.rooms.get(index).id + "\n");
                    MainClient.bw.flush();
                    MainScreen.rooms.get(index).clearMessages();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        leftSplitPane.setBottomComponent(leftBottomPanel);

        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.add(leftSplitPane);

        JSplitPane rightSplitPane = new JSplitPane();
        rightSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);

        JPanel rightMiddlePane = new JPanel();
        rightMiddlePane.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                int index = tabbedPane.getSelectedIndex();
                roomMembersListModel.removeAllElements();
                RoomMsgPanel roomMsgPanel = (RoomMsgPanel) tabbedPane.getComponentAt(index);
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


            }
        });
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        rightMiddlePane.add(tabbedPane, gbc);


        JTextField textField = new JTextField();
        JButton button = new JButton("Send");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                String message = textField.getText();
                try {
                    MainClient.bw.write("message\n");
                    MainClient.bw.write(MainScreen.rooms.get(tabbedPane.getSelectedIndex()).id + "\n");
                    MainClient.bw.write(message + "\n");
                    MainClient.bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(textField);
        buttonPanel.add(button);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.PAGE_END;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        rightMiddlePane.add(buttonPanel, gbc);
        rightMiddlePane.setMinimumSize(new Dimension(200, 200));

        JPanel rightMemberPanel = new JPanel();
        rightMemberPanel.setLayout(new BoxLayout(rightMemberPanel, BoxLayout.Y_AXIS));
        rightMemberPanel.add(new JLabel("Members"));
        rightMemberPanel.add(roomMembersList);
        rightSplitPane.setRightComponent(rightMemberPanel);

        rightSplitPane.setLeftComponent(rightMiddlePane);
        rightSplitPane.setResizeWeight(1);

        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(rightSplitPane);


        JSplitPane splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(200);
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);

        setSize(800, 600);


        add(splitPane);


        setVisible(true);
    }

}
