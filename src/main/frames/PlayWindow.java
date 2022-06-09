package main.frames;

import main.FileLoader;
import main.ParameterHolder;
import main.Window;
import main.WindowFrame;
import main.sudoku.Board;
import main.sudoku.Cell;
import main.sudoku.SudokuController;
import main.ui.MenuButton;
import main.ui.Style;
import main.ui.UICell;

import java.awt.*;
import java.util.ArrayList;

public class PlayWindow implements WindowFrame, SudokuController
{
    private Window window;
    private FileLoader loader;
    private PlayWindowView view;

    private boolean isRunning = false;
    private Thread thread;
    private ArrayList<Runnable> endActions;

    private Board board;
    private Color[] markColors;

    public enum NumpadMode
    {
        Digit(0), Pencil(1), Set(2), Colors(3);

        private final int code;
        NumpadMode(int code)
        {
            this.code = code;
        }
    };
    private NumpadMode numpadMode;
    private int[] numpadMask;
    private ArrayList<Point> selectedCells;

    public enum ConstraintShowMode
    {
        DontShow, ShowAll, ShowWrong
    }
    private ConstraintShowMode showConstraintStatus = ConstraintShowMode.ShowWrong;
    private boolean[] constraintStates;
    private boolean autoCheck = false, showConflicts = true, showingSolve = false;

    public PlayWindow(Window window, int width, int height)
    {
        this(window, width, height, null);
    }
    public PlayWindow(Window window, int width, int height, String filePath)
    {
        this.window = window;
        view = new PlayWindowView(this, window, width, height);
        endActions = new ArrayList<>();

        loader = FileLoader.getInstance();

        markColors = loader.ReadColorArray("mark_colors.png");

        constraintStates = new boolean[1];
        board = new Board(this);
        ConstructBoard();

        selectedCells = new ArrayList<>();
        numpadMode = NumpadMode.Digit;
        numpadMask = new int[]{
                0, 0, 0, 0, 0, 0, 0, 0, 0
        };
        SelectMode(NumpadMode.Digit);

        showConstraintStatus = ConstraintShowMode.ShowWrong;

        ConstructBoard();
        if(filePath != null)
            LoadBoard(filePath);
        view.SetGeneralInfo(board.GetTitle(), board.GetAuthor(), board.GetRuleSet());
    }

    public Canvas getFrame()
    {
        return view;
    }

    public synchronized void start()
    {
        thread = new Thread(this);
        thread.start();
        isRunning = true;
    }

    public synchronized void stop()
    {
        try
        {
            thread.join();
            isRunning = false;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void run()
    {
        long delta;
        long lastTick = 0, lastRender = 0, maxFps = 60, minDelta = 1000/maxFps;
        boolean tr = true;

        while(isRunning) {
            if (endActions.size() > 0) {
                break;
            }

            delta = (System.currentTimeMillis() - lastTick);
            if (delta*2 > minDelta)
            {
                Tick(delta);
                lastTick = System.currentTimeMillis();
            }

            if(isRunning && System.currentTimeMillis()-lastRender > minDelta)
            {
                view.Render();
                lastRender = System.currentTimeMillis();
            }
        }

        if(endActions.size() > 0)
            endActions.get(0).run();

        stop();
    }

    private void Tick(double delta)
    {
        view.Tick(delta);
    }

    public void Start() {
        view.Start();
    }
    public void ClickCheckSolution()
    {
        if(!showingSolve)
        {
            showingSolve = true;
            showConstraintStatus = ConstraintShowMode.ShowWrong;
            CheckSolution();
        }
        else
        {
            showingSolve = false;
            if(showConflicts)
                showConstraintStatus = ConstraintShowMode.ShowWrong;
            else
                showConstraintStatus = ConstraintShowMode.DontShow;
            view.HideMessage();
        }
        view.SetCheckButtonState(showingSolve);
    }
    public void CheckSolution()
    {
        if(solutionIsCorrect())
            Win();
        else
            WrongSolution();
    }
    private boolean solutionIsCorrect()
    {
        if(board == null)
            return false;
        return board.CheckBoard();
    }
    public void Win()
    {
        view.Finish();
        view.ShowMessage("Congratulations!", true);
    }
    public void WrongSolution()
    {
        view.ShowMessage("Solution is not right!", false);
    }
    public void SetConflicts(boolean state)
    {
        showConflicts = state;
        if(!showingSolve) {
            if(showConflicts)
                showConstraintStatus = ConstraintShowMode.ShowWrong;
            else
                showConstraintStatus = ConstraintShowMode.DontShow;
        }
        UpdateConstraints(constraintStates);
    }
    public void SetAutoCheck(boolean state)
    {
        autoCheck = state;
        if(solutionIsCorrect())
            Win();
    }
    public ParameterHolder<Boolean> GetConflictsParameter()
    {
        return new ParameterHolder<Boolean>() {
            public void Set(Boolean value) {
                SetConflicts(value);
            }
            public Boolean Get() {
                return showConflicts;
            }
        };
    }
    public ParameterHolder<Boolean> GetCheckParameter()
    {
        return new ParameterHolder<Boolean>() {
            public void Set(Boolean value) {
                SetAutoCheck(value);
            }
            public Boolean Get() {
                return autoCheck;
            }
        };
    }
    public void MainMenu()
    {
        endActions.add(() -> window.LoadMainMenu());
    }

    public void LoadBoard()
    {
        LoadBoard("/home/kazinak/Desktop/PO2/SudokuArchitect/res/sudokuFile.saf");
    }
    public void LoadBoard(String path) {
        try {
            board.BoardLoad(path);
        }
        catch(Exception e)
        {
            System.err.println("Couldn't Open .saf file. Opening empty board.");
            e.printStackTrace();
        }
        ConstructBoard();
    }
    public void SaveBoard(String path)
    {
        board.BoardSave(path);
    }
    public void SaveBoard()
    {
        SaveBoard("/home/kazinak/Desktop/PO2/SudokuArchitect/res/sudokuFile.saf");
    }

    public void SetDigit(int x, int y, int digit) {
        board.SetDigit(x, y, digit);
    }
    public void SetPencil(int x, int y, int mask) {
        board.SetPencil(x, y, mask);
    }
    public void SetSmall(int x, int y, int mask) {
        board.SetSmall(x, y, mask);
    }
    public void SetColor(int x, int y, int mask) {
        board.SetColors(x, y, mask);
    }
    public Board GetBoard() {
        return board;
    }
    public void UpdateCell(int x, int y, Cell cell) {
        UICell uiCell = view.GetCell(x, y);
        String main = "";
        StringBuilder pencil = new StringBuilder();
        StringBuilder small = new StringBuilder();
        if(cell.GetValue() != 0)
            main = ((Integer)cell.GetValue()).toString();
        else
        {
            int mask = cell.GetPencil();
            for(int i = 0; i < 9; i++)
            {
                if((mask & (1 << i)) > 0)
                {
                    pencil.append((char) (i + 1 + '0'));
                }
            }
            mask = cell.GetSmall();
            for(int i = 0; i < 9; i++)
            {
                if((mask & (1 << i)) > 0)
                {
                    small.append((char) (i + 1 + '0'));
                }
            }
        }
        uiCell.SetTexts(main, pencil.toString(), small.toString());

        ArrayList<Color> colors = new ArrayList<>();
        if(cell.GetColor() == 0)
            colors.add(markColors[0]);
        else
        {
            int mask = cell.GetColor();
            for(int i = 0; i < 9; i++)
            {
                if((mask & (1 << i)) > 0)
                {
                    colors.add(markColors[i+1]);
                }
            }
        }
        uiCell.SetColors(colors.toArray(Color[]::new));
    }
    public void MoveSelection(int dx, int dy)
    {
        if(selectedCells.size() != 1)
            return;
        int selectedX = selectedCells.get(0).x, selectedY = selectedCells.get(0).y;
        selectedX += dx; selectedY += dy;
        if(selectedX < 0)
            selectedX = 0;
        else if(selectedX >= board.GetWidth())
            selectedX = board.GetWidth()-1;

        if(selectedY < 0)
            selectedY = 0;
        else if(selectedY >= board.GetHeight())
            selectedY = board.GetHeight()-1;

        SelectCell(selectedX, selectedY);
    }
    private void AddMaskToNumpad(int mask, int d)
    {
        for(int i = 0; i < 9; i++)
        {
            if((mask & (1 << i)) > 0)
                numpadMask[i] += d;
        }
    }
    private int NumpadToMask()
    {
        int mask = 0;
        if(selectedCells.size() == 0)
            return mask;
        for(int i = 0; i < 9; i++)
        {
            if(numpadMask[i] == selectedCells.size())
                mask |= (1 << i);
        }
        return mask;
    }
    public void SelectCell(int x, int y)
    {
        selectedCells = new ArrayList<>();
        selectedCells.add(new Point(x, y));

        Cell cell = board.GetCell(x, y);
        numpadMask = new int[]{
                0, 0, 0, 0, 0, 0, 0, 0, 0
        };
        switch (numpadMode)
        {
            case Digit:
                int val = cell.GetValue();
                if(val > 0)
                    numpadMask[val-1]++;
                break;
            case Pencil:
                AddMaskToNumpad(cell.GetPencil(), 1);
                break;
            case Set:
                AddMaskToNumpad(cell.GetSmall(), 1);
                break;
            case Colors:
                AddMaskToNumpad(cell.GetColor(), 1);
                break;
        }
        int mask = NumpadToMask();
        view.SelectNumpad(mask, true);
        view.SelectCell(x, y);
    }
    public void ToggleCell(int x, int y)
    {
        int d = 1;
        if(selectedCells.contains(new Point(x, y)))
        {
            d = -1;
            selectedCells.remove(new Point(x, y));
        }
        else {
            selectedCells.add(new Point(x, y));
        }
        Cell cell = board.GetCell(x, y);
        switch (numpadMode)
        {
            case Digit:
                int val = cell.GetValue();
                if(val > 0)
                    numpadMask[val-1] += d;
                break;
            case Pencil:
                AddMaskToNumpad(cell.GetPencil(), d);
                break;
            case Set:
                AddMaskToNumpad(cell.GetSmall(), d);
                break;
            case Colors:
                AddMaskToNumpad(cell.GetColor(), d);
                break;
        }
        view.SelectNumpad(NumpadToMask(), false);
        view.SelectCells(selectedCells);
    }
    public void UpdateSelection()
    {
        numpadMask = new int[]{
            0, 0, 0, 0, 0, 0, 0, 0, 0
        };
        switch (numpadMode)
        {
            case Digit:
                for(Point p : selectedCells) {
                    int val = board.GetCell(p.x ,p.y).GetValue();
                    if (val > 0)
                        numpadMask[val-1]++;
                }
                break;
            case Pencil:
                for(Point p : selectedCells) {
                    AddMaskToNumpad(board.GetCell(p.x, p.y).GetPencil(), 1);
                }
                break;
            case Set:
                for(Point p : selectedCells) {
                    AddMaskToNumpad(board.GetCell(p.x, p.y).GetSmall(), 1);
                }
                break;
            case Colors:
                for(Point p : selectedCells) {
                    AddMaskToNumpad(board.GetCell(p.x, p.y).GetColor(), 1);
                }
                break;
        }
        view.SelectCells(selectedCells);
        view.SelectNumpad(NumpadToMask(), false);
    }
    public void PressNumpad(int digit)
    {
        if(digit == 0)
        {
            numpadMask = new int[]{
                    0, 0, 0, 0, 0, 0, 0, 0, 0
            };
            switch (numpadMode) {
                case Digit:
                    for (Point p : selectedCells) {
                        int val = board.GetCell(p.x, p.y).GetValue();
                        if(val > 0)
                            numpadMask[val-1]--;
                        SetDigit(p.x, p.y, 0);
                        val = board.GetCell(p.x, p.y).GetValue();
                        if(val > 0)
                            numpadMask[val-1]++;
                    }
                    break;
                case Pencil:
                    for (Point p : selectedCells) {
                        int mask = board.GetCell(p.x, p.y).GetPencil();
                        AddMaskToNumpad(mask, -1);
                        SetPencil(p.x, p.y, 0);
                        mask = board.GetCell(p.x, p.y).GetPencil();
                        AddMaskToNumpad(mask, 1);
                    }
                    break;
                case Set:
                    for (Point p : selectedCells) {
                        int mask = board.GetCell(p.x, p.y).GetSmall();
                        AddMaskToNumpad(mask, -1);
                        SetSmall(p.x, p.y, 0);
                        mask = board.GetCell(p.x, p.y).GetSmall();
                        AddMaskToNumpad(mask, 1);
                    }
                    break;
                case Colors:
                    for (Point p : selectedCells) {
                        int mask = board.GetCell(p.x, p.y).GetColor();
                        AddMaskToNumpad(mask, -1);
                        SetColor(p.x, p.y, 0);
                        mask = board.GetCell(p.x, p.y).GetColor();
                        AddMaskToNumpad(mask, 1);
                    }
                    break;
            }
            view.SelectNumpad(NumpadToMask(), false);
            for(Point p : selectedCells){
                UpdateCell(p.x, p.y, board.GetCell(p.x, p.y));
            }
            return;
        }
        if(numpadMode == NumpadMode.Digit)
        {
            for (Point p : selectedCells) {
                int val = board.GetCell(p.x, p.y).GetValue();
                if(val > 0)
                    numpadMask[val-1]--;
                SetDigit(p.x, p.y, digit);
                val = board.GetCell(p.x, p.y).GetValue();
                if(val > 0)
                    numpadMask[val-1]++;
            }
        }
        else
        {
            digit--;
            boolean add = true;
            if(numpadMask[digit] == selectedCells.size())
                add = false;
            switch (numpadMode)
            {
                case Pencil:
                    for(Point p : selectedCells) {
                        int mask = board.GetCell(p.x, p.y).GetPencil();
                        AddMaskToNumpad(mask, -1);
                        mask |= (1 << digit);
                        if(!add)
                            mask ^= (1 << digit);
                        SetPencil(p.x, p.y, mask);
                        mask = board.GetCell(p.x, p.y).GetPencil();
                        AddMaskToNumpad(mask, 1);
                    }
                    break;
                case Set:
                    for(Point p : selectedCells) {
                        int mask = board.GetCell(p.x, p.y).GetSmall();
                        AddMaskToNumpad(mask, -1);
                        mask |= (1 << digit);
                        if(!add)
                            mask ^= (1 << digit);
                        SetSmall(p.x, p.y, mask);
                        mask = board.GetCell(p.x, p.y).GetSmall();
                        AddMaskToNumpad(mask, 1);
                    }
                    break;
                case Colors:
                    for(Point p : selectedCells) {
                        int mask = board.GetCell(p.x, p.y).GetColor();
                        AddMaskToNumpad(mask, -1);
                        mask |= (1 << digit);
                        if(!add)
                            mask ^= (1 << digit);
                        SetColor(p.x, p.y, mask);
                        mask = board.GetCell(p.x, p.y).GetColor();
                        AddMaskToNumpad(mask, 1);
                    }
                    break;
            }
        }
        view.SelectNumpad(NumpadToMask(), false);
        for(Point p : selectedCells){
            UpdateCell(p.x, p.y, board.GetCell(p.x, p.y));
        }
    }
    public void ClearNumpad()
    {
        numpadMask = new int[]{
            0, 0, 0, 0, 0, 0, 0, 0, 0
        };
        for(Point p : selectedCells) {
            SetDigit(p.x, p.y, 0);
            SetPencil(p.x, p.y, 0);
            SetSmall(p.x, p.y, 0);
            SetColor(p.x, p.y, 0);
            switch (numpadMode) {
                case Digit:
                    int val = board.GetCell(p.x, p.x).GetValue();
                    if(val > 0)
                        numpadMask[val-1]++;
                    break;
                case Pencil:
                    AddMaskToNumpad(board.GetCell(p.x, p.y).GetPencil(), 1);
                    break;
                case Set:
                    AddMaskToNumpad(board.GetCell(p.x, p.y).GetSmall(), 1);
                    break;
                case Colors:
                    AddMaskToNumpad(board.GetCell(p.x, p.y).GetColor(), 1);
                    break;
            }
        }
        for(Point p : selectedCells){
            UpdateCell(p.x, p.y, board.GetCell(p.x, p.y));
        }
        view.SelectNumpad(NumpadToMask(), false);
    }
    public void SelectMode(NumpadMode mode)
    {
        view.UnselectMode(numpadMode.code);

        numpadMode = mode;
        UpdateSelection();

        view.SelectMode(numpadMode.code);
    }
    public void ConstructBoard()
    {
        view.ConstructBoard();
        int w = board.GetWidth(), h = board.GetHeight();
        for(int i = 0; i < w; i++)
        {
            for(int j = 0; j < h; j++)
            {
                Cell cell = board.GetCell(i, j);
                if(cell.GetType() == Cell.CellType.Outside)
                    view.GetCell(i, j).SetVisibility(false);
                else
                {
                    UpdateCell(i, j, cell);
                }
            }
        }
        view.CreateGraphics(board.GetConstraintGraphics(), board.GetGraphics());
        constraintStates = board.GetConstraintsState();
        ClearConstraints();
    }
    public void UpdateConstraint(int i, boolean value) {
        constraintStates[i] = value;
        if(showConstraintStatus != ConstraintShowMode.DontShow)
            view.UpdateConstraint(i, (constraintStates[i])? PlayWindowView.ConstraintsState.Ok : PlayWindowView.ConstraintsState.Wrong);
    }
    public void UpdateConstraints(boolean[] values) {
        if(values == null)
            return;
        constraintStates = values;
        switch (showConstraintStatus)
        {
            case ShowWrong:
                ShowConstraints(PlayWindowView.ConstraintsState.Idle);
                break;
            case ShowAll:
                ShowConstraints(PlayWindowView.ConstraintsState.Ok);
                break;
            case DontShow:
                ClearConstraints();
                break;
        }
        if(solutionIsCorrect())
            Win();
    }
    public void ShowConstraints(PlayWindowView.ConstraintsState okState)
    {
        PlayWindowView.ConstraintsState[] values = new PlayWindowView.ConstraintsState[constraintStates.length];
        for(int i = 0; i < constraintStates.length; i++)
        {
            values[i] = (constraintStates[i])? okState : PlayWindowView.ConstraintsState.Wrong;
        }
        view.UpdateConstraints(values);
    }
    public void ClearConstraints() {
        PlayWindowView.ConstraintsState[] values = new PlayWindowView.ConstraintsState[constraintStates.length];
        for(int i = 0; i < constraintStates.length; i++)
        {
            values[i] = PlayWindowView.ConstraintsState.Idle;
        }
        view.UpdateConstraints(values);
    }
}
