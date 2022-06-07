package main.sudoku;

import main.ui.Style;

import java.awt.*;
import java.awt.geom.Point2D;

public interface SudokuView
{
    Point2D.Double GetCellPos(int x, int y);
    Point2D.Double GetNodePos(int x, int y);
    Point GetSize();
    Style GetStyle();
}
