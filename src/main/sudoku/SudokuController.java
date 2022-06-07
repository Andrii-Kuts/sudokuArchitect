package main.sudoku;

import java.util.ArrayList;

public interface SudokuController
{
    void SetDigit(int x, int y, int digit);
    void SetPencil(int x, int y, int mask);
    void SetSmall(int x, int y, int mask);
    void SetColor(int x, int y, int mask);
    Board GetBoard();

    void UpdateCell(int x, int y, Cell cell);
    void UpdateConstraint(int i, boolean value);
    void UpdateConstraints(boolean[] values);
}
