import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StartMenu {
    private JFrame frame;
    private JTextField usernameField;

    public StartMenu() {
        initialize();
    }

    private void initialize() {
        // Create the main frame
        frame = new JFrame("Game Start Menu");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setLocationRelativeTo(null); // Center the window

        // Create panel for the form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username label and field
        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(usernameLabel, gbc);

        usernameField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(usernameField, gbc);

        // Add form panel to frame
        frame.add(formPanel, BorderLayout.CENTER);

        // Create panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // Create Game button
        JButton createGameButton = new JButton("Create Game");
        createGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText().trim();
                if (!username.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Creating game for: " + username);
                    new CreateGameWindow(username);
                    frame.dispose();
                } else {
                    JOptionPane.showMessageDialog(frame, "Please enter a username", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        buttonPanel.add(createGameButton);

        // Join Game button
        JButton joinGameButton = new JButton("Join Game");
        joinGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText().trim();
                if (!username.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Joining game as: " + username);
                    new JoinGameWindow(username);
                    frame.dispose();
                } else {
                    JOptionPane.showMessageDialog(frame, "Please enter a username", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        buttonPanel.add(joinGameButton);

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
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Make the frame visible
        frame.setVisible(true);
    }

    public String getUsername() {
        return usernameField.getText().trim();
    }
}