package client_src;

import com.sun.tools.javac.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class LogonScreen extends JFrame {
    public JPanel panel1;
    private JTextField textField1;
    private JPasswordField passwordField1;
    private JButton loginButton;
    private JButton registerButton;
    private JTextField serverAddressField;
    private JTextField serverPortField;
    private JButton connectButton;
    public JPanel serverPanel;

    public LogonScreen() {
        super("Logon");

        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        serverAddressField = new JTextField("", 10);
        serverPortField = new JTextField("", 5);

        connectButton = new JButton("Connect");
        connectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                String serverAddress = serverAddressField.getText();
                String serverPort = serverPortField.getText();

                if (serverAddress.isBlank() || serverPort.isBlank()) {
                    JOptionPane.showMessageDialog(null, "Server address or port cannot be empty");
                } else {
                    try {
                        MainClient.s = new java.net.Socket(serverAddress, Integer.parseInt(serverPort));
                        MainClient.is = MainClient.s.getInputStream();
                        MainClient.os = MainClient.s.getOutputStream();
                        MainClient.br = new java.io.BufferedReader(new java.io.InputStreamReader(MainClient.s.getInputStream()));
                        MainClient.bw = new java.io.BufferedWriter(new java.io.OutputStreamWriter(MainClient.s.getOutputStream()));
                        JOptionPane.showMessageDialog(null, "Connected to server");
                        MainClient.logonScreen.remove(serverPanel);
                        MainClient.logonScreen.add(panel1);
                        MainClient.logonScreen.getRootPane().setDefaultButton(loginButton);
                        MainClient.logonScreen.revalidate();
                        MainClient.logonScreen.repaint();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, "Cannot connect to server");
                        MainClient.s = null;
                        System.out.println(e.getMessage());
                    }
                }
            }
        });

        serverPanel = new JPanel();
        serverPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        serverPanel.add(new JLabel("Server Address"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        serverPanel.add(serverAddressField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        serverPanel.add(new JLabel("Server Port"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        serverPanel.add(serverPortField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        serverPanel.add(connectButton, gbc);
        this.add(serverPanel);


        textField1 = new JTextField();
        passwordField1 = new JPasswordField();

        loginButton = new JButton("Login");
        registerButton = new JButton("Register");

        loginButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                String username = textField1.getText();
                String password = passwordField1.getText();

                if (username.isBlank() || password.isBlank()) {
                    JOptionPane.showMessageDialog(null, "Username or password cannot be empty");
                } else {
                    try {
                        MainClient.bw.write("login\n");
                        MainClient.bw.write(username + "\n");
                        MainClient.bw.write(password + "\n");
                        MainClient.bw.flush();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
//                    read the response from server
                    try {
                        String response = MainClient.br.readLine();
                        if (response.equals("login success")) {
                            JOptionPane.showMessageDialog(null, "Login success");
                            MainClient.bw.write("online\n");
                            MainClient.bw.write("fetch rooms\n");
                            MainClient.bw.flush();

                            MainClient.username = username;
                            MainClient.mainScreen = new MainScreen();

                            MainClient.logonScreen.setVisible(false);
                            MainClient.logonScreen.dispose();

                            MainClient.t.start();
                        } else {
                            JOptionPane.showMessageDialog(null, "Login failed");
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        });

        registerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if(MainClient.s == null) {
                    JOptionPane.showMessageDialog(null, "Please connect to server first");
                    return;
                }
                String username = textField1.getText();
                String password = passwordField1.getText();

                if (username.isBlank() || password.isBlank()) {
                    JOptionPane.showMessageDialog(null, "Username or password cannot be empty");
                } else {
                    try {
                        MainClient.bw.write("register\n");
                        MainClient.bw.write(username + "\n");
                        MainClient.bw.write(password + "\n");
                        MainClient.bw.flush();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
//                    read the response from server
                    try {
                        String response = MainClient.br.readLine();
                        System.out.println(response);
                        if (response.equals("register success")) {
                            JOptionPane.showMessageDialog(null, "Register success");

                        } else {
                            JOptionPane.showMessageDialog(null, "Register failed");
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        });

        panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel1.add(new JLabel("Username"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel1.add(textField1, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel1.add(new JLabel("Password"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel1.add(passwordField1, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel1.add(loginButton, gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel1.add(registerButton, gbc);



        this.getRootPane().setDefaultButton(connectButton);

        this.setSize(400, 400);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);

    }
}
