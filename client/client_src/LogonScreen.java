package client_src;

import com.sun.tools.javac.Main;

import javax.swing.*;
import java.awt.*;

public class LogonScreen extends JFrame {
    private JPanel panel1;
    private JTextField textField1;
    private JPasswordField passwordField1;
    private JButton loginButton;
    private JButton registerButton;

    public LogonScreen() {
        super("Logon");

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
        GridBagConstraints gbc = new GridBagConstraints();
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
        this.add(panel1);
        this.setSize(400, 400);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);

    }
}
