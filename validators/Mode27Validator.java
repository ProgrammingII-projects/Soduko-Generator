package validators;

import model.SudokuBoard;
import model.ValidationResult;

import java.util.*;
import java.util.concurrent.*;

public class Mode27Validator extends SudokuValidator {

    public Mode27Validator(SudokuBoard board) {
        super(board);
    }

    @Override
    public ValidationResult validate() {
        int[][] g = boardCopy();
        ExecutorService exec = Executors.newFixedThreadPool(27);
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int r = 0; r < 9; r++) {
            final int row = r;
            tasks.add(() -> {
                Map<Integer, List<Integer>> positions = new LinkedHashMap<>();
                for (int c = 0; c < 9; c++) {
                    int v = g[row][c];
                    positions.computeIfAbsent(v, k -> new ArrayList<>()).add(c + 1);
                }
                for (Map.Entry<Integer, List<Integer>> e : positions.entrySet()) {
                    if (e.getValue().size() > 1) result.addRowDuplicate(row + 1, e.getKey(), e.getValue());
                }
                return null;
            });
        }

        for (int c = 0; c < 9; c++) {
            final int col = c;
            tasks.add(() -> {
                Map<Integer, List<Integer>> positions = new LinkedHashMap<>();
                for (int r = 0; r < 9; r++) {
                    int v = g[r][col];
                    positions.computeIfAbsent(v, k -> new ArrayList<>()).add(r + 1);
                }
                for (Map.Entry<Integer, List<Integer>> e : positions.entrySet()) {
                    if (e.getValue().size() > 1) result.addColDuplicate(col + 1, e.getKey(), e.getValue());
                }
                return null;
            });
        }

        for (int br = 0; br < 3; br++) {
            for (int bc = 0; bc < 3; bc++) {
                final int boxIndex = br * 3 + bc + 1;
                final int baseRow = br * 3;
                final int baseCol = bc * 3;
                tasks.add(() -> {
                    Map<Integer, List<Integer>> positions = new LinkedHashMap<>();
                    int pos = 1;
                    for (int r = baseRow; r < baseRow + 3; r++) {
                        for (int c = baseCol; c < baseCol + 3; c++) {
                            int v = g[r][c];
                            positions.computeIfAbsent(v, k -> new ArrayList<>()).add(pos);
                            pos++;
                        }
                    }
                    for (Map.Entry<Integer, List<Integer>> e : positions.entrySet()) {
                        if (e.getValue().size() > 1) result.addBoxDuplicate(boxIndex, e.getKey(), e.getValue());
                    }
                    return null;
                });
            }
        }

        try {
            List<Future<Void>> futures = exec.invokeAll(tasks);
            for (Future<Void> f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } finally {
            exec.shutdown();
        }

        return result;
    }
}
