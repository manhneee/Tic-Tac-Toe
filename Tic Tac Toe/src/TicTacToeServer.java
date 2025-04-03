import java.io.*;
import java.net.*;
import java.util.*;

public class TicTacToeServer {

    private static final int PORT = 12345;
    private static final int MAX_PLAYERS = 4;
    private static final int BOARD_SIZE = 10;

    private ServerSocket serverSocket;
    private final List<ClientHandler> players = new ArrayList<>();
    private final String[][] board = new String[BOARD_SIZE][BOARD_SIZE];
    private int currentPlayer = 1;

    public static void main(String[] args) {
        new TicTacToeServer().start();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started. Waiting for players...");

            while (players.size() < MAX_PLAYERS) {
                Socket socket = serverSocket.accept();
                ClientHandler player = new ClientHandler(socket, players.size() + 1);
                players.add(player);
                new Thread(player).start();
                System.out.println("Player " + player.playerId + " connected.");
            }
            broadcast("START");
            broadcast("TURN " + currentPlayer);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void broadcast(String message) {
        for (ClientHandler player : players) {
            player.sendMessage(message);
        }
    }

    private synchronized void handleMove(int row, int col, int playerId) {
        System.out.println("Received move from Player " + playerId + " at (" + row + "," + col + ")");

        if (playerId != currentPlayer) {
            System.out.println("It's not Player " + playerId + "'s turn. It's Player " + currentPlayer + "'s turn.");
            return;
        }
        if (board[row][col] != null) {
            System.out.println("Cell already occupied at (" + row + "," + col + ")");
            return;
        }

        String symbol = switch (playerId) {
            case 1 -> "X";
            case 2 -> "Y";
            case 3 -> "A";
            case 4 -> "B";
            default -> "?";
        };

        board[row][col] = symbol;
        broadcast("MOVE " + row + " " + col + " " + symbol);

        if (checkWin(row, col, symbol)) {
            broadcast("WIN " + playerId);
        } else {
            currentPlayer = currentPlayer % MAX_PLAYERS + 1;
            broadcast("TURN " + currentPlayer);
        }
    }

    private boolean checkWin(int row, int col, String symbol) {
        return checkDirection(row, col, symbol, 1, 0) ||
               checkDirection(row, col, symbol, 0, 1) ||
               checkDirection(row, col, symbol, 1, 1) ||
               checkDirection(row, col, symbol, 1, -1);
    }

    private boolean checkDirection(int row, int col, String symbol, int dRow, int dCol) {
        int count = 1;
        count += countConsecutive(row, col, symbol, dRow, dCol);
        count += countConsecutive(row, col, symbol, -dRow, -dCol);
        return count >= 4;
    }

    private int countConsecutive(int row, int col, String symbol, int dRow, int dCol) {
        int count = 0;
        for (int i = 1; i < 4; i++) {
            int r = row + i * dRow;
            int c = col + i * dCol;
            if (r < 0 || r >= BOARD_SIZE || c < 0 || c >= BOARD_SIZE) break;
            if (!symbol.equals(board[r][c])) break;
            count++;
        }
        return count;
    }

    class ClientHandler implements Runnable {
        private final Socket socket;
        private final int playerId;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket, int playerId) {
            this.socket = socket;
            this.playerId = playerId;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                sendMessage("WELCOME " + playerId);

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("MOVE")) {
                        String[] parts = message.split(" ");
                        int row = Integer.parseInt(parts[1]);
                        int col = Integer.parseInt(parts[2]);
                        handleMove(row, col, playerId);
                    }
                }
            } catch (IOException e) {
                System.out.println("Player " + playerId + " disconnected.");
            }
        }

        public void sendMessage(String msg) {
            out.println(msg);
        }
    }
}
