import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;

public class GuessTheSpelling extends JFrame {
    static final String JDBC_URL = Main.getUrl();
    static final String USERNAME = Main.getUsername();
    static final String PASSWORD = Main.getPassword();

    static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS GERMAN_WORDS_PRO (" +
            "ID NUMBER PRIMARY KEY, " +
            "CORRECT_SPELLING VARCHAR2(50), " +
            "INCORRECT_SPELLING1 VARCHAR2(50), " +
            "INCORRECT_SPELLING2 VARCHAR2(50), " +
            "INCORRECT_SPELLING3 VARCHAR2(50))";

    static final String SELECT_ALL_SQL = "SELECT * FROM GERMAN_WORDS_PRO";
    static final String INSERT_SQL = "INSERT INTO GERMAN_WORDS_PRO (ID, CORRECT_SPELLING, INCORRECT_SPELLING1, INCORRECT_SPELLING2, INCORRECT_SPELLING3) " +
            "VALUES (?, ?, ?, ?, ?)";
    static final String SELECT_RANDOM_SQL = "SELECT * FROM GERMAN_WORDS_PRO WHERE ID = ?";

    private Connection connection;
    private int currentRound = 1;
    private int score = 0;

    private JButton[] optionButtons = new JButton[4];
    private JLabel imageLabel = new JLabel(); 
    private String[] germanWords;
    private String[] incorrectSpellings1;
    private String[] incorrectSpellings2;
    private String[] incorrectSpellings3;

    public GuessTheSpelling() {
        try {
            connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
            System.out.println("Connected to the database.");
    
            createTableIfNotExists();
            insertDataIfNotExists();
            initializeGermanWords();
    
            setExtendedState(JFrame.MAXIMIZED_BOTH); 
            setLocationRelativeTo(null); 
            setLayout(new BorderLayout());
            getContentPane().setBackground(new Color(230, 230, 230)); 
    
           
            JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            imagePanel.setBackground(Color.BLACK); 
            imagePanel.setBorder(BorderFactory.createEmptyBorder(60, 20, 80, 20)); 
            add(imagePanel, BorderLayout.NORTH);
            imagePanel.add(imageLabel); 

            
            JPanel buttonsPanel = new JPanel(new GridLayout(2, 2, 20, 20)); 
            buttonsPanel.setBackground(new Color(230, 230, 230)); 
            buttonsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); 
            add(buttonsPanel, BorderLayout.CENTER);
    
            
            for (int i = 0; i < 4; i++) {
                optionButtons[i] = new JButton();
                optionButtons[i].setFont(new Font("Arial", Font.BOLD, 20));
                optionButtons[i].setOpaque(false);
                optionButtons[i].addActionListener(new OptionButtonListener());
                buttonsPanel.add(optionButtons[i]);
            }
    
            setVisible(true);
    
            displayWords();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    

    private void createTableIfNotExists() throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getTables(null, null, "GERMAN_WORDS_PRO", null)) {
            if (!resultSet.next()) {
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(CREATE_TABLE_SQL);
                    System.out.println("Table GERMAN_WORDS_PRO created successfully.");
                }
            } else {
                System.out.println("Table GERMAN_WORDS_PRO already exists.");
            }
        }
    }

    private void insertDataIfNotExists() throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SELECT_ALL_SQL)) {
            if (!resultSet.next()) {
                @SuppressWarnings("unused")
                Random random = new Random();
                for (int i = 0; i < germanWords.length; i++) {
                    Vector<String> variations = generateVariations(germanWords[i]);
                    Collections.shuffle(variations);
                    try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL)) {
                        preparedStatement.setInt(1, i + 1);
                        preparedStatement.setString(2, germanWords[i]);
                        preparedStatement.setString(3, incorrectSpellings1[i]);
                        preparedStatement.setString(4, incorrectSpellings2[i]);
                        preparedStatement.setString(5, incorrectSpellings3[i]);
                        preparedStatement.executeUpdate();
                    }
                }
            }
        }
    }

    private void initializeGermanWords() {
        germanWords = new String[]{"Blume", "Auto", "Wein", "Burger", "Pizza", "Haus", "Hund", "Katze", "Tisch", "Stuhl",
                "Buch", "Fenster", "Telefon", "Schule", "Kuchen", "Wasser", "Stadt", "Park", "Baum", "Garten"};

        incorrectSpellings1 = new String[]{"Blumme", "Autoo", "Weein", "Burgger", "Pizaa", "Hauss", "Hunnd", "Katze", "Tish", "Stuhll",
                "Buchh", "Fennster", "Telefoon", "Schulee", "Kuche", "Wasserr", "Statd", "Parck", "Baumm", "Gartenn"};

        incorrectSpellings2 = new String[]{"Blum", "Atuo", "Weinn", "Burger", "Pizaa", "Haus", "Hundd", "Katze", "Tiscch", "Stuuhl",
                "Buchh", "Fennster", "Telefohn", "Schule", "Kuche", "Wasser", "Statd", "Parck", "Baum", "Gartenn"};

        incorrectSpellings3 = new String[]{"Blumm", "Auto", "Wein", "Burger", "Pizaa", "Hous", "Hund", "Kattze", "Tisc", "Stuhl",
                "Buch", "Fenstter", "Telefonn", "Schulee", "Kuchen", "Wassser", "Stat", "Parrk", "Bauum", "Garten"};
    }

    private Vector<String> generateVariations(String word) {
        Vector<String> variations = new Vector<>();
        variations.add(word); 
        for (int i = 0; i < 3; i++) {
            StringBuilder variation = new StringBuilder(word);
            int index = new Random().nextInt(variation.length());
            char randomChar = (char) ('a' + new Random().nextInt(26));
            variation.setCharAt(index, randomChar);
            variations.add(variation.toString());
        }
        return variations;
    }

    private void displayWords() {
        try {
            int randomId = new Random().nextInt(20) + 1;
            try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_RANDOM_SQL)) {
                preparedStatement.setInt(1, randomId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String correctSpelling = resultSet.getString("CORRECT_SPELLING");
                        String imagePath = "lib\\" + correctSpelling + ".png"; 
                        displayImage(imagePath); 
                        String[] options = {correctSpelling, resultSet.getString("INCORRECT_SPELLING1"),
                                resultSet.getString("INCORRECT_SPELLING2"), resultSet.getString("INCORRECT_SPELLING3")};
                        shuffleArray(options);
                        displayOptions(options);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayImage(String imagePath) {
        ImageIcon icon = new ImageIcon(imagePath);
        Image scaledImage = icon.getImage().getScaledInstance(600, 400, Image.SCALE_DEFAULT); 
        icon = new ImageIcon(scaledImage);
        imageLabel.setIcon(icon);
    }

    private void displayOptions(String[] options) {
      
        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText(options[i]);
            optionButtons[i].setVisible(true);
        }
    }

    private void handleWordSelection(String selectedWord) {
        if (Arrays.asList(germanWords).contains(selectedWord)) {
            score++;
            System.out.println("Correct word selected! Score: " + score);
        } else {
            System.out.println("Incorrect word selected! Score: " + score);
        }
    }

    private class OptionButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton button = (JButton) e.getSource();
            handleWordSelection(button.getText());
            currentRound++;
            if (currentRound > 10) {
                JOptionPane.showMessageDialog(null, "Game Over! Your score is: " + score);
                dispose();
            } else {
                displayWords();
            }
        }
    }

    private static void shuffleArray(String[] array) {
        Random rnd = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            String temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GuessTheSpelling());
    }
}
