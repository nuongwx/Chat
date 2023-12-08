package client_src;

import javax.swing.*;
import java.io.IOException;
import java.util.*;
public class MainScreen extends JFrame {
    public static ArrayList<String> onlineUsers = new ArrayList<String>();
    public static ArrayList<Room> rooms = new ArrayList<Room>();

    public static JPanel leftPanel = new JPanel();
    public static JPanel rightPanel = new JPanel();

    public static DefaultListModel<String> onlineUsersListModel = new DefaultListModel<String>();
    public static DefaultListModel<String> roomsListModel = new DefaultListModel<String>();
    public static JList<String> onlineUsersList = new JList<String>(onlineUsersListModel);
    public static JList<String> roomsList = new JList<String>(roomsListModel);

    public MainScreen() {
        super("Chat Application");


        JSplitPane leftSplitPane = new JSplitPane();
        leftSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

        JPanel leftTopPanel = new JPanel();
        leftTopPanel.setLayout(new BoxLayout(leftTopPanel, BoxLayout.Y_AXIS));
        leftTopPanel.add(new JLabel("Online Users"));
        leftTopPanel.add(onlineUsersList);
        onlineUsersListModel.addElement("test");
        leftSplitPane.setTopComponent(leftTopPanel);

        JPanel leftBottomPanel = new JPanel();
        leftBottomPanel.setLayout(new BoxLayout(leftBottomPanel, BoxLayout.Y_AXIS));
        leftBottomPanel.add(new JLabel("Rooms"));
        leftBottomPanel.add(roomsList);
        roomsListModel.addElement("test");
        leftSplitPane.setBottomComponent(leftBottomPanel);

        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.add(leftSplitPane);

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
