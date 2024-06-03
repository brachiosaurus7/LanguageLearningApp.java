import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TranslationMaster extends JFrame {
    private static final int GAME_ITERATIONS = 10;
    
    private int currentRound = 0;
    private int score = 0;

    private JLabel wordLabel;
    private JButton[] optionButtons;
    private JLabel scoreLabel;
    private JButton restartButton;

    String url, username, password;

    public TranslationMaster() {
        super("Pick the appropriate translation");
        
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(230, 230, 230));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(50, 50, 50));
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        wordLabel = new JLabel("", SwingConstants.CENTER);
        wordLabel.setForeground(Color.WHITE);
        wordLabel.setFont(new Font("Arial", Font.BOLD, 28));
        topPanel.add(wordLabel, BorderLayout.CENTER);

        restartButton = new JButton("Restart");
        restartButton.setFont(new Font("Arial", Font.PLAIN, 18));
        restartButton.setVisible(false);
        restartButton.addActionListener(new RestartButtonListener());
        topPanel.add(restartButton, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        buttonPanel.setBackground(new Color(230, 230, 230));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        optionButtons = new JButton[4];
        for (int i = 0; i < optionButtons.length; i++) {
            optionButtons[i] = new JButton();
            optionButtons[i].setFont(new Font("Arial", Font.PLAIN, 18));
            optionButtons[i].setBackground(new Color(100, 100, 100));
            optionButtons[i].setForeground(Color.WHITE); 
            optionButtons[i].addActionListener(new OptionButtonListener());
            buttonPanel.add(optionButtons[i]);
        }
        add(buttonPanel, BorderLayout.CENTER);

        scoreLabel = new JLabel("", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 20)); 
        scoreLabel.setForeground(Color.BLACK); 
        scoreLabel.setVisible(false);
        add(scoreLabel, BorderLayout.SOUTH);

        url = Main.getUrl();
        username = Main.getUsername();
        password = Main.getPassword();

        createTableAndDataIfNeeded();

        loadNextRound();

        setVisible(true);
    }

    private void createTableAndDataIfNeeded() {
        try (Connection c = DriverManager.getConnection(url, username, password)) {
            if (!tableExists(c, "GERMAN_WORDS")) {
                createGermanWordsTable(c);
                addGermanWords(c);
            } else {
                if (getTotalRows(c) < GAME_ITERATIONS) {
                    addGermanWords(c);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet resultSet = meta.getTables(null, null, tableName, new String[]{"TABLE"});
        return resultSet.next();
    }

    private void createGermanWordsTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE GERMAN_WORDS (" +
                            "WORD VARCHAR2(50), " +
                            "MEANING VARCHAR2(50))"
            );
        }
    }

    private void addGermanWords(Connection connection) throws SQLException {
        String[] words = {
                "Haus", "Auto", "Baum", "Hund", "Katze",
                "Tisch", "Stuhl", "Blume", "Wasser", "Apfel"
        };


        String[] meanings = {
                "house", "car", "tree", "dog", "cat",
                "table", "chair","flower", "water", "apple"
            };
    
            String query = "INSERT INTO GERMAN_WORDS (WORD, MEANING) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                for (int i = 0; i < words.length; i++) {
                    preparedStatement.setString(1, words[i]);
                    preparedStatement.setString(2, meanings[i]);
                    preparedStatement.executeUpdate();
                }
            }
        }
    
        private int getTotalRows(Connection connection) throws SQLException {
            String query = "SELECT COUNT(*) FROM GERMAN_WORDS";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
                return 0;
            }
        }
    
        private void loadNextRound() {
            if (currentRound < GAME_ITERATIONS) {
                currentRound++;
                try (Connection c = DriverManager.getConnection(url, username, password)) {
                    String germanWord = getRandomGermanWord(c);
                    wordLabel.setText("<html><div style='text-align: center;'>" + germanWord + "</div></html>");
    
                    List<String> meanings = getMeaningOptions(c, germanWord);
                    Collections.shuffle(meanings);
    
                    for (int i = 0; i < optionButtons.length; i++) {
                        optionButtons[i].setText(meanings.get(i));
                    }
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                endGame();
            }
        }
    
        private String getRandomGermanWord(Connection connection) throws SQLException {
            String query = "SELECT WORD FROM GERMAN_WORDS ORDER BY DBMS_RANDOM.VALUE FETCH FIRST 1 ROWS ONLY";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getString("WORD");
                } else {
                    throw new SQLException("No German words found in the table.");
                }
            }
        }
    
        private List<String> getMeaningOptions(Connection connection, String germanWord) throws SQLException {
            String query = "SELECT MEANING FROM GERMAN_WORDS WHERE WORD != ? ORDER BY DBMS_RANDOM.VALUE FETCH FIRST 3 ROWS ONLY";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, germanWord);
                ResultSet resultSet = preparedStatement.executeQuery();
                List<String> meanings = new ArrayList<>();
                meanings.add(getMeaningForWord(connection, germanWord));
    
                while (resultSet.next()) {
                    meanings.add(resultSet.getString("MEANING"));
                }
    
                return meanings;
            }
        }
    
        private String getMeaningForWord(Connection connection, String germanWord) throws SQLException {
            String query = "SELECT MEANING FROM GERMAN_WORDS WHERE WORD = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, germanWord);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getString("MEANING");
                } else {
                    throw new SQLException("Meaning not found for word: " + germanWord);
                }
            }
        }
    
        private class OptionButtonListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                JButton button = (JButton) e.getSource();
                String selectedMeaning = button.getText();
                try (Connection c = DriverManager.getConnection(url, username, password)) {
                    String germanWord = wordLabel.getText().replaceAll("<.*?>", ""); 
                    String correctMeaning = getMeaningForWord(c, germanWord);
                    if (selectedMeaning.equals(correctMeaning)) {
                        score++;
                        JOptionPane.showMessageDialog(TranslationMaster.this, "Correct!", "Result", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(TranslationMaster.this, "Incorrect!", "Result", JOptionPane.ERROR_MESSAGE);
                    }
                    loadNextRound();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(TranslationMaster.this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    
        private class RestartButtonListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                restartGame();
            }
        }
    
        private void restartGame() {
            currentRound = 0;
            score = 0;
            scoreLabel.setVisible(false);
            restartButton.setVisible(false);
            for (JButton button : optionButtons) {
                button.setEnabled(true);
            }
            loadNextRound();
        }
    
        private void endGame() {
            JOptionPane.showMessageDialog(this, "Game Over! Your final score: " + score, "Game Over", JOptionPane.INFORMATION_MESSAGE);
            scoreLabel.setText("Final Score: " + score);
            scoreLabel.setVisible(true);
            for (JButton button : optionButtons) {
                button.setEnabled(false); 
            }
            restartButton.setVisible(true);
        }
    
        public static void main(String[] args) {
            SwingUtilities.invokeLater(TranslationMaster::new);
        }
    }
    
