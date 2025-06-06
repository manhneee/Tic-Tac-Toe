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
    String myPlayerName;
    String currentName;
    int totalPlayers;
    int timer;
    int timeLeft;
    Timer clock;
    private boolean gameEnded = false;

    public TicTacToeClient(String serverAddress, int PORT, String username) {
        setupGUI();
        this.myPlayerName = username;

        try {
            socket = new Socket(serverAddress, PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            new Thread(this).start();
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> {
                int option = JOptionPane.showOptionDialog(
                    frame,
                    "\"Could not connect to server\"",
                    "Connection Error",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE,
                    null,
                    new Object[]{"Back to Menu", "Exit"},
                    "Back to Menu"
                );
                if (option == JOptionPane.YES_OPTION) {
                    frame.dispose();
                    new SignedInMenu(myPlayerName);
                } else if (option == JOptionPane.NO_OPTION) {
                    System.exit(1);
                }
            });
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
                    totalPlayers = Integer.parseInt(msg.split(" ")[1]);
                    timer = Integer.parseInt(msg.split(" ")[2]);
                    enableBoard();
                } else if (msg.startsWith("WELCOME")) {
                    myPlayerId = Integer.parseInt(msg.split(" ")[1]);
                } else if (msg.startsWith("WAITING_FOR_READY")) {
                    disableBoard();
                    SwingUtilities.invokeLater(() -> {
                        readyButton.setEnabled(true);
                        readyButton.setText("Ready");
                        readyButton.setBackground(new Color(76, 175, 80)); // Green color
                    });
                } else if (msg.startsWith("MOVE")) {
                    String[] parts = msg.split(" ");
                    int row = Integer.parseInt(parts[1]);
                    int col = Integer.parseInt(parts[2]);
                    int playerId = Integer.parseInt(parts[3]);

                    String symbol = getSymbolForPlayer(playerId, totalPlayers);
                    Color color = getColorForPlayer(playerId, totalPlayers);
                    int index = row * 10 + col;
                    SwingUtilities.invokeLater(() -> {
                        buttons[index].setText(symbol);
                        buttons[index].setForeground(color);
                    });
                } else if (msg.startsWith("TURN")) {
                    currentTurn = Integer.parseInt(msg.split(" ")[1]);
                    currentName = msg.split(" ")[2];
                    if (currentTurn == myPlayerId) {
                        enableBoard();
                        startCountDown(timer);
                    } else {
                        enableBoard();
                        stopCountDown();
                        textField.setText(currentName + "'s turn");
                    }
                } else if (msg.startsWith("WIN")) {
                    String[] parts = msg.split(" ");
                    int winnerId = Integer.parseInt(parts[1]);
                    String winnerName = parts[2];
                    Color winnerColor = getColorForPlayer(winnerId, totalPlayers);

                    SwingUtilities.invokeLater(() -> {
                        if (winnerId == myPlayerId) {
                            textField.setText("You win!");
                        } else {
                            textField.setText(winnerName + " wins!");
                        }
                        stopCountDown();
                    });

                    for (int i = 3; i < parts.length; i += 2) {
                        int row = Integer.parseInt(parts[i]);
                        int col = Integer.parseInt(parts[i + 1]);
                        int index = row * 10 + col;

                        SwingUtilities.invokeLater(() -> {
                            buttons[index].setBackground(winnerColor);
                        });
                    }
                    disableBoard();

                    SwingUtilities.invokeLater(() -> {
                        if (!gameEnded) {
                            gameEnded = true;
                            int option = JOptionPane.showOptionDialog(
                                frame,
                                "Game over!\nDo you want to go back to the menu?",
                                "Game Over",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                new Object[]{"Back to Menu", "Exit"},
                                "Back to Menu"
                            );
                            if (option == JOptionPane.YES_OPTION) {
                                frame.dispose();
                                new SignedInMenu(myPlayerName);
                            } else if (option == JOptionPane.NO_OPTION) {
                                System.exit(0);
                            }
                        }
                    });


                } else if (msg.startsWith("DRAW")) {
                    SwingUtilities.invokeLater(() -> {
                        textField.setText("It's a draw!");
                        stopCountDown();
                    });
                    disableBoard();


                    SwingUtilities.invokeLater(() -> {
                        if (!gameEnded) {
                            gameEnded = true;
                            int option = JOptionPane.showOptionDialog(
                                frame,
                                "Game over!\nDo you want to go back to the menu?",
                                "Game Over",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                new Object[]{"Back to Menu", "Exit"},
                                "Back to Menu"
                            );
                            if (option == JOptionPane.YES_OPTION) {
                                frame.dispose();
                                new SignedInMenu(myPlayerName);
                            } else if (option == JOptionPane.NO_OPTION) {
                                System.exit(0);
                            }
                        }
                    });

                } else if (msg.startsWith("DISCONNECT")) {
                    String[] parts = msg.split(" ");
                    String disconnectedPlayer = parts[1];
                    SwingUtilities.invokeLater(() -> {
                        disableBoard();
                        stopCountDown();
                        textField.setText(disconnectedPlayer + " disconnected.");
                        // Show a message dialog
                        if (!gameEnded) {
                            gameEnded = true;
                            int option = JOptionPane.showOptionDialog(
                                frame,
                                "Connection lost.\nDo you want to go back to the menu?",
                                "Connection Lost",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.ERROR_MESSAGE,
                                null,
                                new Object[]{"Back to Menu", "Exit"},
                                "Back to Menu"
                            );
                            if (option == JOptionPane.YES_OPTION) {
                                frame.dispose();
                                new SignedInMenu(myPlayerName);
                            } else if (option == JOptionPane.NO_OPTION) {
                                System.exit(1);
                            }
                        }
                    });
                    disableBoard();
                }
            }
        } catch (IOException e) {
            // Handle the exception
            // Show a back to menu dialog
            SwingUtilities.invokeLater(() -> {
                int option = JOptionPane.showOptionDialog(
                    frame,
                    "Connection lost.\nDo you want to go back to the menu?",
                    "Connection Lost",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE,
                    null,
                    new Object[]{"Back to Menu", "Exit"},
                    "Back to Menu"
                );
                if (option == JOptionPane.YES_OPTION) {
                    frame.dispose();
                    new SignedInMenu(myPlayerName);
                } else if (option == JOptionPane.NO_OPTION) {
                    System.exit(1);
                }
            });
        }
    }

    private String getSymbolForPlayer(int playerId, int numOfPlayers) {
        if (numOfPlayers == 2) {
            return playerId == 1 ? "X" : "O";
        } else if (numOfPlayers == 3) {
            return playerId == 1 ? "X" : (playerId == 2 ? "Y" : "Z");
        } else if (numOfPlayers == 4) {
            return playerId == 1 ? "X" : (playerId == 2 ? "Y" : (playerId == 3 ? "A" : "B"));
        } else
            return "?";
    }

    private Color getColorForPlayer(int playerId, int numOfPlayers) {
        if (numOfPlayers == 2) {
            return playerId == 1 ? Color.RED : Color.BLUE;
        } else if (numOfPlayers == 3) {
            return playerId == 1 ? Color.GREEN : (playerId == 2 ? Color.BLUE : Color.RED);
        } else if (numOfPlayers == 4) {
            return playerId == 1 ? Color.BLUE
                    : (playerId == 2 ? Color.ORANGE : (playerId == 3 ? Color.GREEN : Color.RED));
        } else
            return Color.BLACK;
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

    private void startCountDown(int timer) {
        timeLeft = timer;
        textField.setText("Your turn (" + getSymbolForPlayer(myPlayerId, totalPlayers) + ") - " + timeLeft + "s left");

        clock = new Timer(1000, _ -> {
            timeLeft--;
            if (timeLeft > 0) {
                textField.setText(
                        "Your turn (" + getSymbolForPlayer(myPlayerId, totalPlayers) + ") - " + timeLeft + "s left");
            } else {
                clock.stop();
                textField.setText("Time's up!");
                out.println("TIMEOUT");
            }
        });
        clock.start();
    }

    private void stopCountDown() {
        if (clock != null && clock.isRunning()) {
            clock.stop();
        }
    }
}
