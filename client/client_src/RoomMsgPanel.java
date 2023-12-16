package client_src;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;

public class RoomMsgPanel extends JPanel {
    public JPanel msgPanel = new JPanel();
    public Room room;
    public JScrollPane scrollPane;


    public RoomMsgPanel(Room room) {
        super();
        this.room = room;
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
//        c.anchor = GridBagConstraints.PAGE_START;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;

        msgPanel.setLayout(new BoxLayout(msgPanel, BoxLayout.Y_AXIS));

        for (Message message : room.getMessages()) {
            this.addMessage(message);
        }
        msgPanel.revalidate();
        msgPanel.repaint();

        scrollPane = new JScrollPane(msgPanel);
        scrollPane.setPreferredSize(new Dimension(500, 200));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, c);

        this.revalidate();
        this.repaint();

    }

    public void addMessage(Message message) {
        msgPanel.add(Box.createHorizontalGlue());
        msgPanel.add(new MessageBubble(message, this));
        msgPanel.revalidate();
        msgPanel.repaint();
    }

    public void refresh() {
        msgPanel.removeAll();
        msgPanel.setLayout(new BoxLayout(msgPanel, BoxLayout.Y_AXIS));

        for (Message message : room.getMessages()) {
            this.addMessage(message);
        }
//        msgPanel.revalidate();
//        msgPanel.repaint();

    }

    @Override
    public String toString() {
        return room.name;
    }
}
