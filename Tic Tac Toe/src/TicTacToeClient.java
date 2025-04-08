import javax.swing.*;
import javax.swing.border.EmptyBorder;

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
    JButton readyButton = new JButton();

    Socket socket;
    BufferedReader in;
    PrintWriter out;

    int myPlayerId = 0;
    int currentTurn = 1;
    String myPlayerSymbol;
    String myPlayerName;
    String currentSymbol;
    String currentName;
    int totalPlayers;

    public TicTacToeClient(String serverAddress, int PORT, String username) {
        setupGUI();
        myPlayerName = username;

        try {
            socket = new Socket(serverAddress, PORT);
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

        readyButton.setText("Ready");
        readyButton.setFont(new Font("Arial", Font.BOLD, 20));
        readyButton.setFocusable(false);
        readyButton.setBackground(new Color(76, 175, 80)); // Green color
        readyButton.setForeground(Color.WHITE);
        readyButton.setBorder(new EmptyBorder(10, 20, 10, 20));
        readyButton.addActionListener(_ -> {
            out.println("READY " + myPlayerName);
            readyButton.setEnabled(false);
            readyButton.setText("Waiting...");
            readyButton.setBackground(new Color(244, 67, 54)); // Red color when waiting
        });

        titlePanel.add(textField, BorderLayout.CENTER);
        titlePanel.add(readyButton, BorderLayout.EAST);

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

                if (msg.startsWith("START")) {
                    SwingUtilities.invokeLater(() -> {
                        readyButton.setVisible(false); // Hide the ready button when game starts
                    });
                    totalPlayers = Integer.parseInt(msg.split(" ")[1]);
                    break;
                } else if (msg.startsWith("WAITING_FOR_READY")) {
                    SwingUtilities.invokeLater(() -> {
                        readyButton.setEnabled(true);
                        textField.setText("Press Ready when prepared");
                    });
                } else if (msg.startsWith("WELCOME")) {
                    myPlayerId = Integer.parseInt(msg.split(" ")[1]);
                    switch (totalPlayers) {
                        case 2:
                            switch (myPlayerId) {
                                case 1:
                                    myPlayerSymbol = "X";
                                    textField.setForeground(Color.RED);
                                    break;
                                case 2:
                                    myPlayerSymbol = "O";
                                    textField.setForeground(Color.BLUE);
                                    break;
                                default:
                                    break;
                            }
                            break;
                        case 3:
                            switch (myPlayerId) {
                                case 1:
                                    myPlayerSymbol = "X";
                                    textField.setForeground(Color.GREEN);
                                    break;
                                case 2:
                                    myPlayerSymbol = "Y";
                                    textField.setForeground(Color.BLUE);
                                    break;
                                case 3:
                                    myPlayerSymbol = "Z";
                                    textField.setForeground(Color.RED);
                                    break;
                                default:
                                    break;
                            }
                            break;
                        case 4:
                            switch (myPlayerId) {
                                case 1:
                                    myPlayerSymbol = "X";
                                    textField.setForeground(Color.BLUE);
                                    break;
                                case 2:
                                    myPlayerSymbol = "Y";
                                    textField.setForeground(Color.ORANGE);
                                    break;
                                case 3:
                                    myPlayerSymbol = "A";
                                    textField.setForeground(Color.GREEN);
                                    break;
                                case 4:
                                    myPlayerSymbol = "B";
                                    textField.setForeground(Color.RED);
                                    break;
                                default:
                                    break;
                            }
                            break;
                        default:
                            break;
                    }
                    textField.setText("Welcome " + myPlayerName + " (" + myPlayerSymbol + ")");
                    disableBoard();
                } else if (msg.startsWith("TURN")) {
                    currentTurn = Integer.parseInt(msg.split(" ")[1]);
                    currentName = msg.split(" ")[2];
                    switch (totalPlayers) {
                        case 2:
                            switch (currentTurn) {
                                case 1:
                                    currentSymbol = "X";
                                    break;
                                case 2:
                                    currentSymbol = "O";
                                    break;
                                default:
                                    break;
                            }
                            break;
                        case 3:
                            switch (currentTurn) {
                                case 1:
                                    currentSymbol = "X";
                                    break;
                                case 2:
                                    currentSymbol = "Y";
                                    break;
                                case 3:
                                    currentSymbol = "Z";
                                    break;
                                default:
                                    break;
                            }
                            break;
                        case 4:
                            switch (currentTurn) {
                                case 1:
                                    currentSymbol = "X";
                                    break;
                                case 2:
                                    currentSymbol = "Y";
                                    break;
                                case 3:
                                    currentSymbol = "A";
                                    break;
                                case 4:
                                    currentSymbol = "B";
                                    break;
                                default:
                                    break;
                            }
                            break;
                        default:
                            break;
                    }
                    if (currentTurn == myPlayerId) {
                        enableBoard();
                        textField.setText("Your turn (" + myPlayerSymbol + ")");
                    } else {
                        enableBoard();
                        textField.setText(currentName + "'s turn");
                    }
                } else if (msg.startsWith("MOVE")) {
                    String[] parts = msg.split(" ");
                    int row = Integer.parseInt(parts[1]);
                    int col = Integer.parseInt(parts[2]);
                    String symbol = parts[3];
                    int index = row * 10 + col;

                    buttons[index].setText(symbol);
                    switch (totalPlayers) {
                        case 2:
                            switch (symbol) {
                                case "X" -> buttons[index].setForeground(Color.RED);
                                case "O" -> buttons[index].setForeground(Color.BLUE);
                            }
                            break;
                        case 3:
                            switch (symbol) {
                                case "X" -> buttons[index].setForeground(Color.GREEN);
                                case "Y" -> buttons[index].setForeground(Color.BLUE);
                                case "Z" -> buttons[index].setForeground(Color.RED);
                            }
                            break;
                        case 4:
                            switch (symbol) {
                                case "X" -> buttons[index].setForeground(Color.BLUE);
                                case "Y" -> buttons[index].setForeground(Color.ORANGE);
                                case "A" -> buttons[index].setForeground(Color.GREEN);
                                case "B" -> buttons[index].setForeground(Color.RED);
                            }
                            break;
                        default:
                            break;
                    }
                } else if (msg.startsWith("WIN")) {
                    String[] parts = msg.split(" ");
                    int winner = Integer.parseInt(parts[1]);
                    String winningPlayer = msg.split(" ")[2];
                    if (winner == myPlayerId) {
                        textField.setText("You win!");
                    } else {
                        textField.setText(winningPlayer + " wins!");
                    }

                    Color winColor = Color.WHITE;

                    switch (totalPlayers) {
                        case 2:
                            winColor = switch (winner) {
                                case 1 -> Color.RED;
                                case 2 -> Color.BLUE;
                                default -> Color.WHITE;
                            };
                            break;
                        case 3:
                            winColor = switch (winner) {
                                case 1 -> Color.GREEN;
                                case 2 -> Color.BLUE;
                                case 3 -> Color.RED;
                                default -> Color.WHITE;
                            };
                            break;
                        case 4:
                            winColor = switch (winner) {
                                case 1 -> Color.BLUE;
                                case 2 -> Color.ORANGE;
                                case 3 -> Color.GREEN;
                                case 4 -> Color.RED;
                                default -> Color.WHITE;
                            };
                        default:
                            break;
                    }

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
