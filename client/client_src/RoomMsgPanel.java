package client_src;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;

public class RoomMsgPanel extends JPanel {
    public JPanel msgPanel = new JPanel();
    public Room room;


    public RoomMsgPanel(Room room) {
        super();
        this.room = room;
        msgPanel.setLayout(new BoxLayout(msgPanel, BoxLayout.Y_AXIS));
        for (Message message : room.getMessages()) {
            msgPanel.add(new JLabel(message.author + ": " + message.text));
        }
        msgPanel.revalidate();
        msgPanel.repaint();
        add(msgPanel);
//        setVisible(true);
    }

    public void addMessage(Message message) {
        msgPanel.add(new JLabel(message.author + ": " + message.text));
        msgPanel.revalidate();
        msgPanel.repaint();
    }

    public void refresh() {
        msgPanel.removeAll();
        msgPanel.setLayout(new BoxLayout(msgPanel, BoxLayout.Y_AXIS));
        for (Message message : room.getMessages()) {
            msgPanel.add(new JLabel(message.author + ": " + message.text));
        }
        msgPanel.revalidate();
        msgPanel.repaint();
    }

    @Override
    public String toString() {
        return room.name;
    }

//    public void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        int y = 0;
//        for (Message message : room.getMessages()) {
//            g.drawString(message.author + ": " + message.text, 0, y);
//            y += 20;
//        }
//    }
}
