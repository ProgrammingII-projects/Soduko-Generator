package validators;

import model.SudokuBoard;
import model.ValidationResult;

public abstract class SudokuValidator {
    protected final SudokuBoard board;
    protected final ValidationResult result = new ValidationResult();

    protected SudokuValidator(SudokuBoard board) {
        this.board = board;
    }

    // run full validation (must process whole board even if invalidities discovered)
    public abstract ValidationResult validate();

    protected int[][] boardCopy() {
        return board.getCopy();
    }
}
