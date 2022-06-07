package main.frames;

import main.FileLoader;
import main.Window;
import main.WindowFrame;
import main.sudoku.*;
import main.sudoku.graphics.CellClues;
import main.sudoku.graphics.Constraint;
import main.sudoku.graphics.Graphic;
import main.sudoku.graphics.GraphicsManager;
import main.ui.Style;
import main.ui.UICell;
import main.ui.graphics.UICellClue;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;

public class EditorWindow implements WindowFrame, SudokuController {
    private boolean isRunning = false;
    private Thread thread;
    private ArrayList<Runnable> endActions;

    private EditorWindowView view;
    private FileLoader loader;
    private Window window;
    private Board board;
    private Style style;

    private String filePath;
    private boolean fileSaved = true;

    public enum NumpadMode {
        Digit(0), Symbol(1), Clue(2), Pattern(3);

        public int value;

        NumpadMode(int value) {
            this.value = value;
        }
    }
    private NumpadMode numpadMode;

    private int dimensionX, dimensionY;
    private CellClues[][] cellClues;

    private int symbolsPage, symbolsCount, patternsPage, patternsCount;
    private int clueIndex;

    private ArrayList<Point> selectedCells;
    private int outsideCells = 0;

    private int selectedGraphic, selectedConstraint;
    private ArrayList<Graphic> graphics;
    private ArrayList<Constraint> constraints;
    private ArrayList<Boolean> graphicsToggle, constraintsToggle;

    private SudokuSettings sudokuSettings;

    //region Constructors
    public EditorWindow(main.Window window, int width, int height, String filePath, boolean newFile, int dimX, int dimY) {
        this.window = window;
        view = new EditorWindowView(this, window, width, height);
        loader = FileLoader.getInstance();
        endActions = new ArrayList<>();
        style = Style.getInstance();
        symbolsCount = style.GetShapesCount();
        patternsCount = style.GetPatternsCount();
        this.filePath = filePath;

        style.SetParametersOffset(-500, 150);
        style.SetParametersAnchor(Style.Anchor.Upper_Right);
        board = new Board(this);
        if(newFile)
        {
            board.CreateNew(dimX, dimY);
        }
        if (filePath != null)
            LoadBoard(filePath);
        else
            LoadBoard(null);
        if (newFile)
            this.filePath = null;

        selectedCells = new ArrayList<>();

        symbolsPage = 0;
        LoadSymbolsPage();
        SelectClue(0);
        patternsPage = 0;
        LoadPatternsPage();

        sudokuSettings = new SudokuSettings(board);
        view.SetSettingsGroup(sudokuSettings.GetUI(view));

        SelectMode(NumpadMode.Digit);
        view.DeselectSettings();
    }

    public EditorWindow(main.Window window, int width, int height) {
        this(window, width, height, null, true);
    }
    public EditorWindow(main.Window window, int width, int height, String filePath, boolean newFile) {
        this(window, width, height, filePath, newFile, 9, 9);
    }
    //endregion

    //region Basic Functions

    public synchronized void start() {
        thread = new Thread(this);
        thread.start();
        isRunning = true;
    }

    public synchronized void stop() {
        try {
            thread.join();
            isRunning = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        long delta;
        long lastTick = 0, lastRender = 0, maxFps = 30, minDelta = 1000 / maxFps;
        boolean tr = true;

        while (isRunning) {
            if (endActions.size() > 0) {
                break;
            }

            delta = (System.currentTimeMillis() - lastTick);
            if (delta * 4 > minDelta) {
                Tick(delta / 1000.0);
                lastTick = System.currentTimeMillis();
            }

            if (isRunning && System.currentTimeMillis() - lastRender > minDelta) {
                view.Render();
                lastRender = System.currentTimeMillis();
            }
        }

        if (endActions.size() > 0)
            endActions.get(0).run();

        stop();
    }

    public Canvas getFrame() {
        return view;
    }

    private void Tick(double delta) {
        if (selectedGraphic != -1)
            view.UpdateGraphic(graphics.get(selectedGraphic), selectedGraphic);
        if (selectedConstraint != -1)
            view.UpdateConstraint(constraints.get(selectedConstraint), selectedConstraint);
        view.Tick(delta);
    }
    //endregion

    //region Main Bar
    public void LoadBoard(String path) {
        if (path != null) {
            try {
                board.BoardLoad(path);
            }
            catch(Exception e)
            {
                System.err.println("Couldn't Read .saf file, creating new board");
                e.printStackTrace();
                filePath = null;
            }
        }

        dimensionX = board.GetWidth();
        dimensionY = board.GetHeight();
        ConstructBoard();
        for (int i = 0; i < dimensionX; i++) {
            for (int j = 0; j < dimensionY; j++) {
                board.SetDigit(i, j, 0);
                board.SetPencil(i, j, 0);
                board.SetSmall(i, j, 0);
                board.SetColors(i, j, 0);
            }
        }

        graphics = board.GetGraphics();

        cellClues = new CellClues[dimensionX][dimensionY];
        for (int i = 0; i < dimensionX; i++) {
            for (int j = 0; j < dimensionY; j++) {
                cellClues[i][j] = new CellClues();
                cellClues[i][j].x = i;
                cellClues[i][j].y = j;
            }
        }
        boolean[][] usedCellClues = new boolean[dimensionX][dimensionY];
        for (Graphic g : graphics) {
            if (g instanceof CellClues) {
                CellClues cc = (CellClues) g;
                int x = cc.x, y = cc.y;
                cellClues[x][y] = cc;
                usedCellClues[x][y] = true;
            }
        }
        for (int i = 0; i < dimensionX; i++) {
            for (int j = 0; j < dimensionY; j++) {
                if (!usedCellClues[i][j]) {
                    board.AddGraphic(cellClues[i][j]);
                }
            }
        }
        graphics = board.GetGraphics();
        constraints = board.GetConstraintGraphics();
        graphicsToggle = new ArrayList<>();
        for (int i = 0; i < graphics.size(); i++) {
            graphicsToggle.add(true);
        }
        constraintsToggle = new ArrayList<>();
        for (int i = 0; i < constraints.size(); i++) {
            constraintsToggle.add(true);
        }
        selectedGraphic = -1;
        selectedConstraint = -1;
        CreateGraphics();
        for (int i = 0; i < dimensionX; i++) {
            for (int j = 0; j < dimensionY; j++) {
                UpdateCellClue(i, j);
            }
        }
    }

    private BufferedImage toBufferedImage(Image image)
    {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }
        BufferedImage res = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = res.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return res;
    }

    public void SaveBoard(String path) {
        boolean[] selectedGraphics = new boolean[graphics.size()], selectedConstraints = new boolean[constraints.size()];
        for(int i = 0; i < graphics.size(); i++) {
            selectedGraphics[i] = graphics.get(i).IsSelected();
            graphics.get(i).Deselect();
        }
        for(int i = 0; i < constraints.size(); i++) {
            selectedConstraints[i] = constraints.get(i).IsSelected();
            constraints.get(i).Deselect();
        }
        view.DeselectCells();
        BufferedImage boardRender = view.RenderBoard();
        for(int i = 0; i < graphics.size(); i++) {
            if(selectedGraphics[i])
                graphics.get(i).Select();
        }
        for(int i = 0; i < constraints.size(); i++) {
            if(selectedConstraints[i])
                constraints.get(i).Select();
        }
        view.SelectCells(selectedCells);

        Point2D.Double boardPos = style.GetPoint(view.GetNodePos(0, 0), Style.Anchor.Upper_Left),
                boardPos2 = style.GetPoint(view.GetNodePos(dimensionX, dimensionY), Style.Anchor.Upper_Left);
        Point pos1 = new Point((int)boardPos.x, (int)boardPos.y), pos2 = new Point((int)(boardPos2.x-boardPos.x), (int)(boardPos2.y-boardPos.y));
        BufferedImage fullRender = boardRender.getSubimage(pos1.x, pos1.y, pos2.x, pos2.y);
        int prevWidth = fullRender.getWidth(), prevHeight = fullRender.getHeight();
        double scale = 256.0/Math.max(prevWidth, prevHeight);
        prevWidth *= scale;
        prevHeight *= scale;
        BufferedImage preview = toBufferedImage(fullRender.getScaledInstance(prevWidth, prevHeight, Image.SCALE_SMOOTH));
        board.SetFullImage(fullRender);
        board.SetPreview(preview);

        ArrayList<Graphic> saveGraphics = new ArrayList<>();
        for (int i = 0; i < graphics.size(); i++) {
            Graphic graphic = graphics.get(i);
            if (graphicsToggle.get(i)) {
                if (!(graphic instanceof CellClues) || !((CellClues) graphic).IsEmpty()) {
                    saveGraphics.add(graphics.get(i));
                }
            }
        }
        board.SetGraphics(saveGraphics);
        ArrayList<Constraint> saveConstraints = new ArrayList<>();
        ArrayList<SudokuConstraint> sudokuConstraints = new ArrayList<>();
        for (int i = 0; i < constraints.size(); i++) {
            Constraint constraint = constraints.get(i);
            if (constraintsToggle.get(i)) {
                saveConstraints.add(constraint);
                SudokuConstraint sc = constraint.GetConstraint();
                sc.SetBoard(board);
                sudokuConstraints.add(sc);
            }
        }
        board.SetConstraints(sudokuConstraints);
        board.SetConstraintGraphics(saveConstraints);
        board.BoardSave(path);
    }

    public boolean SelectFile() {
        try {
            FileDialog dialog = new FileDialog(window.getFrame());
            dialog.setMode(FileDialog.SAVE);
            dialog.setVisible(true);
            if (dialog.getFile() == null)
                return false;
            String path = dialog.getDirectory() + dialog.getFile();
            filePath = path;
            return true;
        } catch (Exception e) {
            System.err.println("Couldn't open file browser");
            e.printStackTrace();
            return false;
        }
    }

    public void MainMenu() {
        endActions.add(() -> window.LoadMainMenu());
    }

    public void Save() {
        if (filePath == null || filePath == "") {
            if (!SelectFile())
                return;
        }
        SaveBoard(filePath);
    }

    public void SaveAs() {
        if (!SelectFile())
            return;
        SaveBoard(filePath);
    }
    //endregion

    //region SudokuController Implementation
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
    //endregion

    //region Board Construction
    public void ConstructBoard() {
        view.ConstructBoard();
        int w = board.GetWidth(), h = board.GetHeight();
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                Cell cell = board.GetCell(i, j);
                UpdateCell(i, j, cell);
            }
        }
    }

    public void CreateGraphics() {
        graphicsToggle = new ArrayList<>();
        constraintsToggle = new ArrayList<>();
        for (int i = 0; i < graphics.size(); i++) {
            graphicsToggle.add(true);
        }
        for (int i = 0; i < constraints.size(); i++) {
            constraintsToggle.add(true);
        }
        view.CreateGraphics(constraints, graphics);
    }
    //endregion

    //region Cells Selection
    public void ToggleCell(int x, int y) {
        if (selectedGraphic != -1) {
            SelectGraphics(-1);
        }
        if (selectedConstraint != -1) {
            SelectConstraint(-1);
        }
        view.DeselectSettings();

        int d = 1;
        if (selectedCells.contains(new Point(x, y))) {
            d = -1;
            selectedCells.remove(new Point(x, y));
        } else {
            selectedCells.add(new Point(x, y));
        }
        if (selectedCells.size() == 1) {
            view.SelectNumpad(board.GetCell(selectedCells.get(0).x, selectedCells.get(0).y).GetValue(), false);
        } else {
            view.SelectNumpad(0, false);
        }
        if(board.GetCell(selectedCells.get(0).x, selectedCells.get(0).y).GetType() == Cell.CellType.Outside) {
            outsideCells += d;
        }
        String text = "";
        switch (clueIndex) {
            case 0:
                for (Point p : selectedCells) {
                    if (cellClues[p.x][p.y].ULClue.equals(""))
                        continue;
                    if (!text.equals("") && !cellClues[p.x][p.y].ULClue.equals(text)) {
                        text = "";
                        break;
                    } else {
                        text = cellClues[p.x][p.y].ULClue;
                    }
                }
                break;
            case 1:
                for (Point p : selectedCells) {
                    if (cellClues[p.x][p.y].LLClue.equals(""))
                        continue;
                    if (!text.equals("") && !cellClues[p.x][p.y].LLClue.equals(text)) {
                        text = "";
                        break;
                    } else {
                        text = cellClues[p.x][p.y].LLClue;
                    }
                }
                break;
            case 2:
                for (Point p : selectedCells) {
                    if (cellClues[p.x][p.y].URClue.equals(""))
                        continue;
                    if (!text.equals("") && !cellClues[p.x][p.y].URClue.equals(text)) {
                        text = "";
                        break;
                    } else {
                        text = cellClues[p.x][p.y].URClue;
                    }
                }
                break;
            case 3:
                for (Point p : selectedCells) {
                    if (cellClues[p.x][p.y].LRClue.equals(""))
                        continue;
                    if (!text.equals("") && !cellClues[p.x][p.y].LRClue.equals(text)) {
                        text = "";
                        break;
                    } else {
                        text = cellClues[p.x][p.y].LRClue;
                    }
                }
                break;
        }
        view.SetClueText(text);
        view.SelectCells(selectedCells);
        view.SelectTurnOff(outsideCells == selectedCells.size());
    }

    public void SelectCell(int x, int y) {
        if (selectedGraphic != -1) {
            SelectGraphics(-1);
        }
        if (selectedConstraint != -1) {
            SelectConstraint(-1);
        }
        view.DeselectSettings();

        selectedCells = new ArrayList<>();
        selectedCells.add(new Point(x, y));

        Cell cell = board.GetCell(x, y);
        view.SelectNumpad(cell.GetValue(), false);
        if(board.GetCell(selectedCells.get(0).x, selectedCells.get(0).y).GetType() == Cell.CellType.Outside) {
            outsideCells = 1;
        } else {
            outsideCells = 0;
        }
        String text = "";
        switch (clueIndex) {
            case 0:
                text = cellClues[x][y].ULClue;
                break;
            case 1:
                text = cellClues[x][y].LLClue;
                break;
            case 2:
                text = cellClues[x][y].URClue;
                break;
            case 3:
                text = cellClues[x][y].LRClue;
                break;
        }
        view.SetClueText(text);
        view.SelectCell(x, y);
        view.SelectTurnOff(outsideCells == selectedCells.size());
    }

    public void ClearCellSelection() {
        outsideCells = 0;
        selectedCells = new ArrayList<>();
        view.SelectCells(selectedCells);
        view.SelectTurnOff(outsideCells == selectedCells.size());
    }

    public void MoveSelection(int dx, int dy) {
        if (selectedCells.size() != 1)
            return;
        int selectedX = selectedCells.get(0).x, selectedY = selectedCells.get(0).y;
        selectedX += dx;
        selectedY += dy;
        if (selectedX < 0)
            selectedX = 0;
        else if (selectedX >= board.GetWidth())
            selectedX = board.GetWidth() - 1;

        if (selectedY < 0)
            selectedY = 0;
        else if (selectedY >= board.GetHeight())
            selectedY = board.GetHeight() - 1;

        SelectCell(selectedX, selectedY);
    }

    public void SelectMode(NumpadMode mode) {
        view.SelectMode(mode.value);
        numpadMode = mode;
    }
    //endregion

    //region Cell Editor

    public void ToggleOutside()
    {
        if(outsideCells == selectedCells.size())
        {
            for(Point p : selectedCells)
            {
                Cell cell = board.GetCell(p.x, p.y);
                if(cell.GetValue() != 0) {
                    cell.SetType(Cell.CellType.Frozen);
                } else{
                    cell.SetType(Cell.CellType.Regular);
                }
                UpdateCell(p.x, p.y, board.GetCell(p.x, p.y));
            }
            outsideCells = 0;
        }
        else {
            for(Point p : selectedCells)
            {
                Cell cell = board.GetCell(p.x, p.y);
                cell.SetType(Cell.CellType.Outside);
                UpdateCell(p.x, p.y, board.GetCell(p.x, p.y));
            }
            outsideCells = selectedCells.size();
        }
        view.SelectTurnOff(outsideCells == selectedCells.size());
    }

    //region Digits
    public void PressNumpad(int digit) {
        for (Point p : selectedCells) {
            if(board.GetCell(p.x, p.y).GetType() == Cell.CellType.Outside)
                outsideCells--;
            board.GetCell(p.x, p.y).SetDigit(digit);
            if (digit == 0)
                board.GetCell(p.x, p.y).SetType(Cell.CellType.Regular);
            else
                board.GetCell(p.x, p.y).SetType(Cell.CellType.Frozen);
            UpdateCell(p.x, p.y, board.GetCell(p.x, p.y));
        }
        if (selectedCells.size() == 1) {
            view.SelectNumpad(board.GetCell(selectedCells.get(0).x, selectedCells.get(0).y).GetValue(), false);
        } else {
            view.SelectNumpad(0, false);
        }
        view.SelectTurnOff(outsideCells == selectedCells.size());
    }

    public void ClearNumpad() {
        for (Point p : selectedCells) {
            board.GetCell(p.x, p.y).SetDigit(0);
            CellClues clue = cellClues[p.x][p.y];
            clue.symbol = -1;
            clue.pattern = -1;
            clue.ULClue = clue.URClue = clue.LLClue = clue.LRClue = "";
            if(board.GetCell(p.x, p.y).GetType() != Cell.CellType.Outside)
                board.GetCell(p.x, p.y).SetType(Cell.CellType.Regular);
            UpdateCell(p.x, p.y, board.GetCell(p.x, p.y));
            UpdateCellClue(p.x, p.y);
        }
        if (selectedCells.size() == 1) {
            view.SelectNumpad(board.GetCell(selectedCells.get(0).x, selectedCells.get(0).y).GetValue(), false);
        } else {
            view.SelectNumpad(0, false);
        }
    }
    //endregion

    //region Symbols
    public void ClickSymbol(int index) {
        for (Point p : selectedCells) {
            cellClues[p.x][p.y].symbol = index + symbolsPage;
            UpdateCellClue(p.x, p.y);
        }
    }

    public void ClearSymbol() {
        for (Point p : selectedCells) {
            cellClues[p.x][p.y].symbol = -1;
            UpdateCellClue(p.x, p.y);
        }
    }

    private void LoadSymbolsPage() {
        for (int i = 0; i < 6; i++) {
            view.SetSymbol(i, symbolsPage + i);
        }
        view.SetSymbolLabel((symbolsPage / 6) + 1 + "/" + (symbolsCount + 5) / 6);
    }

    public void MoveSymbolsPage(int shift) {
        symbolsPage += shift * 6;
        if (symbolsPage < 0)
            symbolsPage = 0;
        else if (symbolsPage >= symbolsCount)
            symbolsPage = (symbolsCount - 1) / 6 * 6;
        LoadSymbolsPage();
    }
    //endregion

    //region Clues
    public void SelectClue(int index) {
        clueIndex = index;
        String text = "";
        switch (clueIndex) {
            case 0:
                for (Point p : selectedCells) {
                    if (!text.equals("") && !cellClues[p.x][p.y].ULClue.equals(text)) {
                        text = "";
                        break;
                    } else {
                        text = cellClues[p.x][p.y].ULClue;
                    }
                }
                break;
            case 1:
                for (Point p : selectedCells) {
                    if (!text.equals("") && !cellClues[p.x][p.y].LLClue.equals(text)) {
                        text = "";
                        break;
                    } else {
                        text = cellClues[p.x][p.y].LLClue;
                    }
                }
                break;
            case 2:
                for (Point p : selectedCells) {
                    if (!text.equals("") && !cellClues[p.x][p.y].URClue.equals(text)) {
                        text = "";
                        break;
                    } else {
                        text = cellClues[p.x][p.y].URClue;
                    }
                }
                break;
            case 3:
                for (Point p : selectedCells) {
                    if (!text.equals("") && !cellClues[p.x][p.y].LRClue.equals(text)) {
                        text = "";
                        break;
                    } else {
                        text = cellClues[p.x][p.y].LRClue;
                    }
                }
                break;
        }
        view.SetClueText(text);
        view.SetClueIndex(clueIndex);
    }

    public void SetClueText(String text) {
        switch (clueIndex) {
            case 0:
                for (Point p : selectedCells) {
                    cellClues[p.x][p.y].ULClue = text;
                }
                break;
            case 1:
                for (Point p : selectedCells) {
                    cellClues[p.x][p.y].LLClue = text;
                }
                break;
            case 2:
                for (Point p : selectedCells) {
                    cellClues[p.x][p.y].URClue = text;
                }
                break;
            case 3:
                for (Point p : selectedCells) {
                    cellClues[p.x][p.y].LRClue = text;
                }
                break;
        }
        for (Point p : selectedCells) {
            UpdateCellClue(p.x, p.y);
        }
    }
    //endregion

    //region Patterns
    public void ClickPattern(int index) {
        for (Point p : selectedCells) {
            cellClues[p.x][p.y].pattern = index + patternsPage;
            UpdateCellClue(p.x, p.y);
        }
    }

    public void ClearPattern() {
        for (Point p : selectedCells) {
            cellClues[p.x][p.y].pattern = -1;
            UpdateCellClue(p.x, p.y);
        }
    }

    private void LoadPatternsPage() {
        for (int i = 0; i < 6; i++) {
            view.SetPattern(i, patternsPage + i);
        }
        view.SetPatternLabel((patternsPage / 6) + 1 + "/" + (patternsCount + 5) / 6);
    }

    public void MovePatternsPage(int shift) {
        patternsPage += shift * 6;
        if (patternsPage < 0)
            patternsPage = 0;
        else if (patternsPage >= patternsCount)
            patternsPage = (patternsCount - 1) / 6 * 6;
        LoadPatternsPage();
    }
    //endregion

    //endregion

    //region View Update
    public void UpdateCell(int x, int y, Cell cell) {
        UICell uiCell = view.GetCell(x, y);
        String main = "";
        if (cell.GetValue() != 0)
            main = ((Integer) cell.GetValue()).toString();
        uiCell.SetTexts(main, "", "");
        uiCell.SetColors(new Color[]{new Color(0, 0, 0, 0)});
        switch (board.GetCell(x, y).GetType()) {
            case Regular:
                uiCell.SetTextColor(style.GetColor(Style.ColorPalette.Body3.value));
                uiCell.SetOutside(false);
                break;
            case Frozen:
                uiCell.SetTextColor(style.GetColor(Style.ColorPalette.Body1.value + 2));
                uiCell.SetOutside(false);
                break;
            case Outside:
                uiCell.SetOutside(true);
                break;
        }
    }

    public void UpdateCellClue(int x, int y) {
        view.SetCellClue(x, y, cellClues[x][y]);
    }

    public void UpdateConstraint(int i, boolean value) {

    }

    public void UpdateConstraints(boolean[] values) {

    }
    //endregion

    //region Constraints & Graphics
    public void SelectGraphics(int ind) {
        if (selectedGraphic != -1) {
            graphics.get(selectedGraphic).Deselect();
            view.DeselectGraphic(selectedGraphic);
            view.UpdateGraphic(graphics.get(selectedGraphic), selectedGraphic);
        }
        view.DeselectSettings();
        if (selectedConstraint != -1) {
            constraints.get(selectedConstraint).Deselect();
            view.DeselectConstraint(selectedConstraint);
            view.UpdateConstraint(constraints.get(selectedConstraint), selectedConstraint);
        }
        selectedConstraint = -1;
        if (ind != -1) {
            graphics.get(ind).Select();
            ClearCellSelection();
            view.SelectMode(-1);
            view.SelectGraphic(ind);
        } else {
            view.ClearInputField();
            view.SelectMode(numpadMode.value);
        }
        selectedGraphic = ind;
    }

    public void ToggleGraphic(int ind) {
        graphicsToggle.set(ind, !graphicsToggle.get(ind));
        view.ToggleGraphic(ind, graphicsToggle.get(ind));
    }

    public void SelectConstraint(int ind) {
        if (selectedConstraint != -1) {
            constraints.get(selectedConstraint).Deselect();
            view.DeselectConstraint(selectedConstraint);
            view.UpdateConstraint(constraints.get(selectedConstraint), selectedConstraint);
        }
        view.DeselectSettings();
        if (selectedGraphic != -1) {
            graphics.get(selectedGraphic).Deselect();
            view.DeselectGraphic(selectedGraphic);
            view.UpdateGraphic(graphics.get(selectedGraphic), selectedGraphic);
        }
        selectedGraphic = -1;
        if (ind != -1) {
            constraints.get(ind).Select();
            ClearCellSelection();
            view.SelectMode(-1);
            view.SelectConstraint(ind);
        } else {
            view.ClearInputField();
            view.SelectMode(numpadMode.value);
        }
        selectedConstraint = ind;
    }

    public void ToggleConstraint(int ind) {
        constraintsToggle.set(ind, !constraintsToggle.get(ind));
        view.ToggleConstraint(ind, constraintsToggle.get(ind));
    }

    public void DeleteGraphic(int ind) {
        if (selectedGraphic != -1) {
            SelectGraphics(-1);
        }if (selectedConstraint != -1) {
            SelectConstraint(-1);
        }

        graphics.remove(ind);
        graphicsToggle.remove(ind);
        CreateGraphics();
    }

    public void DeleteConstraint(int ind) {
        if (selectedGraphic != -1) {
            SelectGraphics(-1);
        }if (selectedConstraint != -1) {
            SelectConstraint(-1);
        }
        constraints.remove(ind);
        constraintsToggle.remove(ind);
        CreateGraphics();
    }

    public void AddGraphic(int ind)
    {
        if (selectedGraphic != -1) {
            SelectGraphics(-1);
        }if (selectedConstraint != -1) {
        SelectConstraint(-1);
         }
        try {
            GraphicsManager manager = GraphicsManager.getInstance();
            Class<?> cl = manager.GetGraphicsClasses()[ind];
            Constructor<?> constructor = cl.getConstructor();
            Graphic sc = (Graphic) constructor.newInstance(new Object[]{});
            graphics.add(sc);
            graphicsToggle.add(true);
            CreateGraphics();
            SelectGraphics(graphics.size()-1);
        }
        catch(Exception e)
        {
            System.err.println("Couldn't create new graphic " + ind);
            e.printStackTrace();
            return;
        }
    }

    public void AddConstraint(int ind)
    {
        if (selectedGraphic != -1) {
            SelectGraphics(-1);
        }if (selectedConstraint != -1) {
        SelectConstraint(-1);
        }
        try {
            GraphicsManager manager = GraphicsManager.getInstance();
            Class<?> cl = manager.GetConstraintGraphicsClasses()[ind];
            Constructor<?> constructor = cl.getConstructor();
            Constraint sc = (Constraint) constructor.newInstance(new Object[]{});
            constraints.add(sc);
            constraintsToggle.add(true);
            CreateGraphics();
            SelectConstraint(constraints.size()-1);
        }
        catch(Exception e)
        {
            System.err.println("Couldn't create new constraint " + ind);
            e.printStackTrace();
            return;
        }
    }
    //endregion

    //region General Settings
    public void SelectSettings()
    {
        if(selectedGraphic != -1) {
            graphics.get(selectedGraphic).Deselect();
            view.DeselectGraphic(selectedGraphic);
            selectedGraphic = -1;
        }
        if(selectedConstraint != -1) {
            constraints.get(selectedConstraint).Deselect();
            view.DeselectConstraint(selectedConstraint);
            selectedConstraint = -1;
        }
        ClearCellSelection();
        view.SelectMode(-1);
        view.SelectSettings();
    }
    //endregion
}
