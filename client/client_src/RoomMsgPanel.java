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
            JLabel label = new JLabel(message.author + ": " + message.text);
            label.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    if (SwingUtilities.isRightMouseButton(evt)) {
                        JPopupMenu popupMenu = new JPopupMenu();
                        JMenuItem deleteItem = new JMenuItem("Delete");
                        deleteItem.addActionListener(new java.awt.event.ActionListener() {
                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                try {
                                    MainClient.bw.write("delete message\n");
                                    MainClient.bw.write(message.id + "\n");
                                    MainClient.bw.flush();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        popupMenu.add(deleteItem);

                        if(message.text.contains("\u200B")) {
                            JMenuItem downloadItem = new JMenuItem("Download");
                            downloadItem.addActionListener(new java.awt.event.ActionListener() {
                                public void actionPerformed(java.awt.event.ActionEvent evt) {
                                    try {
                                        MainClient.bw.write("download file\n");
                                        MainClient.bw.write(message.id + "\n");
                                        MainClient.bw.flush();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            popupMenu.add(downloadItem);
                        }

                        popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                    }
                }
            });
            msgPanel.add(label);
//            msgPanel.add(new JLabel(message.author + ": " + message.text));
        }
        msgPanel.revalidate();
        msgPanel.repaint();
        add(msgPanel);
//        setVisible(true);
    }

    public void addMessage(Message message) {
//        msgPanel.add(new JLabel(message.author + ": " + message.text));
        JLabel label = new JLabel(message.author + ": " + message.text);
        label.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (SwingUtilities.isRightMouseButton(evt)) {
                    JPopupMenu popupMenu = new JPopupMenu();
                    JMenuItem deleteItem = new JMenuItem("Delete");
                    deleteItem.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            try {
                                MainClient.bw.write("delete message\n");
                                MainClient.bw.write(message.id + "\n");
                                MainClient.bw.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    popupMenu.add(deleteItem);

                    if(message.text.contains("\u200B")) {
                        JMenuItem downloadItem = new JMenuItem("Download");
                        downloadItem.addActionListener(new java.awt.event.ActionListener() {
                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                try {
                                    MainClient.bw.write("download\n");
                                    MainClient.bw.write(message.id + "\n");
                                    MainClient.bw.flush();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        popupMenu.add(downloadItem);
                    }

                    popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        });
        msgPanel.add(label);
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
