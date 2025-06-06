import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TicTacToeServer {

    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 4;
    private static final int BOARD_SIZE = 10;

    private ServerSocket serverSocket;
    private final List<ClientHandler> players = new ArrayList<>();
    private final int[][] board = new int[BOARD_SIZE][BOARD_SIZE];
    private int currentPlayer = 1;
    private String[] playerNames = new String[4];

    private final AtomicInteger readycount = new AtomicInteger(0);

    private int WIN_COUNT;
    private int timer;

    public TicTacToeServer(int PORT, int WIN_COUNT, int timer) {
        this.WIN_COUNT = WIN_COUNT;
        this.timer = timer;

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started. Waiting for players...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            while (players.size() < MAX_PLAYERS) {
                Socket socket = serverSocket.accept();
                ClientHandler player = new ClientHandler(socket, players.size() + 1);
                players.add(player);
                new Thread(player).start();
                System.out.println("Player " + player.playerId + " connected.");
            }
        } catch (Exception e) {
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
            players.get(playerId - 1).sendMessage("ERROR Not your turn");
            return;
        }
        if (board[row][col] != 0) { // Check if the cell is already occupied
            players.get(playerId - 1).sendMessage("ERROR Cell already occupied");
            return;
        }

        board[row][col] = playerId; // Store the player ID in the board
        broadcast("MOVE " + row + " " + col + " " + playerId); // Broadcast the move

        List<int[]> winSequence = getWinningSequence(row, col, playerId);
        if (winSequence != null) {
            StringBuilder winMsg = new StringBuilder("WIN " + playerId + " " + playerNames[playerId - 1]);
            for (int[] cell : winSequence) {
                winMsg.append(" ").append(cell[0]).append(" ").append(cell[1]);
            }
            System.out.println(winMsg.toString());
            broadcast(winMsg.toString());
            // --- Ranking update logic ---
            DatabaseHelper dbHelper = new DatabaseHelper();
            // Winner gets +10
            dbHelper.updateRanking(playerNames[playerId - 1], 10);
            // Others get -8
            for (int i = 0; i < players.size(); i++) {
                if (i != (playerId - 1)) {
                    dbHelper.updateRanking(playerNames[i], -8);
                }
            }
        } else if (isFull(board)) {
            System.out.println("DRAW");
            broadcast("DRAW");
            // --- Ranking update logic for draw ---
            DatabaseHelper dbHelper = new DatabaseHelper();
            for (int i = 0; i < players.size(); i++) {
                dbHelper.updateRanking(playerNames[i], 5);
            }
        } else {
            currentPlayer = currentPlayer % players.size() + 1;
            broadcast("TURN " + currentPlayer + " " + playerNames[currentPlayer - 1]);
        }
    }

    private synchronized void handleTimeout(int playerId) {
        if (playerId == currentPlayer) {
            System.out.println("Player " + playerId + " timed out. Switching turn.");
            currentPlayer = currentPlayer % players.size() + 1;
            broadcast("TURN " + currentPlayer + " " + playerNames[currentPlayer - 1]);
        } else {
            players.get(playerId - 1).sendMessage("ERROR Not your turn to timeout");
        }
    }

    private List<int[]> getWinningSequence(int row, int col, int playerId) {
        int[][] directions = { { 1, 0 }, { 0, 1 }, { 1, 1 }, { 1, -1 } };
        for (int[] d : directions) {
            List<int[]> sequence = new ArrayList<>();
            sequence.add(new int[] { row, col });

            int dr = d[0], dc = d[1];
            for (int i = 1; i < WIN_COUNT; i++) {
                int r = row + i * dr, c = col + i * dc;
                if (r < 0 || r >= BOARD_SIZE || c < 0 || c >= BOARD_SIZE)
                    break;
                if (board[r][c] != playerId)
                    break;
                sequence.add(new int[] { r, c });
            }
            for (int i = 1; i < WIN_COUNT; i++) {
                int r = row - i * dr, c = col - i * dc;
                if (r < 0 || r >= BOARD_SIZE || c < 0 || c >= BOARD_SIZE)
                    break;
                if (board[r][c] != playerId)
                    break;
                sequence.add(new int[] { r, c });
            }

            if (sequence.size() >= WIN_COUNT)
                return sequence;
        }
        return null;
    }

    private boolean isFull(int[][] board) {
        for (int[] row : board) {
            for (int cell : row) {
                if (cell == 0)
                    return false;
            }
        }
        return true;
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
                sendMessage("WAITING_FOR_READY");

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("READY")) {
                        String playerName = message.split(" ")[1];
                        playerNames[playerId - 1] = playerName;
                        int count = readycount.incrementAndGet();
                        System.out.println("Player " + playerId + " is ready (" + count + "/" + players.size() + ")");

                        if (count >= MIN_PLAYERS && count == players.size()) {
                            broadcast("START " + players.size() + " " + timer);
                            broadcast("TURN " + currentPlayer + " " + playerNames[currentPlayer - 1]);
                        }
                    } else if (message.startsWith("MOVE")) {
                        String[] parts = message.split(" ");
                        int row = Integer.parseInt(parts[1]);
                        int col = Integer.parseInt(parts[2]);
                        handleMove(row, col, playerId);
                    } else if (message.startsWith("TIMEOUT")) {
                        handleTimeout(playerId);
                    }
                }
            } catch (IOException e) {
                System.out.println("Player " + playerId + " disconnected.");
                broadcast("DISCONNECT " + playerNames[playerId - 1]);
            }
        }

        public void sendMessage(String msg) {
            out.println(msg);
        }
    }
}
