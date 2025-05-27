import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class CreateGameWindow {
    private JFrame frame;
    private JTextField addressField;
    private JTextField portField;
    private JTextField timerField;
    private JTextField wincountField;
    private TicTacToeServer server;

    public CreateGameWindow(String username) {
        // Create the main frame
        frame = new JFrame("Create Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setLocationRelativeTo(null); // Center the window

        // Create panel for the form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Address label and field (auto-filled and disabled)
        JLabel addressLabel = new JLabel("Your IP:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(addressLabel, gbc);

        addressField = new JTextField(20);
        addressField.setEditable(false);
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            addressField.setText(ip);
        } catch (UnknownHostException e) {
            addressField.setText("Unavailable");
        }
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(addressField, gbc);

        // Port label and field
        JLabel portLabel = new JLabel("Port:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(portLabel, gbc);

        portField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(portField, gbc);

        // Timer label and field
        JLabel timerLabel = new JLabel("Timer:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(timerLabel, gbc);

        timerField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(timerField, gbc);

        // Win count label and field
        JLabel wincountLabel = new JLabel("Win count:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(wincountLabel, gbc);

        wincountField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 3;
        formPanel.add(wincountField, gbc);

        // Add form panel to frame
        frame.add(formPanel, BorderLayout.CENTER);

        // Create panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // Create button
        JButton createButton = new JButton("Create");
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String portText = portField.getText().trim();
                String timerText = timerField.getText().trim();
                String wincountText = wincountField.getText().trim();
                if (portText.isEmpty() || timerText.isEmpty() || wincountText.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter all fields", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    int port = Integer.parseInt(portText);
                    int timer = Integer.parseInt(timerText);
                    int wincount = Integer.parseInt(wincountText);

                    // Start server in a new thread
                    new Thread(() -> {
                        try {
                            server = new TicTacToeServer(port, wincount, timer);
                            server.start(); // start accepting clients
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(frame, "Server failed to start: " + ex.getMessage(),
                                    "Server Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }).start();

                    // Short delay to ensure server starts before client connects
                    Thread.sleep(2000);

                    // Automatically connect the user as the first client
                    new TicTacToeClient("localhost", port, username);
                    frame.dispose(); // Close the create game window

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid port number", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Unexpected error: " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        buttonPanel.add(createButton);

        // Cancel button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            frame.dispose(); // Close the create game window
            new SignedInMenu(username); // Open the signed-in menu

        });
        buttonPanel.add(cancelButton);

        // Add button panel to frame
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Make the frame visible
        frame.setVisible(true);
    }
}
