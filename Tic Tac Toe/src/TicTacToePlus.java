import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.*;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TicTacToePlus implements ActionListener {

    Random random = new Random();
    JFrame frame = new JFrame();
    JPanel titlePanel = new JPanel();
    JPanel buttonPanel = new JPanel();
    JLabel textField = new JLabel();
    JButton[] buttons = new JButton[100];
    int playerTurn = 1;

    TicTacToePlus() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.getContentPane().setBackground(new Color(50, 50, 50));
        frame.setLayout(new BorderLayout());
        frame.setVisible(true);
        frame.setTitle("Tic Tac Toe");

        textField.setBackground(new Color(25, 25, 25));
        textField.setForeground(new Color(25, 255, 0));
        textField.setFont(new Font("Ink Free", Font.BOLD, 70));
        textField.setHorizontalAlignment(JLabel.CENTER);
        textField.setText("Tic-Tac-Toe");
        textField.setOpaque(true);

        titlePanel.setLayout(new BorderLayout());
        titlePanel.setBounds(0, 0, 800, 100);
        titlePanel.add(textField);

        buttonPanel.setLayout(new GridLayout(10, 10));
        buttonPanel.setBackground(new Color(150, 150, 150));

        for (int i = 0; i < 100; i++) {
            buttons[i] = new JButton();
            buttonPanel.add(buttons[i]);
            buttons[i].setFont(new Font("MV Boli", Font.BOLD, 50));
            buttons[i].setFocusable(false);
            buttons[i].setActionCommand(String.valueOf(i)); // Set index as action command
            buttons[i].addActionListener(this);
        }

        frame.add(titlePanel, BorderLayout.NORTH);
        frame.add(buttonPanel);

        textField.setText("Player " + playerTurn + "'s turn");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int i = Integer.parseInt(e.getActionCommand()); // Get the button index
        if (buttons[i].getText().isEmpty()) {
            switch (playerTurn) {
                case 1:
                    buttons[i].setForeground(new Color(0, 0, 255));
                    buttons[i].setText("X");
                    playerTurn++;
                    break;
                case 2:
                    buttons[i].setForeground(new Color(255, 255, 0));
                    buttons[i].setText("Y");
                    playerTurn++;
                    break;
                case 3:
                    buttons[i].setForeground(new Color(0, 255, 0));
                    buttons[i].setText("A");
                    playerTurn++;
                    break;
                case 4:
                    buttons[i].setForeground(new Color(255, 0, 0));
                    buttons[i].setText("B");
                    playerTurn = 1;
                    break;
            }
            textField.setText("Player " + playerTurn + "'s turn");
            check(i);
        }
    }

    public void check(int i) {
        int row = i / 10;
        int col = i % 10;
        String symbol = buttons[i].getText();

        // Check all 4 possible directions
        checkLine(row, col, 0, 1, symbol); // Horizontal
        checkLine(row, col, 1, 0, symbol); // Vertical
        checkLine(row, col, 1, 1, symbol); // Diagonal down-right
        checkLine(row, col, 1, -1, symbol); // Diagonal down-left
    }

    private void checkLine(int row, int col, int rowDelta, int colDelta, String symbol) {
        int count = 1; // Start with current symbol
        JButton[] sequence = new JButton[4];
        sequence[0] = buttons[row * 10 + col];

        // Check in positive direction
        for (int step = 1; step < 4; step++) {
            int newRow = row + step * rowDelta;
            int newCol = col + step * colDelta;

            if (newRow < 0 || newRow >= 10 || newCol < 0 || newCol >= 10) {
                break;
            }

            if (!buttons[newRow * 10 + newCol].getText().equals(symbol)) {
                break;
            }

            sequence[count] = buttons[newRow * 10 + newCol];
            count++;
        }

        // Check in negative direction
        for (int step = 1; step < 4; step++) {
            int newRow = row - step * rowDelta;
            int newCol = col - step * colDelta;

            if (newRow < 0 || newRow >= 10 || newCol < 0 || newCol >= 10) {
                break;
            }

            if (!buttons[newRow * 10 + newCol].getText().equals(symbol)) {
                break;
            }

            // Shift array to make room for this new element at the beginning
            System.arraycopy(sequence, 0, sequence, 1, sequence.length - 1);
            sequence[0] = buttons[newRow * 10 + newCol];
            count++;
        }

        if (count >= 4) {
            // We need exactly 4 for the win condition
            JButton[] winningSequence = new JButton[4];
            // Find the first 4 consecutive in the sequence
            for (int j = 0; j <= count - 4; j++) {
                boolean consecutive = true;
                for (int k = 0; k < 4; k++) {
                    if (sequence[j + k] == null) {
                        consecutive = false;
                        break;
                    }
                }
                if (consecutive) {
                    System.arraycopy(sequence, j, winningSequence, 0, 4);
                    winConditionSatisfied(winningSequence);
                    return;
                }
            }
        }
    }

    public void winConditionSatisfied(JButton[] winningButtons) {
        String symbol = winningButtons[0].getText();
        int winningPlayer = 0;

        switch (symbol) {
            case "X":
                winningPlayer = 1;
                break;
            case "Y":
                winningPlayer = 2;
                break;
            case "A":
                winningPlayer = 3;
                break;
            case "B":
                winningPlayer = 4;
                break;
        }

        for (JButton button : winningButtons) {
            switch (winningPlayer) {
                case 1:
                    button.setBackground(new Color(0, 0, 255));
                    break;
                case 2:
                    button.setBackground(new Color(255, 255, 0));
                    break;
                case 3:
                    button.setBackground(new Color(0, 255, 0));
                    break;
                case 4:
                    button.setBackground(new Color(255, 0, 0));
                    break;
                default:
                    break;
            }
        }

        textField.setText("Player " + winningPlayer + " wins!");

        for (JButton button : buttons) {
            button.setEnabled(false);
        }
    }
}
