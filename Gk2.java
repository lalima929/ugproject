import java.awt.*;
import javax.swing.*;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import java.util.Random;

public class Gk2 extends JFrame {
    private int totalScore = 0;
    private String playerName;
    private String gameMode; // New variable to store game mode

    private static final String[] WORDS_EASY = {
        "frog", "ant", "bear", "duck", "pig"
    };
    private static final String[] WORDS_HARD = {
        "java", "python", "developer", "computer", "zebra",
        "horse", "monkey", "panda"
    };
    private static final String[] EIMAGES = {
        "frog.jpg", "ant.jpg", "bear.jpg", "duck.png", "pig.jpg"};
        private static final String[] HIMAGES={
        "java.png", "python.png", "developer.png", "computer.png", "zebra.jpg",
        "horse.jpg", "monkey.jpg", "panda.png"
    };
    private static final String[] FRUITS = {"apple", "banana", "cherry", "date", "elderberry"};
    private static final String[] FRUIT_IMAGES = {"apple.jpg", "banana.jpg", "cherry.jpg", "date.jpg", "elderberry.jpg"};
    private static final String[] FLOWERS = {"rose", "tulip", "daisy", "lily", "sunflower"};
    private static final String[] FLOWER_IMAGES = {"rose.jpg", "tulip.jpg", "daisy.jpg", "lily.jpg", "sunflower.jpg"};
    private static final String[] NUMBER_WORDS_EASY = {
        "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
        "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen",
        "eighteen", "nineteen", "twenty", "twenty-one", "twenty-two", "twenty-three",
        "twenty-four", "twenty-five", "twenty-six", "twenty-seven", "twenty-eight",
        "twenty-nine", "thirty", "thirty-one", "thirty-two", "thirty-three", "thirty-four",
        "thirty-five", "thirty-six", "thirty-seven", "thirty-eight", "thirty-nine", "forty",
        "forty-one", "forty-two", "forty-three", "forty-four", "forty-five", "forty-six",
        "forty-seven", "forty-eight", "forty-nine", "fifty"
    };
    private static final String[] NUMBER_WORDS_HARD = {
        "fifty-one", "fifty-two", "fifty-three", "fifty-four", "fifty-five",
        "fifty-six", "fifty-seven", "fifty-eight", "fifty-nine", "sixty",
        "sixty-one", "sixty-two", "sixty-three", "sixty-four", "sixty-five",
        "sixty-six", "sixty-seven", "sixty-eight", "sixty-nine", "seventy",
        "seventy-one", "seventy-two", "seventy-three", "seventy-four", "seventy-five",
        "seventy-six", "seventy-seven", "seventy-eight", "seventy-nine", "eighty",
        "eighty-one", "eighty-two", "eighty-three", "eighty-four", "eighty-five",
        "eighty-six", "eighty-seven", "eighty-eight", "eighty-nine", "ninety",
        "ninety-one", "ninety-two", "ninety-three", "ninety-four", "ninety-five",
        "ninety-six", "ninety-seven", "ninety-eight", "ninety-nine", "one hundred"
    };

    private JLabel scoreLabel;
    private JPanel mainPanel;
    private JPanel imagePanel;

    // Database connection details
    private static final String URL = "jdbc:mysql://localhost:3306/games"; // Replace with your DB URL
    private static final String USER = "root"; // Replace with your DB username
    private static final String PASSWORD = "lalima@29"; // Replace with your DB password

    private int jumbledWordsTries;
    private int guessTheItemTries;
    private int numberInWordsTries;

    public Gk2() {
        setTitle("Game Menu");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        imagePanel = new JPanel();
        imagePanel.setPreferredSize(new Dimension(300, 300));
        imagePanel.setBackground(Color.WHITE); // To handle cases with no image

        JPanel buttonPanel = createButtonPanel();
        scoreLabel = new JLabel("Total Score: " + totalScore);

        mainPanel.add(scoreLabel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(imagePanel, BorderLayout.EAST);

        add(mainPanel);
        promptForPlayerName();
    }

    private void promptForPlayerName() {
        playerName = JOptionPane.showInputDialog(this, "Enter your name:");
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Anonymous";
        }
        promptForGameMode(); // Call to select game mode
    }

    private void promptForGameMode() {
        String[] modes = {"Child", "Adult"};
        gameMode = (String) JOptionPane.showInputDialog(
                this,
                "Select Category:",
                "Game Mode",
                JOptionPane.QUESTION_MESSAGE,
                null,
                modes,
                modes[0]
        );
        if (gameMode == null) {
            gameMode = "Child"; // Default to Easy if canceled
        }
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10)); // Center alignment with gaps

        JButton jumbledWordsButton = new JButton("Jumbled Words");
        JButton guessTheItemButton = new JButton("Guess the Item");
        JButton guessTheNumberButton = new JButton("Number in Words");
        JButton showHighScoresButton = new JButton("Show High Scores");
        JButton exitButton = new JButton("Exit");

        jumbledWordsButton.addActionListener(e -> playJumbledWords());
        guessTheItemButton.addActionListener(e -> playGuessTheItem());
        guessTheNumberButton.addActionListener(e -> playGuessTheNumber());
        showHighScoresButton.addActionListener(e -> showHighScores());
        exitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(jumbledWordsButton);
        buttonPanel.add(guessTheItemButton);
        buttonPanel.add(guessTheNumberButton);
        buttonPanel.add(showHighScoresButton);
        buttonPanel.add(exitButton);

        return buttonPanel;
    }

    private void updateScore(int score) {
        totalScore += score;
        scoreLabel.setText("Total Score: " + totalScore);
        saveHighScore(playerName, totalScore);
    }

    private void saveHighScore(String playerName, int score) {
        String query = "INSERT INTO games (player_name, score) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(URL,USER,PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, playerName);
            stmt.setInt(2, score);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showHighScores() {
        String query = "SELECT player_name, score FROM games ORDER BY score DESC LIMIT 10";

        try (Connection conn = DriverManager.getConnection(URL,USER,PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            StringBuilder highScores = new StringBuilder();
            highScores.append("High Scores:\n");
            while (rs.next()) {
                String name = rs.getString("player_name");
                int score = rs.getInt("score");
                highScores.append(name).append(": ").append(score).append("\n");
            }

            JOptionPane.showMessageDialog(this, highScores.toString(), "High Scores", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void playJumbledWords() {
        SwingUtilities.invokeLater(() -> {
            clearPanel();
    
            String[] wordsToUse = gameMode.equals("Child") ? WORDS_EASY : WORDS_HARD;
            int index = new Random().nextInt(wordsToUse.length);
            String wordToGuess = wordsToUse[index];
            String jumbledWord = jumbleWord(wordToGuess);
            String[] images = gameMode.equals("Child") ? EIMAGES : HIMAGES;
            jumbledWordsTries = 0;
    
            JTextField guessField = new JTextField(10);
            JButton submitButton = new JButton("Submit");
            JButton playAgainButton = new JButton("Play Again");
            JButton backToMenuButton = new JButton("Back to Menu");
            JLabel resultLabel = new JLabel();
            JLabel promptLabel = new JLabel("Guess the word: " + jumbledWord);
            JLabel triesLabel = new JLabel("Tries: " + jumbledWordsTries);
    
            submitButton.addActionListener(e -> {
                String userGuess = guessField.getText().trim();
                jumbledWordsTries++;
                triesLabel.setText("Tries: " + jumbledWordsTries);
                if (userGuess.equalsIgnoreCase(wordToGuess)) {
                    displayImage(images[index]);
                    resultLabel.setText("Correct! The word was: " + wordToGuess);
                    updateScore(10 - jumbledWordsTries);
                    imagePanel.add(playAgainButton);
                    imagePanel.add(backToMenuButton);
                } else {
                    resultLabel.setText("Wrong guess! Try again.");
                    if (jumbledWordsTries >= 5) {
                        resultLabel.setText("Sorry, you've used all your tries. The word was " + wordToGuess);
                        guessField.setText(""); // Reset for new guess
                        imagePanel.add(resultLabel); // Display final result
                    }
                }
                imagePanel.revalidate();
                imagePanel.repaint();
            });
    
            playAgainButton.addActionListener(e -> playJumbledWords());
            backToMenuButton.addActionListener(e -> resetGame());

            JPanel gamePanel = createGamePanel(promptLabel, guessField, submitButton, playAgainButton, backToMenuButton, resultLabel, triesLabel);
            imagePanel.add(gamePanel);
            imagePanel.revalidate();
            imagePanel.repaint();
        });
    }
    
    private void playGuessTheItem() {
        SwingUtilities.invokeLater(() -> {
            clearPanel();
    
            String[] itemsToUse = gameMode.equals("Child") ? FRUITS : FLOWERS;
            String[] imagesToUse = gameMode.equals("Child") ? FRUIT_IMAGES : FLOWER_IMAGES;
            int index = new Random().nextInt(itemsToUse.length);
            String itemToGuess = itemsToUse[index];
            String imagePath = imagesToUse[index];
            guessTheItemTries = 0;
    
            JTextField guessField = new JTextField(10);
            JButton submitButton = new JButton("Submit");
            JButton playAgainButton = new JButton("Play Again");
            JButton backToMenuButton = new JButton("Back to Menu");
            JLabel resultLabel = new JLabel();
            JLabel promptLabel = new JLabel("Guess the item: ");
            JLabel triesLabel = new JLabel("Tries: " + guessTheItemTries);
    
            displayImage(imagePath); // Show the image
    
            submitButton.addActionListener(e -> {
                String userGuess = guessField.getText().trim();
                guessTheItemTries++;
                triesLabel.setText("Tries: " + guessTheItemTries);
                if (userGuess.equalsIgnoreCase(itemToGuess)) {
                    resultLabel.setText("Correct! The item was: " + itemToGuess);
                    updateScore(10 - guessTheItemTries); // Scoring based on tries
                    imagePanel.add(playAgainButton);
                    imagePanel.add(backToMenuButton);
                } else {
                    resultLabel.setText("Wrong guess! Try again.");
                    if (guessTheItemTries >= 5) {
                        resultLabel.setText("Sorry, you've used all your tries. The item was " + itemToGuess);
                        guessField.setText(""); // Reset for new guess
                        imagePanel.add(resultLabel); // Display final result
                    }
                }
                imagePanel.revalidate();
                imagePanel.repaint();
            });
    
            playAgainButton.addActionListener(e -> playGuessTheNumber());
            backToMenuButton.addActionListener(e -> resetGame());

            JPanel gamePanel = createGamePanel(promptLabel, guessField, submitButton, playAgainButton, backToMenuButton, resultLabel, triesLabel);
            imagePanel.add(gamePanel);
            imagePanel.revalidate();
            imagePanel.repaint();
        });
    }
    
    private void playGuessTheNumber() {
        SwingUtilities.invokeLater(() -> {
            clearPanel();
    
            int numberToGuess = gameMode.equals("Child") ? new Random().nextInt(50) + 1 : new Random().nextInt(50) + 51;
            String numberInWords = gameMode.equals("Child") ? NUMBER_WORDS_EASY[numberToGuess - 1] : NUMBER_WORDS_HARD[numberToGuess - 51];
            numberInWordsTries = 0;
    
            JTextField guessField = new JTextField(10);
            JButton submitButton = new JButton("Submit");
            JButton playAgainButton = new JButton("Play Again");
            JButton backToMenuButton = new JButton("Back to Menu");
            JLabel resultLabel = new JLabel();
            JLabel promptLabel = new JLabel("What is the number " + numberToGuess + " in words?");
            JLabel triesLabel = new JLabel("Tries: " + numberInWordsTries);
    
            displayImage("numbers.jpg"); // Show a default image for numbers
    
            submitButton.addActionListener(e -> {
                numberInWordsTries++;
                triesLabel.setText("Tries: " + numberInWordsTries);
                String guess = guessField.getText().trim().toLowerCase();
                if (guess.equals(numberInWords)) {
                    resultLabel.setText("Correct! The number is " + numberInWords);
                    updateScore(10); // Fixed score for correct answer
                    imagePanel.add(playAgainButton);
                    imagePanel.add(backToMenuButton);
                } else {
                    resultLabel.setText("Sorry, that's not correct.");
                    if (numberInWordsTries >= 5) {
                        resultLabel.setText("You've used all your tries. The number was " + numberInWords);
                        guessField.setText(""); // Reset for new guess
                        imagePanel.add(resultLabel); // Display final result
                    }
                }
                imagePanel.revalidate();
                imagePanel.repaint();
            });
            
            playAgainButton.addActionListener(e -> playGuessTheNumber());
            backToMenuButton.addActionListener(e -> resetGame());

            JPanel gamePanel = createGamePanel(promptLabel, guessField, submitButton, playAgainButton, backToMenuButton, resultLabel, triesLabel);
            imagePanel.add(gamePanel);
            imagePanel.revalidate();
            imagePanel.repaint();
        });
    }
    
    
    private void clearPanel() {
        imagePanel.removeAll();
        imagePanel.revalidate();
        imagePanel.repaint();
    }

    private String jumbleWord(String word) {
        List<Character> letters = new ArrayList<>();
        for (char c : word.toCharArray()) {
            letters.add(c);
        }
        Collections.shuffle(letters);
        StringBuilder jumbledWord = new StringBuilder();
        for (char c : letters) {
            jumbledWord.append(c);
        }
        return jumbledWord.toString();
    }

    private void displayImage(String imagePath) {
        imagePanel.removeAll();
        try {
            Image image = ImageIO.read(getClass().getResource(imagePath));
            ImageIcon icon = new ImageIcon(image.getScaledInstance(300, 300, Image.SCALE_SMOOTH));
            imagePanel.add(new JLabel(icon));
        } catch (IOException e) {
            e.printStackTrace();
        }
        imagePanel.revalidate();
        imagePanel.repaint();
    }
    private JPanel createGamePanel(JLabel promptLabel, JTextField guessField, JButton submitButton, JButton playAgainButton, JButton backToMenuButton, JLabel resultLabel, JLabel triesLabel) {
        JPanel gamePanel = new JPanel();
        gamePanel.setLayout(new GridLayout(7, 1)); // Adjusted layout to fit all components
        gamePanel.add(promptLabel);
        gamePanel.add(guessField);
        gamePanel.add(submitButton);
        gamePanel.add(resultLabel);
        gamePanel.add(triesLabel);
        gamePanel.add(playAgainButton);
        gamePanel.add(backToMenuButton);
        return gamePanel;
    }

    private void resetGame() {
        totalScore = 0;
        scoreLabel.setText("Total Score: " + totalScore);
        promptForPlayerName();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Gk2 game = new Gk2();
            game.setVisible(true);
        });
    }
}
