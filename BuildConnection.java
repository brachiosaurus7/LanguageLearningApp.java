import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class BuildConnection extends JFrame implements ActionListener {
    private JTextField hostnameField;
    private JTextArea serviceArea;
    private JTextField serviceInputField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private String url;
    private String username;
    private String password;

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public BuildConnection() {
        setTitle("Oracle Database Connection");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1540, 820));

        JLabel background = new JLabel(new ImageIcon("lib\\sql.png"));
        background.setLayout(new BorderLayout());
        setContentPane(background);
        setLayout(new GridBagLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Build SQL Connection");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.GREEN);
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(20, 10, 10, 10);
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel hostnameLabel = new JLabel("Hostname:");
        hostnameLabel.setForeground(Color.WHITE);
        hostnameField = new JTextField(20);
        hostnameField.setEditable(false);
        panel.add(hostnameLabel, gbc);
        gbc.gridx++;
        panel.add(hostnameField, gbc);
        gbc.gridx++;

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel serviceLabel = new JLabel("Currently available service names:");
        serviceLabel.setForeground(Color.WHITE); 
        serviceArea = new JTextArea(10, 40);
        serviceArea.setEditable(false);
        JScrollPane serviceScrollPane = new JScrollPane(serviceArea);

        panel.add(serviceLabel, gbc);
        gbc.gridx++;
        panel.add(serviceScrollPane, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel serviceInputLabel = new JLabel("Default service name:");
        serviceInputLabel.setForeground(Color.WHITE);
        serviceInputField = new JTextField(20);
        serviceInputField.setText("xepdb1");
        serviceInputField.setEditable(false);

        panel.add(serviceInputLabel, gbc);
        gbc.gridx++;
        panel.add(serviceInputField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel usernameLabel = new JLabel("Enter the local Oracle database username:");
        usernameLabel.setForeground(Color.WHITE);
        usernameField = new JTextField(20);

        panel.add(usernameLabel, gbc);
        gbc.gridx++;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel passwordLabel = new JLabel("Enter the local Oracle database password:");
        passwordLabel.setForeground(Color.WHITE);
        passwordField = new JPasswordField(20);

        panel.add(passwordLabel, gbc);
        gbc.gridx++;
        panel.add(passwordField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;

        JButton connectButton = new JButton("Connect to Database");
        connectButton.addActionListener(this);
        panel.add(connectButton, gbc);

        gbc.gridy++;
        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setForeground(Color.WHITE); 
        panel.add(statusLabel, gbc);

        add(panel);
        pack();
        setVisible(true);

        getHostname();
        getServiceNames();
    }

    private void getHostname() {
        try {
            Process process = Runtime.getRuntime().exec("hostname");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                hostnameField.setText(line);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void getServiceNames() {
        try {
            Process process = Runtime.getRuntime().exec("lsnrctl status");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder serviceOutput = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                serviceOutput.append(line).append("\n");
            }

            serviceArea.setText(serviceOutput.toString().trim());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ((e.getActionCommand().equals("Connect to Database"))) {
            url = "jdbc:oracle:thin:@//" + hostnameField.getText() + ":1521/" + serviceInputField.getText();
            username = usernameField.getText();
            password = new String(passwordField.getPassword());

            try {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                Connection c = DriverManager.getConnection(url, username, password);
                statusLabel.setText("Database connection successful.");
                dispose();
                c.close();
            } catch (ClassNotFoundException | SQLException ex) {
                statusLabel.setText("Database connection failed.");
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BuildConnection::new);
        
    }
}
