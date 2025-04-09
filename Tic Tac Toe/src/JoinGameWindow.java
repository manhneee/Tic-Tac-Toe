import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class JoinGameWindow {
    private JFrame frame;
    private JTextField addressField;
    private JTextField portField;

    public JoinGameWindow(String username) {
        // Create the main frame
        frame = new JFrame("Join Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setLocationRelativeTo(null); // Center the window

        // Create panel for the form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Address label and field
        JLabel addressLabel = new JLabel("Address:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(addressLabel, gbc);

        addressField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(addressField, gbc);

        // PORT label and field
        JLabel portLabel = new JLabel("Port:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(portLabel, gbc);

        portField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(portField, gbc);

        // Add form panel to frame
        frame.add(formPanel, BorderLayout.CENTER);

        // Create panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // Connect button
        JButton ConnectButton = new JButton("Connect");
        ConnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String addsv = addressField.getText().trim();
                int portsv = Integer.parseInt(portField.getText().trim());
                if (!addsv.isEmpty() && !portField.getText().trim().isEmpty()) {
                    new TicTacToeClient(addsv, portsv, username);
                    frame.dispose(); // Close the join game window
                } else {
                    JOptionPane.showMessageDialog(frame, "Please enter an IP and port", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        buttonPanel.add(ConnectButton);

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
}
