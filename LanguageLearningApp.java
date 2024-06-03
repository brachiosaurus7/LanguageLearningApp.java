import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class LanguageLearningApp extends JFrame implements ActionListener {
    private JButton[] buttons;
    private JLabel tipLabel;
    private JLabel creditLabel;

    private String[] tips = {
            "Set Realistic Goals: Break down language learning into manageable goals that are achievable over a few months.",
            "Stay Motivated: Remind yourself why you are learning a language to maintain motivation.",
            "Focus on Specific Learning Objectives: Concentrate on what you want to learn and tailor your approach accordingly.",
            "Read for Pleasure: Reading in the target language exposes you to new vocabulary and grammatical structures.",
            "Immerse Yourself: Travel to places where the language is spoken to practice and enhance your skills.",
            "Engage with the Culture: Understanding the culture associated with the language aids in language mastery.",
            "Practice Listening and Speaking: Actively engage in listening and speaking activities to improve language skills.",
            "Learn Vocabulary in Context: Memorize vocabulary by associating words with contexts or situations.",
            "Revise Vocabulary Regularly: Regularly review new words to enhance retention and language proficiency.",
            "Share Your Goals: Communicate your language learning goals to others for support and accountability."
    };

    private String[] imageUrls = {
            "lib\\Image 1.png",
            "lib\\image 2.jpg",
            "lib\\Image 3.png",
            "lib\\Image 4.jpg"
    };

    private String[] gameNames = {
            "Hangman:",
            "Sentence builder:",
            "Translation Master:",
            "Spelling Whiz:"
    };

    private String[] gameDescriptions = {
            "Vocabulary Challenge",
            "Grammar Guru",
            "English to German word guesser",
            "Guess the correct spelling"
    };

    public LanguageLearningApp() {
        setTitle("LANGUAGE LEARNING APP");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1540, 820));
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        JLabel appLabel = new JLabel("Language Learning App (for German)");
        appLabel.setFont(new Font("Arial", Font.BOLD, 24));
        topPanel.add(appLabel);

        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        buttons = new JButton[4];
        for (int i = 0; i < 4; i++) {
            buttons[i] = new JButton();
            buttons[i].addActionListener(this);
            centerPanel.add(buttons[i]);
            loadButtonImage(buttons[i], imageUrls[i]);
            buttons[i].setText("<html><center>" + gameNames[i] + "<br>" + gameDescriptions[i] + "</center></html>");
            changeButtonColor(buttons[i]);
        }

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton logOutButton = new JButton("Exit Game");
        logOutButton.addActionListener(this);
        buttonPanel.add(logOutButton);

        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.add(Box.createHorizontalStrut(20), BorderLayout.LINE_END); 

        tipLabel = new JLabel();
        bottomPanel.add(tipLabel, BorderLayout.LINE_START);

        creditLabel = new JLabel("MADE BY HARSHIT SIDHER 0221BCA054 AND DHAANI SANGWAN 0221BCA060", SwingConstants.RIGHT);
        creditLabel.setForeground(Color.GRAY);
        bottomPanel.add(creditLabel, BorderLayout.PAGE_END);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        updateTip();

        pack();
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (int i = 0; i < buttons.length; i++) {
            if (e.getSource() == buttons[i]) {
                switch (i) {
                    case 0:
                        Hangman.main(new String[]{});
                        break;
                    case 1:
                        GermanSentenceFormationGame.main(new String[]{});
                        break;
                    case 2:
                        TranslationMaster.main(new String[]{});
                        break;
                    case 3:
                        GuessTheSpelling.main(new String[]{});
                        break;
                }
                break;
            }
        }

        if (e.getActionCommand().equals("Exit Game")) {
            System.exit(0); 
        }
    }

    private void updateTip() {
        int index = (int) (Math.random() * tips.length);
        tipLabel.setText("<html><body><p style='width: 450px;'> " + tips[index] + "</p></body></html>");
        tipLabel.setHorizontalAlignment(SwingConstants.LEFT);
        tipLabel.setVerticalAlignment(SwingConstants.BOTTOM);
    }

    private void loadButtonImage(JButton button, String imageUrl) {
        ImageIcon icon = new ImageIcon(imageUrl);
        Image img = icon.getImage();
        Image scaledImg = img.getScaledInstance(300, 300, Image.SCALE_SMOOTH);
        button.setIcon(new ImageIcon(scaledImg));
    }

    private void changeButtonColor(JButton button) {
        Random rand = new Random();
        int r = rand.nextInt(256);
        int g = rand.nextInt(256);
        int b = rand.nextInt(256);
        Color randomColor = new Color(r, g, b);
        button.setBackground(randomColor);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LanguageLearningApp::new);
    }
}
