package main.sudoku.graphics;

import main.sudoku.Savable;
import main.sudoku.SudokuView;
import main.ui.MenuGroup;
import main.ui.MenuView;
import main.ui.RenderObject;

public interface Graphic extends Savable
{
    RenderObject Convert(SudokuView view);
    void Convert(SudokuView view, RenderObject renderObject);
    String GetName();
    void Select();
    void Deselect();
    boolean IsSelected();
    void GenerateUI(MenuView view);
}
