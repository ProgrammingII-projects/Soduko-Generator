package validators;

import model.SudokuBoard;
import model.ValidationResult;

import java.util.concurrent.*;

public class Mode3Validator extends SudokuValidator {

    public Mode3Validator(SudokuBoard board) {
        super(board);
    }

    @Override
    public ValidationResult validate() {
        int[][] g = boardCopy();

        ExecutorService exec = Executors.newFixedThreadPool(3);
        Future<?> rowsFuture = exec.submit(() -> validateRows(g));
        Future<?> colsFuture = exec.submit(() -> validateCols(g));
        Future<?> boxesFuture = exec.submit(() -> validateBoxes(g));

        try {
            // wait for all, but we must let all tasks process full board
            rowsFuture.get();
            colsFuture.get();
            boxesFuture.get();
        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        } finally {
            exec.shutdown();
        }

        return result;
    }

    private void validateRows(int[][] g) {
        for (int r = 0; r < 9; r++) {
            java.util.Map<Integer, java.util.List<Integer>> positions = new java.util.LinkedHashMap<>();
            for (int c = 0; c < 9; c++) {
                int v = g[r][c];
                positions.computeIfAbsent(v, k -> new java.util.ArrayList<>()).add(c + 1);
            }
            for (java.util.Map.Entry<Integer, java.util.List<Integer>> e : positions.entrySet()) {
                if (e.getValue().size() > 1) result.addRowDuplicate(r + 1, e.getKey(), e.getValue());
            }
        }
    }

    private void validateCols(int[][] g) {
        for (int c = 0; c < 9; c++) {
            java.util.Map<Integer, java.util.List<Integer>> positions = new java.util.LinkedHashMap<>();
            for (int r = 0; r < 9; r++) {
                int v = g[r][c];
                positions.computeIfAbsent(v, k -> new java.util.ArrayList<>()).add(r + 1);
            }
            for (java.util.Map.Entry<Integer, java.util.List<Integer>> e : positions.entrySet()) {
                if (e.getValue().size() > 1) result.addColDuplicate(c + 1, e.getKey(), e.getValue());
            }
        }
    }

    private void validateBoxes(int[][] g) {
        for (int br = 0; br < 3; br++) {
            for (int bc = 0; bc < 3; bc++) {
                int boxIndex = br * 3 + bc + 1;
                java.util.Map<Integer, java.util.List<Integer>> positions = new java.util.LinkedHashMap<>();
                int baseRow = br * 3;
                int baseCol = bc * 3;
                int pos = 1;
                for (int r = baseRow; r < baseRow + 3; r++) {
                    for (int c = baseCol; c < baseCol + 3; c++) {
                        int v = g[r][c];
                        positions.computeIfAbsent(v, k -> new java.util.ArrayList<>()).add(pos);
                        pos++;
                    }
                }
                for (java.util.Map.Entry<Integer, java.util.List<Integer>> e : positions.entrySet()) {
                    if (e.getValue().size() > 1) result.addBoxDuplicate(boxIndex, e.getKey(), e.getValue());
                }
            }
        }
    }
}
