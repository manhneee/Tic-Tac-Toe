import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SignedInMenu {
    private JFrame frame;
    private JTextField usernameField;

    public SignedInMenu(String username) {
        initialize(username);
    }

    private void initialize(String username) {
        // Create the main frame
        frame = new JFrame("Signed In Menu");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 250);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setLocationRelativeTo(null); // Center the window

        // Create panel for the form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username label and field (not editable)
        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(usernameLabel, gbc);

        usernameField = new JTextField(15);
        usernameField.setText(username);
        usernameField.setEditable(false);
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(usernameField, gbc);
        
        // Add form panel to frame
        frame.add(formPanel, BorderLayout.CENTER);

        DatabaseHelper dbHelper = new DatabaseHelper();
        int ranking = dbHelper.getRanking(username);
        JLabel rankingLabel = new JLabel("Ranking Score: " + ranking);
        rankingLabel.setFont(new Font("Arial", Font.BOLD, 14));
        rankingLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Add padding on top using an empty border
        rankingLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        frame.add(rankingLabel, BorderLayout.NORTH);
        
        // Create panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // Create Game button
        JButton createGameButton = new JButton("Create Game");
        createGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new CreateGameWindow(username);
                frame.dispose();
            }
        });
        buttonPanel.add(createGameButton);

        // Join Game button
        JButton joinGameButton = new JButton("Join Game");
        joinGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new JoinGameWindow(username);
                frame.dispose();
            }
        });
        buttonPanel.add(joinGameButton);

        //Scoreboard button
        JButton scoreboardButton = new JButton("Scoreboard");
        scoreboardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ScoreboardWindow(username);
            }
        });
        buttonPanel.add(scoreboardButton);

        // Logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(frame,
                        "Are you sure you want to log out?", "Confirm",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    frame.dispose();
                    new StartMenu();
                }
            }
        });
        buttonPanel.add(logoutButton);

        // Add button panel to frame
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Make the frame visible
        frame.setVisible(true);
    }

    public String getUsername() {
        return usernameField.getText().trim();
    }
}