package main.sudoku;

import main.ParameterHolder;
import main.ui.MenuGroup;
import main.ui.MenuView;
import main.ui.Style;

public class SudokuSettings
{
    private Board board;
    private MenuGroup group;

    public SudokuSettings(Board board) {
        this.board = board;
    }
    public SudokuSettings() {

    }
    public void SetBoard(Board board)
    {
        this.board = board;
    }

    public MenuGroup GetUI(MenuView view)
    {
        SudokuView sudokuView = (SudokuView)view;
        Style style = sudokuView.GetStyle();

        style.StartParameters();
        style.GetStringParameter(view, new ParameterHolder<String>() {
            public void Set(String value) {
                board.SetTitle(value);
            }
            public String Get() {
                return board.GetTitle();
            }
        }, "Title", null);
        style.GetStringParameter(view, new ParameterHolder<String>() {
            public void Set(String value) {
                board.SetAuthor(value);
            }
            public String Get() {
                return board.GetAuthor();
            }
        }, "Author", null);
        style.GetMultiStringParameter(view, new ParameterHolder<String>() {
            public void Set(String value) {
                board.SetDescription(value);
            }
            public String Get() {
                return board.GetDescription();
            }
        }, "Short Description", null, 4);
        style.GetMultiStringParameter(view, new ParameterHolder<String>() {
            public void Set(String value) {
                board.SetRuleSet(value);
            }
            public String Get() {
                return board.GetRuleSet();
            }
        }, "Full Rules Description", null, 10);
        group = style.GetParameterWindow();

        return group;
    }

}
