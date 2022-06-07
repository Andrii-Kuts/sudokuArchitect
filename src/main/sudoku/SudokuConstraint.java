package main.sudoku;

import main.DecodeError;
import main.ui.RenderObject;
import main.ui.graphics.UIConstraint;

import java.nio.ByteBuffer;

public interface SudokuConstraint extends Savable{
    void SetBoard(Board board);
    boolean CheckBoard();
}
