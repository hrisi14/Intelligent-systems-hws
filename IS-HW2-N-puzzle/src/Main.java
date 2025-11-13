import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class Main {

    private static final int INF = Integer.MAX_VALUE;

    private static Map<Integer, Pair<Integer,Integer>> goalState = new HashMap<>();
    private static int matrixSize;

    static final class Pair<T,V> {
        final T first; final V second;
        Pair(T newFirst, V newSecond) {
            this.first = newFirst;
            this.second = newSecond;
        }
    }

    static final class Node {
        final int[][] matrix;
        final int g;
        final Pair<Integer,Integer> zeroStatePos;
        final List<String> moves;

        Node(int[][] newMatrix, int g, Pair<Integer,Integer> zeroStatePos, List<String> moves){
            this.matrix=newMatrix;
            this.g=g;
            this.zeroStatePos=zeroStatePos;
            this.moves=moves;
        }

        @Override public boolean equals(Object o){
            if(!(o instanceof Node)) {
                return false;
            }
            Node other=(Node)o;
            if (this.matrix.length != other.matrix.length ||
                    this.matrix[0].length != other.matrix[0].length) {
                return false;
            }
            if(!Objects.equals(zeroStatePos.first, other.zeroStatePos.first) ||
                    !Objects.equals(zeroStatePos.second, other.zeroStatePos.second))
                return false;
            return Arrays.deepEquals(matrix, other.matrix);
        }

        @Override public int hashCode() {
            return 31*Arrays.deepHashCode(matrix) + 7*zeroStatePos.first + zeroStatePos.second;
        }
    }

    private static void initGoalState(int N, int emptyIndex) {
        goalState.clear();
        if (emptyIndex == -1) {
            emptyIndex = N;
        }
        int emptyIndRow = emptyIndex / matrixSize;
        int emptyIndCol = emptyIndex % matrixSize;
        goalState.put(0, new Pair<>(emptyIndRow, emptyIndCol));
        int num = 1;
        for (int i=0; i<matrixSize; i++){
            for (int j=0; j<matrixSize; j++){
                if (i==emptyIndRow && j==emptyIndCol) {
                    continue;
                }
                goalState.put(num, new Pair<>(i,j));
                if (num==N) {
                    return;
                }
                num++;
            }
        }
    }

    private static int heuristic(Node node){
        int sum=0;
        for(int i=0; i<matrixSize; i++){
            for(int j=0; j<matrixSize; j++){
                int val = node.matrix[i][j];
                if(val==0){
                    continue;
                }
                Pair<Integer,Integer> desiredCoordinates = goalState.get(val);
                sum += Math.abs(desiredCoordinates.first - i) + Math.abs(desiredCoordinates.second - j);
            }
        }
        return sum;
    }

    private static boolean isGoal(Node node) {
        for(int i=0;i<matrixSize;i++){
            for(int j=0;j<matrixSize;j++){
                Pair<Integer,Integer> desiredCoordinates = goalState.get(node.matrix[i][j]);
                if(desiredCoordinates.first!=i || desiredCoordinates.second!=j) {
                    return false;
                }
            }
        }
        return true;
    }

    private static List<Node> successors(Node node){
        List<Node> res = new ArrayList<>(4);
        int x = node.zeroStatePos.first, y = node.zeroStatePos.second;

        if (y+1 < matrixSize) {
            res.add(make(node, x, y, x, y+1, "left"));
        }
        if (y-1 >= 0) {
            res.add(make(node, x, y, x, y-1, "right"));
        }
        if (x+1 < matrixSize) {
            res.add(make(node, x, y, x+1, y, "up"));
        }
        if (x-1 >= 0) {
            res.add(make(node, x, y, x-1, y, "down"));
        }
        return res;
    }

    private static Node make(Node node, int zx, int zy, int tx, int ty, String move){
        int[][] b = cloneBoard(node.matrix);
        b[zx][zy] = b[tx][ty];
        b[tx][ty] = 0;
        List<String> moves = new ArrayList<>(node.moves);
        moves.add(move);
        return new Node(b, node.g+1, new Pair<>(tx,ty), moves);
    }

    private static int[][] cloneBoard(int[][] a){
        int[][] b = new int[a.length][];
        for(int i=0;i<a.length;i++) {
            b[i]=a[i].clone();
        }
        return b;
    }

    public static List<String> idaStar(Node root){
        int bound = heuristic(root);
        Deque<Node> path = new ArrayDeque<>();
        path.addLast(root);

        while (true){
            int t = search(path, 0, bound);
            if (t == 0) {
                return path.peekLast().moves;
            }
            if (t == INF) {
                return null;
            }
            bound = t;
        }
    }

    private static int search(Deque<Node> path, int g, int bound){
        Node node = path.peekLast();
        int f = g + heuristic(node);
        if (f > bound) {
            return f;
        }
        if (isGoal(node)) {
            return 0;
        }
        int min = INF;
        for (Node succ : successors(node)){
            if (path.contains(succ)) {
                continue;
            }
            path.addLast(succ);
            int t = search(path, g+1, bound);
            if (t == 0) {
                return 0;
            }
            if (t < min) {
                min = t;
            }
            path.pollLast();
        }
        return min;
    }

    private static int countInversions(int[][] matrix, int size) {
        int index = 0;
        int [] flattenedMatrix = new int[size*size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                flattenedMatrix[index] = matrix[i][j];
                index++;
            }
        }

        int flattenedArrSize = size*size;
        int inversionsCount = 0;
        for (int i = 0; i < flattenedArrSize; i++) {
            for (int j = i; j < flattenedArrSize; j++) {
                if (flattenedMatrix[i] != 0 && flattenedMatrix[j] != 0 &&
                        flattenedMatrix[i] > flattenedMatrix[j]) {
                    inversionsCount++;
                }
            }
        }
        return inversionsCount;
    }

    private static boolean isSolvable(int[][] matrix, int size, int zeroElementRow) {
        if (size % 2 != 0) {
            return countInversions(matrix, size) % 2 == 0;
        } else {
            return (countInversions(matrix, size) + zeroElementRow) % 2 != 0;
        }
    }

    public static void main(String[] args) {
        boolean timeOnly = "1".equals(System.getenv("FMI_TIME_ONLY"));

        Scanner in = new Scanner(System.in);
        int N = in.nextInt();
        int emptyIndex = in.nextInt();

        matrixSize = (int)Math.sqrt(N + 1);
        int [][] matrix = new int[matrixSize][matrixSize];

        Pair<Integer,Integer> initialEmptySquare = new Pair<>(0, 0);

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                matrix[i][j] = in.nextInt();
                if (matrix[i][j] == 0) {
                    initialEmptySquare = new Pair<>(i, j);
                }
            }
        }

        if (emptyIndex == -1) {
            emptyIndex = N;
        }
        if (!isSolvable(matrix, matrixSize, emptyIndex/matrixSize)) {
            System.out.println(-1);
            return;
        }

        Node root = new Node(matrix, 0, initialEmptySquare, new ArrayList<>());
        long startTime = System.nanoTime();
        initGoalState(N, emptyIndex);
        List<String> answer = idaStar(root);
        long endTime = System.nanoTime();

        if (timeOnly) {
            double duration = (endTime - startTime)/1e6;
            System.out.println("# TIMES_MS: alg=" + duration);
            return;
        }

        if (answer == null){
            System.out.println(-1);
            return;
        }
        System.out.println(answer.size());
        for (String m : answer) {
            System.out.println(m);
        }
    }
}
