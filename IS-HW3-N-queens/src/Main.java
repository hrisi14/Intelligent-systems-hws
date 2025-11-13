import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Main {
    private static final boolean PRINT_BOARD = false;
    private static final Random RNG = new Random();
    private static int [] queens;
    private static int [] queensPerRow;
    private static int [] queensPerD1;
    private static int [] queensPerD2;


    private static void initQueensPositions(int N) {
        queens = new int[N];
        queensPerRow = new int[N];
        queensPerD1 = new int [2*N-1];
        queensPerD2 = new int [2*N-1];
        int[] candRows = new int [N];
        int shift = N - 1;

        for (int col = 0; col < N; col++) {
            int best = Integer.MAX_VALUE;
            for (int row = 0; row < N; row++) {
                int conflicts = conflictsAt(row, col, N);
                if (conflicts < best) {
                    best = conflicts;
                }
            }
            int len = 0;
            for (int row = 0; row < N; row++) {
                int conflicts = conflictsAt(row, col, N);
                if (conflicts == best) {
                    candRows[len++] = row;
                }
            }
            int pickRow = candRows[RNG.nextInt(len)];
            queens[col] = pickRow;
            queensPerRow[pickRow]++;
            queensPerD1[pickRow - col + shift]++;
            queensPerD2[pickRow + col]++;
        }
    }

    private static boolean isSolved(int N) {
        for (int col = 0; col < N; col++) {
            if (getConflictsOfQueen(queens[col], col, N) != 0) {
                return false;
            }
        }
        return true;
    }

    private static int conflictsAt(int row, int col, int N) {
        return queensPerRow[row]
                + queensPerD1[(row - col) + (N - 1)]
                + queensPerD2[row + col];
    }

    private static int getConflictsOfQueen(int row, int col, int N) {
        int c = 0;
        c += queensPerRow[row] - 1;
        c += queensPerD1[(row - col) + (N - 1)] - 1;
        c += queensPerD2[row + col] - 1;
        return c;
    }

    private static int getColOfQueenWithMaxConf(int N) {
        int maxConflicts = -1;
        for (int col = 0; col < N; col++) {
            int currentConflicts = getConflictsOfQueen(queens[col], col, N);
            if (currentConflicts > maxConflicts) {
                maxConflicts = currentConflicts;
            }
        }

        int[] candCols = new int [N];
        int len = 0;
        for (int col = 0; col < N; col++) {
            int c = getConflictsOfQueen(queens[col], col, N);
            if (c == maxConflicts) {
                candCols[len++] = col;
            }
        }
        return candCols[RNG.nextInt(len)];
    }

    private static int getRowOfQueenWithMinConf(int columnIndex, int N) {
        int minConflicts = Integer.MAX_VALUE;

        for (int rowIndex = 0; rowIndex < N; rowIndex++) {
            int currentConflicts = conflictsAt(rowIndex, columnIndex, N);
            if (currentConflicts < minConflicts) {
                minConflicts = currentConflicts;
            }
        }
        int[] candRows = new int [N];
        int len = 0;
        for (int row = 0; row < N; row++) {
            int c = conflictsAt(row, columnIndex, N);
            if (c == minConflicts) candRows[len++] = row;
        }
        return candRows[RNG.nextInt(len)];
    }

    private static void updateQueensPosition(int col, int newRow, int N) {
        int oldRow = queens[col];
        if (oldRow == newRow) {
            return;
        }
        queensPerRow[oldRow]--;
        queensPerD1[(oldRow - col) + (N - 1)]--;
        queensPerD2[oldRow + col]--;

        queens[col] = newRow;
        queensPerRow[newRow]++;
        queensPerD1[(newRow - col) + (N - 1)]++;
        queensPerD2[newRow + col]++;
    }

    public static int[] solve(int N, int k) {
       while (true) {
           initQueensPositions(N);
           //System.out.println("Init queens:" + Arrays.toString(queens));
           int iter = 0;
           while (iter++ <= k * N) {
               int col = getColOfQueenWithMaxConf(N);
               //System.out.println("Best col:" + col);
               if (getConflictsOfQueen(queens[col], col, N) == 0) {
                   return queens;
               }
               int row = getRowOfQueenWithMinConf(col, N);
               //System.out.println("Best row:" + row);
               updateQueensPosition(col, row, N);
           }
           if (isSolved(N)) {
              return queens;
           }
       }
    }

    public static void printBoard(int N) {
        for(int row = 0; row < N; row++)     {
            for (int col = 0; col < N; col++) {
                if(queens[col] == row) {
                   System.out.print('*');
                } else {
                    System.out.print('_');
                }
            }
            System.out.print('\n');
        }
    }

    public static void main(String[] args) {
        boolean timeOnly = "1".equals(System.getenv("FMI_TIME_ONLY"));
        Scanner sc = new Scanner(System.in);
        int N = sc.nextInt();
        int k;

        if (N == 2 || N ==3) {
            System.out.println(-1);
            return;
        }

        if (N >= 8 && N<=1000) {
            k = 10;
        } else if (N>1000 && N<=10000) {
            k = 5;
        } else if (N > 10000) {
            k = (int) (2*Math.log(N));
        } else {
            k = 2;
        }

        double startTime = System.nanoTime();
        queens = solve(N, k);
        double endTime = System.nanoTime();

        if (timeOnly) {
            double duration = (endTime - startTime)/1e6;
            System.out.println("# TIMES_MS: alg=" + duration);
        }
        System.out.println(Arrays.toString(queens));

        if (PRINT_BOARD) {
            printBoard(N);
        }
    }
}