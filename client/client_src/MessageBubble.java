package client_src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.time.*;

public class MessageBubble extends JPanel {
    public Message message;
    public RoomMsgPanel roomMsgPanel;

    public MessageBubble(Message message, RoomMsgPanel roomMsgPanel) {
        super();
        this.message = message;
        this.roomMsgPanel = roomMsgPanel;

        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // parse the time from the message id
        String time = message.id.toString().substring(0, 10);
        LocalDateTime ldt = LocalDateTime.ofEpochSecond(Long.parseLong(time), 0, ZoneOffset.UTC);
        String timeString = ldt.toString().substring(11, 16);

        JTextField timeField = new JTextField("[" + timeString + "]", 7);
        timeField.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        timeField.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        timeField.setEditable(false);
        this.add(timeField);

        JTextField authorField = new JTextField(message.author, 10);
        authorField.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        authorField.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        authorField.setEditable(false);
        this.add(authorField);

        JTextArea textField = new JTextArea(message.text);
        textField.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        textField.setColumns(30);
        textField.setEditable(false);
        textField.setLineWrap(true);
        textField.setWrapStyleWord(true);
        textField.setBackground(this.getBackground());
//        textField.setBorder(javax.swing.BorderFactory.createLineBorder(Color.black, 1));
        textField.revalidate();
        this.add(textField);

//        this.setMinimumSize(new Dimension(this.getMaximumSize().width, textField.getPreferredSize().height));
        this.setMaximumSize(new Dimension(this.getMaximumSize().width, textField.getPreferredSize().height));
//        this.setPreferredSize(new Dimension(this.getMaximumSize().width, textField.getPreferredSize().height * 2));
//        this.setBorder(BorderFactory.createLineBorder(Color.black));
        this.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (SwingUtilities.isRightMouseButton(evt)) {
                    JPopupMenu popupMenu = new JPopupMenu();
                    JMenuItem deleteItem = new JMenuItem("Delete");
                    deleteItem.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            try {
                                MainClient.bw.write("delete message\n");
                                MainClient.bw.write(roomMsgPanel.room.id + "\n");
                                MainClient.bw.write(message.id + "\n");
                                MainClient.bw.flush();
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    });

                    JMenuItem downloadItem = new JMenuItem("Download");
                    downloadItem.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            try {
                                MainClient.bw.write("download file\n");
                                MainClient.bw.write(message.id + "\n");
                                MainClient.bw.flush();
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    });
                    if (message.author.equals(MainClient.username)) {
                        popupMenu.add(deleteItem);
                    }
                    if (message.text.contains("\u200B")) {
                        popupMenu.add(downloadItem);
                    }
                    popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // TODO Auto-generated method stub
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // TODO Auto-generated method stub
            }
        });

    }
}
