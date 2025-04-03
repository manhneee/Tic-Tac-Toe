import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class TicTacToeClient implements ActionListener, Runnable {

    JFrame frame = new JFrame();
    JPanel titlePanel = new JPanel();
    JPanel buttonPanel = new JPanel();
    JLabel textField = new JLabel();
    JButton[] buttons = new JButton[100];

    Socket socket;
    BufferedReader in;
    PrintWriter out;

    int myPlayerId = 0;
    int currentTurn = 1;
    String myPlayerName;
    String currentName;

    public static void main(String[] args) {
        new TicTacToeClient().start();
    }

    public void start() {
        setupGUI();

        try {
            socket = new Socket("localhost", 12345); // Change to your server IP if needed
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            new Thread(this).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Could not connect to server");
            System.exit(1);
        }
    }

    private void setupGUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.setLayout(new BorderLayout());
        frame.setVisible(true);
        frame.setTitle("Tic Tac Toe Client");

        textField.setFont(new Font("Ink Free", Font.BOLD, 40));
        textField.setHorizontalAlignment(JLabel.CENTER);
        textField.setText("Connecting...");
        textField.setOpaque(true);
        textField.setBackground(Color.BLACK);
        textField.setForeground(Color.WHITE);

        titlePanel.setLayout(new BorderLayout());
        titlePanel.add(textField, BorderLayout.CENTER);

        buttonPanel.setLayout(new GridLayout(10, 10));

        for (int i = 0; i < 100; i++) {
            buttons[i] = new JButton();
            buttons[i].setFont(new Font("MV Boli", Font.BOLD, 40));
            buttons[i].setFocusable(false);
            buttons[i].setActionCommand(String.valueOf(i));
            buttons[i].addActionListener(this);
            buttonPanel.add(buttons[i]);
        }

        frame.add(titlePanel, BorderLayout.NORTH);
        frame.add(buttonPanel, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int index = Integer.parseInt(e.getActionCommand());
        int row = index / 10;
        int col = index % 10;

        if (buttons[index].getText().isEmpty() && currentTurn == myPlayerId) {
            out.println("MOVE " + row + " " + col);
        }
    }

    @Override
    public void run() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println("Received: " + msg);

                if (msg.startsWith("WELCOME")) {
                    myPlayerId = Integer.parseInt(msg.split(" ")[1]);
                    switch (myPlayerId) {
                        case 1:
                            myPlayerName = "X";
                            textField.setForeground(Color.BLUE);
                            break;
                        case 2:
                            myPlayerName = "Y";
                            textField.setForeground(Color.ORANGE);
                            break;
                        case 3:
                            myPlayerName = "A";
                            textField.setForeground(Color.GREEN);
                            break;
                        case 4:
                            myPlayerName = "B";
                            textField.setForeground(Color.RED);
                            break;
                        default:
                            break;
                    }
                    textField.setText("You are Player " + myPlayerName);
                    disableBoard();
                } else if (msg.startsWith("TURN")) {
                    currentTurn = Integer.parseInt(msg.split(" ")[1]);
                    switch (currentTurn) {
                        case 1:
                            currentName = "X";
                            break;
                        case 2:
                            currentName = "Y";
                            break;
                        case 3:
                            currentName = "A";
                            break;
                        case 4:
                            currentName = "B";
                            break;
                        default:
                            break;
                    }
                    if (currentTurn == myPlayerId) {
                        enableBoard();
                        textField.setText("Your turn (Player " + myPlayerName + ")");
                    } else {
                        enableBoard();
                        textField.setText("Player " + currentName + "'s turn");
                    }
                } else if (msg.startsWith("MOVE")) {
                    String[] parts = msg.split(" ");
                    int row = Integer.parseInt(parts[1]);
                    int col = Integer.parseInt(parts[2]);
                    String symbol = parts[3];
                    int index = row * 10 + col;

                    buttons[index].setText(symbol);
                    switch (symbol) {
                        case "X" -> buttons[index].setForeground(Color.BLUE);
                        case "Y" -> buttons[index].setForeground(Color.ORANGE);
                        case "A" -> buttons[index].setForeground(Color.GREEN);
                        case "B" -> buttons[index].setForeground(Color.RED);
                    }
                } else if (msg.startsWith("WIN")) {
                    String[] parts = msg.split(" ");
                    int winner = Integer.parseInt(parts[1]);
                    String winningPlayer = null;
                    switch (winner) {
                        case 1:
                            winningPlayer = "X";
                            break;
                        case 2:
                            winningPlayer = "Y";
                            break;
                        case 3:
                            winningPlayer = "A";
                            break;
                        case 4:
                            winningPlayer = "B";
                            break;
                        default:
                            break;
                    }
                    textField.setText("Player " + winningPlayer + " wins!");

                    Color winColor = switch (winner) {
                        case 1 -> Color.CYAN;
                        case 2 -> Color.ORANGE;
                        case 3 -> Color.GREEN;
                        case 4 -> Color.RED;
                        default -> Color.YELLOW;
                    };

                    for (int i = 2; i < parts.length; i += 2) {
                        int row = Integer.parseInt(parts[i]);
                        int col = Integer.parseInt(parts[i + 1]);
                        int index = row * 10 + col;

                        buttons[index].setBackground(winColor); // highlight winner
                    }

                    disableBoard();
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Connection lost.");
            System.exit(1);
        }
    }

    private void disableBoard() {
        for (JButton button : buttons) {
            button.setEnabled(false);
        }
    }

    private void enableBoard() {
        for (JButton button : buttons) {
            if (button.getText().isEmpty()) {
                button.setEnabled(true);
            }
        }
    }
}
