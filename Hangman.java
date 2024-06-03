import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.*;
import java.sql.*;
import java.util.Random;

@SuppressWarnings("unused")
public class Hangman extends JFrame {
    private static final int MAX_WRONG_GUESSES = 6;

    private String currentWord;
    private StringBuilder guessedWord;
    private int wrongGuesses;

    private JLabel wordLabel;
    private JTextArea statusArea;
    private JButton[] letterButtons;
    private JButton restartButton;
    private JTextArea hangmanImageArea;

    public Hangman(String url, String username, String password) {
        super("German Word Game");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(240, 240, 240));
    
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wordLabel = new JLabel("", SwingConstants.CENTER);
        wordLabel.setFont(new Font("Arial", Font.BOLD, 24));
        topPanel.add(wordLabel);
        add(topPanel, BorderLayout.NORTH);
    
        JPanel centerPanel = new JPanel(new GridLayout(3, 8, 5, 5));
        centerPanel.setBackground(new Color(240, 240, 240));
        letterButtons = new JButton[26];
        for (int i = 0; i < letterButtons.length; i++) {
            char letter = (char) ('A' + i);
            letterButtons[i] = new JButton(String.valueOf(letter));
            letterButtons[i].setFont(new Font("Arial", Font.PLAIN, 18));
            letterButtons[i].addActionListener(new LetterButtonListener());
            letterButtons[i].setBackground(Color.GRAY); 
            letterButtons[i].setForeground(Color.WHITE); 
            centerPanel.add(letterButtons[i]);
        }
        add(centerPanel, BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    
        JPanel bottomPanel = new JPanel(new BorderLayout());
        statusArea = new JTextArea(7, 20);
        statusArea.setFont(new Font("Arial", Font.PLAIN, 14));
        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        JScrollPane statusScrollPane = new JScrollPane(statusArea);
        bottomPanel.add(statusScrollPane, BorderLayout.CENTER);
    
        restartButton = new JButton("Restart");
        restartButton.setFont(new Font("Arial", Font.BOLD, 18));
        restartButton.addActionListener(new RestartButtonListener());
        restartButton.setVisible(false);
        bottomPanel.add(restartButton, BorderLayout.EAST);
    
        hangmanImageArea = new JTextArea(7, 20);
        hangmanImageArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        hangmanImageArea.setEditable(false);
        hangmanImageArea.setLineWrap(true);
        hangmanImageArea.setWrapStyleWord(true);
        JScrollPane hangmanScrollPane = new JScrollPane(hangmanImageArea);
        bottomPanel.add(hangmanScrollPane, BorderLayout.SOUTH);
    
        add(bottomPanel, BorderLayout.SOUTH);
        addKeyListener(new KeyboardInputListener());
        initializeGame(url, username, password);
        
        setVisible(true);
        addKeyListener(new KeyboardInputListener());
    }


    private void initializeGame(String url, String username, String password) {
        createTableAndDataIfNeeded(url, username, password);
        currentWord = getRandomWordFromDatabase(url, username, password);
        guessedWord = new StringBuilder();
        for (int i = 0; i < currentWord.length(); i++) {
            guessedWord.append("_");
        }
        wordLabel.setText(guessedWord.toString());
        wrongGuesses = 0;
        updateHangmanImage();
        appendStatus("Type in the characters");
        enableLetterButtons(true);
    }

    private void createTableAndDataIfNeeded(String url, String username, String password) {
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            if (!tableExists(connection, "GERMAN_WORDS")) {
                createGermanWordsTable(connection);
                addGermanWords(connection);
            } else {
                if (getTotalRows(connection) < 10) {
                    addGermanWords(connection);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Exception: " + e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
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
                            "WORD VARCHAR(50))"
            );
        }
    }

    private void addGermanWords(Connection connection) throws SQLException {
        String[] words = {
            "AUTOMOBIL",
            "TISCH",
            "BUCH",
            "STUHL",
            "FENSTER",
            "HAUS",
            "SCHULE",
            "KATZE",
            "WASSER",
            "COMPUTER"
        };

        String query = "INSERT INTO GERMAN_WORDS (WORD) VALUES (?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for (String word : words) {
                preparedStatement.setString(1, word);
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

    private String getRandomWordFromDatabase(String url, String username, String password) {
        String randomWord = null;
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String query = "SELECT WORD FROM GERMAN_WORDS ORDER BY DBMS_RANDOM.VALUE FETCH FIRST 1 ROWS ONLY";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    randomWord = resultSet.getString("WORD").toUpperCase();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Exception: " + e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
        }
        return randomWord;
    }

    private void updateGuessedWord(char letter) {
        boolean letterFound = false;
        for (int i = 0; i < currentWord.length(); i++) {
            if (currentWord.charAt(i) == letter) {
                guessedWord.setCharAt(i, letter);
                letterFound = true;
            }
        }
        if (!letterFound) {
            wrongGuesses++;
        }
        wordLabel.setText(guessedWord.toString());
        checkGameStatus();
    }

    private void checkGameStatus() {
        if (guessedWord.toString().equals(currentWord)) {
            appendStatus("You win! The word was: " + currentWord);
            disableLetterButtons();
            restartButton.setVisible(true);
        } else if (wrongGuesses >= MAX_WRONG_GUESSES) {
            appendStatus("You lose. The word was: " + currentWord);
            disableLetterButtons();
            restartButton.setVisible(true);
        } else {
            updateHangmanImage();
        }
    }

    private void updateHangmanImage() {
        String[] hangmanImages = {
            "  +---+\n" +
            "  |   |\n" +
            "      |\n" +
            "      |\n" +
            "      |\n" +
            "      |\n" +
            "=========",
            "  +---+\n" +
            "  |   |\n" +
            "  O   |\n" +
            "      |\n" +
            "      |\n" +
            "      |\n" +
            "=========",
            "  +---+\n" +
            "  |   |\n" +
            "  O   |\n" +
            "  |   |\n" +
            "      |\n" +
            "      |\n" +
            "=========",
            "  +---+\n" +
            "  |   |\n" +
            "  O   |\n" +
            " /|   |\n" +
            "      |\n" +
            "      |\n" +
            "=========",
            "  +---+\n" +
            "  |   |\n" +
            "  O   |\n" +
            " /|\\  |\n" +
            "      |\n" +
            "      |\n" +
            "=========",
            "  +---+\n" +
            "  |   |\n" +
            "  O   |\n" +
            " /|\\  |\n" +
            " /    |\n" +
            "      |\n" +
            "=========",
            "  +---+\n" +
            "  |   |\n" +
            "  O   |\n" +
            " /|\\  |\n" +
            " / \\  |\n" +
            "      |\n" +
            "========="
        };

        String hangman = wrongGuesses <= MAX_WRONG_GUESSES ? hangmanImages[wrongGuesses] : hangmanImages[MAX_WRONG_GUESSES];
        hangmanImageArea.setText(hangman);
    }

    private void enableLetterButtons(boolean enabled) {
        for (JButton button : letterButtons) {
            button.setEnabled(enabled);
        }
    }

    private void disableLetterButtons() {
        enableLetterButtons(false);
    }

    private class LetterButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton button = (JButton) e.getSource();
            button.setEnabled(false);
            char letter = button.getText().charAt(0);
            updateGuessedWord(letter);
        }
    }

    private class RestartButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            restartGame();
        }
    }

    private void restartGame() {
        String url = Main.getUrl();
        String username = Main.getUsername();
        String password = Main.getPassword();
        initializeGame(url, username, password);
        restartButton.setVisible(false);
    }

    private void appendStatus(String text) {
        statusArea.append(text + "\n");
    }

    private class KeyboardInputListener implements KeyListener {
        @Override
        public void keyPressed(KeyEvent e) {
            char keyChar = e.getKeyChar();
            if (Character.isLetter(keyChar)) {
                char letter = Character.toUpperCase(keyChar);
                updateGuessedWord(letter);
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }
    }

    public static void main(String[] args) {
        String url = Main.getUrl();
        String username = Main.getUsername();
        String password = Main.getPassword();

        SwingUtilities.invokeLater(() -> new Hangman(url, username, password));
    }
}

