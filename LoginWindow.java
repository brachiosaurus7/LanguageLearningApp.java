import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LoginWindow extends JFrame implements ActionListener {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    public LoginWindow() {
        setTitle("LANGUAGE LEARNING APP - Login");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon background = new ImageIcon("lib\\\\image 5.jpg"); 
                Image img = background.getImage();
                g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
            }
        };
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.5;

        JLabel titleLabel = new JLabel("LANGUAGE LEARNING APP", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 40));
        panel.add(titleLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(20, 0, 0, 0);

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.setOpaque(false); 

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBackground(Color.WHITE); 
        usernameLabel.setForeground(Color.BLACK); 
        usernameField = new JTextField(15);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBackground(Color.WHITE); 
        passwordLabel.setForeground(Color.BLACK); 
        passwordField = new JPasswordField(15);


        inputPanel.add(usernameLabel);
        inputPanel.add(usernameField);
        inputPanel.add(passwordLabel);
        inputPanel.add(passwordField);

        panel.add(inputPanel, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(20, 0, 0, 0);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(this);

        JButton displayUsersButton = new JButton("Display Users");
        displayUsersButton.addActionListener(this);

        JPanel buttonPanel1 = new JPanel(new GridLayout(1, 2, 10, 10));
        buttonPanel1.add(loginButton);
        buttonPanel1.add(displayUsersButton);
        panel.add(buttonPanel1, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(20, 0, 0, 0);

        JButton createUserButton = new JButton("Create New User");
        createUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createUser();
            }
        });

        JButton removeUserButton = new JButton("Remove User");
        removeUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeUser();
            }
        });

        JPanel buttonPanel2 = new JPanel(new GridLayout(1, 2, 10, 10));
        buttonPanel2.add(createUserButton);
        buttonPanel2.add(removeUserButton);
        panel.add(buttonPanel2, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(20, 0, 0, 0);

        statusLabel = new JLabel("", SwingConstants.CENTER);
        panel.add(statusLabel, gbc);

        add(panel);
        setVisible(true);

        createAndInsertUsers();
    }

    private void createAndInsertUsers() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");

            Connection c = DriverManager.getConnection(Main.getModifiedUrl(), Main.getUsername(), Main.getPassword());

            System.out.println("Connection successful"); 

            Statement statement = c.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM all_tables WHERE table_name = 'USERS'");
            boolean tableExists = resultSet.next();

            if (!tableExists) {
                statement.executeUpdate("CREATE TABLE users (username VARCHAR(50), password VARCHAR(50))");
                statement.executeUpdate("INSERT INTO users VALUES ('harshit', 'sidher')");
                statement.executeUpdate("INSERT INTO users VALUES ('dhaani', 'sangwan')");
            }

            c.close();

        } catch (ClassNotFoundException | SQLException ee) {
            ee.printStackTrace();
            statusLabel.setText("Database Error");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (e.getActionCommand().equals("Login")) {
            if ((username.equals("harshit") && password.equals("sidher")) ||
                    (username.equals("dhaani") && password.equals("sangwan"))) {
                statusLabel.setText("Login Successful");
   
                Thread openlanguagelearningapp = new Thread(() -> {
                    LanguageLearningApp.main(new String[]{});
                });
                openlanguagelearningapp.start();
        
                try {
                    openlanguagelearningapp.join();
                } catch (InterruptedException a) {
                    a.printStackTrace();
                }
                dispose();
                return; 
            }

            try {
                Class.forName("oracle.jdbc.driver.OracleDriver");

                Connection c = DriverManager.getConnection(Main.getModifiedUrl(), Main.getUsername(), Main.getPassword());

                System.out.println("Connection successful");

                Statement statement = c.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM users WHERE username = '" + username + "' AND password = '" + password + "'");
                boolean loginSuccessful = resultSet.next();

                if (loginSuccessful) {
                    statusLabel.setText("Login Successful");
                    LanguageLearningApp.main(new String[]{});
                    dispose();
                } else {
                    statusLabel.setText("Invalid Username or Password");
                }

                c.close();

            } catch (ClassNotFoundException | SQLException ee) {
                ee.printStackTrace();
                statusLabel.setText("Database Error");
            }
        } else if (e.getActionCommand().equals("Display Users")) {
            displayUsers();
        }
    }

    private void createUser() {
        String newUsername = usernameField.getText();
        String newPassword = new String(passwordField.getPassword());

        if (newUsername.isEmpty() || newPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username or password cannot be empty.");
            return;
        }

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");

            Connection c = DriverManager.getConnection(Main.getModifiedUrl(), Main.getUsername(), Main.getPassword());

            Statement statement = c.createStatement();

            ResultSet resultSet = statement.executeQuery("SELECT * FROM users WHERE username = '" + newUsername + "'");
            boolean userExists = resultSet.next();

            if (userExists) {
                JOptionPane.showMessageDialog(this, "Username already exists. Please choose a different username.");
            } else {
                PreparedStatement preparedStatement = c.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
                preparedStatement.setString(1, newUsername);
                preparedStatement.setString(2, newPassword);
                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "User created successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to create user!");
                }
            }

            c.close();

        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error creating user: " + ex.getMessage());
        }
    }

    private void removeUser() {
        String username = usernameField.getText();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty.");
            return;
        }

        if (username.equals("dhaani") || new String(passwordField.getPassword()).equals("sangwan") ||
            username.equals("harshit") || new String(passwordField.getPassword()).equals("sidher")) {
            JOptionPane.showMessageDialog(this, "Try removing a different user. These people wrote this game");
            return;
        }

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");

            Connection c = DriverManager.getConnection(Main.getModifiedUrl(), Main.getUsername(), Main.getPassword());

            Statement statement = c.createStatement();

            ResultSet resultSet = statement.executeQuery("SELECT * FROM all_tables WHERE table_name = 'USERS'");
            boolean tableExists = resultSet.next();
            if (!tableExists) {
                JOptionPane.showMessageDialog(this, "No users found.");
            } else {
                resultSet = statement.executeQuery("SELECT * FROM users WHERE username = '" + username + "'");
                boolean userExists = resultSet.next();

                if (!userExists) {
                    JOptionPane.showMessageDialog(this, "User not found.");
                } else {
                    int rowsAffected = statement.executeUpdate("DELETE FROM users WHERE username = '" + username + "'");

                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "User removed successfully!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to remove user!");
                    }
                }
            }

            c.close();

        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error removing user: " + ex.getMessage());
        }
    }

    private void displayUsers() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");

            Connection c = DriverManager.getConnection(Main.getModifiedUrl(), Main.getUsername(), Main.getPassword());

            Statement statement = c.createStatement();

            ResultSet resultSet = statement.executeQuery("SELECT * FROM all_tables WHERE table_name = 'USERS'");
            boolean tableExists = resultSet.next();
            if (!tableExists) {
                JOptionPane.showMessageDialog(this, "No users found.");
            } else {
                resultSet = statement.executeQuery("SELECT * FROM users");
                StringBuilder users = new StringBuilder("Users:\n");
                while (resultSet.next()) {
                    String username = resultSet.getString("username");
                    users.append(username).append("\n");
                }
                JOptionPane.showMessageDialog(this, users.toString());
            }

            c.close();

        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error displaying users: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginWindow::new);
    }
}

