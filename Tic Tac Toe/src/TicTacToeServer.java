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

        if (playerId != currentPlayer) return;
        if (board[row][col] != null) return;

        String symbol = switch (playerId) {
            case 1 -> "X";
            case 2 -> "Y";
            case 3 -> "A";
            case 4 -> "B";
            default -> "?";
        };

        board[row][col] = symbol;
        broadcast("MOVE " + row + " " + col + " " + symbol);

        List<int[]> winSequence = getWinningSequence(row, col, symbol);
        if (winSequence != null) {
            StringBuilder winMsg = new StringBuilder("WIN " + playerId);
            for (int[] cell : winSequence) {
                winMsg.append(" ").append(cell[0]).append(" ").append(cell[1]);

            }
            System.out.println(winMsg.toString());
            broadcast(winMsg.toString());
        } else {
            currentPlayer = currentPlayer % MAX_PLAYERS + 1;
            broadcast("TURN " + currentPlayer);
        }
    }

    private List<int[]> getWinningSequence(int row, int col, String symbol) {
        int[][] directions = { {1, 0}, {0, 1}, {1, 1}, {1, -1} };
        for (int[] d : directions) {
            List<int[]> sequence = new ArrayList<>();
            sequence.add(new int[] {row, col});

            int dr = d[0], dc = d[1];
            for (int i = 1; i < 4; i++) {
                int r = row + i * dr, c = col + i * dc;
                if (r < 0 || r >= BOARD_SIZE || c < 0 || c >= BOARD_SIZE) break;
                if (!symbol.equals(board[r][c])) break;
                sequence.add(new int[] {r, c});
            }
            for (int i = 1; i < 4; i++) {
                int r = row - i * dr, c = col - i * dc;
                if (r < 0 || r >= BOARD_SIZE || c < 0 || c >= BOARD_SIZE) break;
                if (!symbol.equals(board[r][c])) break;
                sequence.add(new int[] {r, c});
            }

            if (sequence.size() >= 4) return sequence;
        }
        return null;
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
                broadcast("DISCONNECT " + playerId);
            }
        }

        public void sendMessage(String msg) {
            out.println(msg);
        }
    }
}
