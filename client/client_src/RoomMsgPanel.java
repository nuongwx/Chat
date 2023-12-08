package client_src;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;

public class RoomMsgPanel extends JPanel {
    public ArrayList<Message> messages = new ArrayList<Message>();
    public JPanel msgPanel = new JPanel();

    public JPanel buttonPanel = new JPanel();
    public JTextField textField1 = new JTextField();
    public JButton sendButton = new JButton("Send");

    public Long roomID;


    public RoomMsgPanel(Long id) throws IOException {
        super();
//        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

//        msgPanel.setLayout(new BoxLayout(msgPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(msgPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//        scrollPane.setBounds(0, 0, 800, 500);
        add(scrollPane);

//        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
//        buttonPanel.setBounds(0, 500, 800, 100);

//        textField1.setBounds(0, 0, 700, 100);
        buttonPanel.add(textField1);

//        sendButton.setBounds(700, 0, 100, 100);
        buttonPanel.add(sendButton);

        add(buttonPanel);
        refresh();

        setVisible(true);

    }

    public void addMessage(Message message) throws IOException {
        messages.add(message);
        refresh();
    }

    public void refresh() throws IOException {
        msgPanel.removeAll();
        msgPanel.setLayout(new BoxLayout(msgPanel, BoxLayout.Y_AXIS));
        for (Message message : messages) {
            msgPanel.add(new JLabel(message.author + ": " + message.text));
        }
        msgPanel.revalidate();
        msgPanel.repaint();
    }


    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int y = 0;
        for (Message message : messages) {
            g.drawString(message.author + ": " + message.text, 0, y);
            y += 20;
        }
    }
}
