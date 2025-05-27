import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StartMenu {
    private JFrame frame;
    private DatabaseHelper dbHelper = new DatabaseHelper();

    public StartMenu() {
        initialize();
    }

    private void initialize() {
        // Create the main frame
        frame = new JFrame("Game Start Menu");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(350, 350);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setLocationRelativeTo(null); // Center the window

        // Create panel for buttons
        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 80, 20, 80));

        // Play As Guest button
        JButton guestButton = new JButton("Play As Guest");
        guestButton.addActionListener(e -> {
            String guestName = "Guest" + (int) (Math.random() * 10000);
            frame.dispose();
            new SignedInMenu(guestName);
        });
        buttonPanel.add(guestButton);

        // Register button
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> {
            JTextField userField = new JTextField();
            JPasswordField passField = new JPasswordField();
            Object[] fields = {
                    "Username:", userField,
                    "Password:", passField
            };
            int option = JOptionPane.showConfirmDialog(frame, fields, "Register", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                String user = userField.getText().trim();
                String pass = new String(passField.getPassword());
                if (user.isEmpty() || pass.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter a username and password", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (dbHelper.registerUser(user, pass)) {
                    JOptionPane.showMessageDialog(frame, "Registration successful!");
                } else {
                    JOptionPane.showMessageDialog(frame, "Registration failed (username may exist)", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        buttonPanel.add(registerButton);

        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> {
            JTextField userField = new JTextField();
            JPasswordField passField = new JPasswordField();
            Object[] fields = {
                    "Username:", userField,
                    "Password:", passField
            };
            int option = JOptionPane.showConfirmDialog(frame, fields, "Login", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                String user = userField.getText().trim();
                String pass = new String(passField.getPassword());
                if (user.isEmpty() || pass.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter a username and password", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (dbHelper.loginUser(user, pass)) {
                    JOptionPane.showMessageDialog(frame, "Login successful!");
                    frame.dispose(); // Close StartMenu window
                    new SignedInMenu(user); // Open new SignedInMenu window
                } else {
                    JOptionPane.showMessageDialog(frame, "Login failed", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        buttonPanel.add(loginButton);

        // Scoreboard button
        JButton scoreboardButton = new JButton("Scoreboard");
        scoreboardButton.addActionListener(e -> {
            new ScoreboardWindow();
        });
        buttonPanel.add(scoreboardButton);

        // Quit button
        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(frame,
                        "Are you sure you want to quit?", "Confirm Quit",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
        buttonPanel.add(quitButton);

        // Add button panel to frame
        frame.add(buttonPanel, BorderLayout.CENTER);

        // Make the frame visible
        frame.setVisible(true);
    }
}