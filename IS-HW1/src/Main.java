import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


class Move {
    char [] board;
    int zeroStateIndex;
    public Move(char[] board, int zeroStateIndex) {
        this.board = board.clone();
        this.zeroStateIndex = zeroStateIndex;
    }
}
public class Main {
    private static final char LEFT_FROG = '>';
    private static final char RIGHT_FROG = '<';
    private static final char GAP = '_';
    private static final double MILLI_SECONDS = 1e6;


    private static char[] goalStates;
    private static boolean isGoalState(char[] board) {
        return Arrays.equals(goalStates, board);
    }
    private static List<Move> moves(char [] board, int zeroStateIndex) {
        List<Move> moves = new ArrayList<>(4);
        int len = board.length;

        boolean even = (zeroStateIndex % 2 == 0);

        boolean addedSlideRight = false;
        boolean addedSlideLeft  = false;

        if (zeroStateIndex - 2 >=0 && board[zeroStateIndex - 1] == LEFT_FROG && board[zeroStateIndex - 2] == LEFT_FROG) {
            Move m = new Move(board, zeroStateIndex);
            m.board[zeroStateIndex] = LEFT_FROG;
            m.board[zeroStateIndex - 1] = GAP;
            m.zeroStateIndex = zeroStateIndex - 1;
            moves.add(m);
            addedSlideRight = true;
        }
        if (zeroStateIndex + 2 < len && board[zeroStateIndex + 1] == RIGHT_FROG && board[zeroStateIndex + 2] == RIGHT_FROG) {
            Move m = new Move(board, zeroStateIndex);
            m.board[zeroStateIndex] = RIGHT_FROG;
            m.board[zeroStateIndex + 1] = GAP;
            m.zeroStateIndex = zeroStateIndex + 1;
            moves.add(m);
            addedSlideLeft = true;
        }

        if (even) {
            if (!addedSlideRight && zeroStateIndex - 1 >= 0 && board[zeroStateIndex - 1] == LEFT_FROG) {
                Move m = new Move(board, zeroStateIndex);
                m.board[zeroStateIndex] = LEFT_FROG;
                m.board[zeroStateIndex - 1] = GAP;
                m.zeroStateIndex = zeroStateIndex - 1;
                moves.add(m);
            }
            if (!addedSlideLeft && zeroStateIndex + 1 < len && board[zeroStateIndex + 1] == RIGHT_FROG) {
                Move m = new Move(board, zeroStateIndex);
                m.board[zeroStateIndex] = RIGHT_FROG;
                m.board[zeroStateIndex + 1] = GAP;
                m.zeroStateIndex = zeroStateIndex + 1;
                moves.add(m);
            }
        } else {
            if (!addedSlideLeft && zeroStateIndex + 1 < len && board[zeroStateIndex + 1] == RIGHT_FROG) {
                Move m = new Move(board, zeroStateIndex);
                m.board[zeroStateIndex] = RIGHT_FROG;
                m.board[zeroStateIndex + 1] = GAP;
                m.zeroStateIndex = zeroStateIndex + 1;
                moves.add(m);
            }
            if (!addedSlideRight && zeroStateIndex - 1 >= 0 && board[zeroStateIndex - 1] == LEFT_FROG) {
                Move m = new Move(board, zeroStateIndex);
                m.board[zeroStateIndex] = LEFT_FROG;
                m.board[zeroStateIndex - 1] = GAP;
                m.zeroStateIndex = zeroStateIndex - 1;
                moves.add(m);
            }
        }

        if (zeroStateIndex - 2 >= 0 && board[zeroStateIndex - 2] == LEFT_FROG) {
            Move m = new Move(board, zeroStateIndex);
            m.board[zeroStateIndex] = LEFT_FROG;
            m.board[zeroStateIndex - 2] = GAP;
            m.zeroStateIndex = zeroStateIndex - 2;
            moves.add(m);
        }
        if (zeroStateIndex + 2 < len && board[zeroStateIndex + 2] == RIGHT_FROG) {
            Move m = new Move(board, zeroStateIndex);
            m.board[zeroStateIndex] = RIGHT_FROG;
            m.board[zeroStateIndex + 2] = GAP;
            m.zeroStateIndex = zeroStateIndex + 2;
            moves.add(m);
        }
        return moves;
    }

    public static boolean dfs (char [] board, int zeroStateIndex, List<char[]> list, int remaining) {
        if (isGoalState(board)) {
            return true;
        }
        if (remaining == 0) {
            return false;
        }
        List<Move> moves = moves(board, zeroStateIndex);
        for (Move move: moves) {
            if (dfs(move.board, move.zeroStateIndex, list, remaining - 1)) {
                list.add(move.board);
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        boolean timeOnly = "1".equals(System.getenv("FMI_TIME_ONLY"));
        Scanner input = new Scanner(System.in);
        int n = input.nextInt();
        long start = System.nanoTime();

        int lilyPadsCount = 2*n + 1;
        char [] board = new char[lilyPadsCount];
        goalStates = new char[lilyPadsCount];
        for (int i = 0; i < n; i++) {
            board[i] = LEFT_FROG;
            goalStates[i] = RIGHT_FROG;
        }
        board[n] = GAP;
        goalStates[n] = GAP;
        for (int i = n+1; i < lilyPadsCount; i++) {
            board[i] = RIGHT_FROG;
            goalStates[i] = LEFT_FROG;
        }

        List<char[]> result = new ArrayList<>();
        int maxDepth = n * n + 2 * n;
        dfs(board, n, result, maxDepth);
        result.add(board);
        long end = System.nanoTime();
        double elapsed = (end - start) / MILLI_SECONDS;

        if (timeOnly) {
            System.out.printf("%.3f ms%n", elapsed);
        } else {
            int size = result.size() - 1;
            for (int i = size; i >= 0 ; i--) {
                System.out.println(String.valueOf(result.get(i)));
            }
        }
    }
}


