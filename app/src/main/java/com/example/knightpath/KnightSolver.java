package com.example.knightpath;

import java.util.ArrayList;
import java.util.List;

class KnightSolver {
    public static int MAX_DEPTH = 3;

    // 8 possible movements for a knight.
    public static final int row[] = { 2, 1, -1, -2, -2, -1,  1,  2 };
    public static final int col[] = { 1, 2,  2,  1, -1, -2, -2, -1 };

    // Check if (i, j) is outside the chessboard.
    private static boolean isOutOfBounds(int size, int i, int j) {
        return (i < 0 || j < 0 || i >= size || j >= size);
    }

    private int size;
    private boolean explored[][];
    private List<List<int[]>> solutions;
    private List<int[]> currentPath;

    public KnightSolver(int size) {
        this.size = size;
    }

    public List<List<int[]>> solve(int startI, int startJ, int targetI, int targetJ) {
        this.explored = new boolean[size][size];
        solutions = new ArrayList<>();
        currentPath = new ArrayList<>();
        solveRecursive(startI, startJ, targetI, targetJ, 0);
        return solutions;
    }

    // Solve the problem using backtracking.
    private void solveRecursive(int i, int j, int targetI, int targetJ, int move) {
        if (move > MAX_DEPTH) return;   // If we are over MAX_DEPTH moves, go back.
        if (i == targetI && j == targetJ) {
            solutions.add(new ArrayList<>(currentPath));
            return;
        }

        explored[i][j] = true;
        currentPath.add(new int[]{i, j});
        for (int k = 0; k < 8; ++k) {
            int nextI = i + row[k];
            int nextJ = j + col[k];

            // if new position is a valid and not explored yet
            if (!isOutOfBounds(size, nextI, nextJ) && !explored[nextI][nextJ]) {
                solveRecursive(nextI, nextJ, targetI, targetJ,move + 1);
            }
        }

        // Backtrack
        currentPath.remove(currentPath.size() - 1);
        explored[i][j] = false;
    }
}
