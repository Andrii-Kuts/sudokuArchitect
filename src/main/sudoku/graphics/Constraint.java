package main.sudoku.graphics;

import main.sudoku.Savable;
import main.sudoku.SudokuConstraint;
import main.sudoku.SudokuView;
import main.ui.MenuView;
import main.ui.RenderObject;
import main.ui.graphics.UIConstraint;

public interface Constraint extends Savable
{
    UIConstraint Convert(SudokuView view);
    void Convert(SudokuView view, RenderObject renderObject);
    String GetName();
    void Select();
    void Deselect();
    boolean IsSelected();
    void GenerateUI(MenuView view);
    SudokuConstraint GetConstraint();
    void GetConstraint(SudokuConstraint constraint);
}
