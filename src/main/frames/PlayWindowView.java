package main.frames;

import main.FileLoader;
import main.ObjectsHandler;
import main.Window;
import main.sudoku.Board;
import main.sudoku.SudokuView;
import main.sudoku.graphics.Constraint;
import main.sudoku.graphics.Graphic;
import main.ui.*;
import main.ui.graphics.UIConstraint;
import main.ui.graphics.UIImage;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;

public class PlayWindowView extends Canvas implements SudokuView {
    private Window window;
    private PlayWindow pres;

    private ObjectsHandler handler;
    private InputManager inputManager;
    private FileLoader fileLoader;
    private Style style;

    private int width, height;
    private Image background;

    private int dimensionX, dimensionY;
    private UICell[][] cells;

    private ArrayList<UIConstraint> constraints;
    private ArrayList<RenderObject> graphics;
    private BufferedImage lowGraphics, topGraphics;
    private UIImage lowGraphicsObject, topGraphicsObject;

    private double boardPosX, boardPosY, cellSize;

    private MenuButton[] digitButtons, modeButtons;

    private MenuButton startButton;
    private MenuPanel startPanel;

    private MenuLabel timerLabel, messageLabel;
    private long timerStart, timerEnd;
    private MenuButton checkButton;
    private boolean showTime = false;
    private MenuGroup menuSettings;

    public PlayWindowView(PlayWindow pres, Window window, int width, int height)
    {
        this.pres = pres;
        this.window = window;
        this.width = width;
        this.height = height;

        handler = new ObjectsHandler();
        inputManager = new InputManager(window, this);
        fileLoader = FileLoader.getInstance();
        style = Style.getInstance();
        style.LoadPalette(fileLoader);
        style.SetSize(width, height);
        style.GenerateShapes();
        style.SetHandler(handler);
        background = fileLoader.ReadImage("background.jpg").getScaledInstance(width, height, 0);

        UITransform transform = style.GetTransform(new UITransform(600, 0, 0, 0), Style.Anchor.Center);
        double digitsSize = style.GetScaled(70), posX = transform.x, posY = transform.y, pad = style.GetScaled(5);

        digitButtons = new MenuButton[11];
        String digitButtonNames = "1234567890<";
        int index = 0;
        for(int x = 0; x < 4; x++)
        {
            for(int y = 0; y < 3; y++)
            {
                if(x == 3 && y == 0)
                    continue;
                digitButtons[index] = new MenuButton(posX + y*(digitsSize+pad), posY + (x-1.5)*(digitsSize+pad), digitsSize, digitsSize,
                        style.GetScaled(10));
                style.SetButtonColors(digitButtons[index], 0);
                digitButtons[index].SetTextStyle(style.GetFont(1));
                digitButtons[index].SetText(String.valueOf(digitButtonNames.charAt(index)));
                int finalIndex = index;
                if(index < 10)
                    digitButtons[index].SetAction(() -> pres.PressNumpad((finalIndex +1)%10), 0);
                else
                    digitButtons[index].SetAction(() -> pres.ClearNumpad(), 0);
                handler.AddObject(digitButtons[index]);

                index++;
            }
        }
        modeButtons = new MenuButton[4];
        String modeNames = "dpsc";
        for(int x = 0; x < 4; x++)
        {
            modeButtons[x] = new MenuButton(posX + 3*(digitsSize+pad), posY + (x-1.5)*(digitsSize+pad), digitsSize, digitsSize,
                    style.GetScaled(10));
            style.SetButtonColors(modeButtons[x], 2);
            modeButtons[x].SetTextStyle(style.GetFont(1));
            modeButtons[x].SetText(String.valueOf(modeNames.charAt(x)));
            handler.AddObject(modeButtons[x]);
        }
        modeButtons[0].SetAction(() -> pres.SelectMode(PlayWindow.NumpadMode.Digit), 0);
        modeButtons[1].SetAction(() -> pres.SelectMode(PlayWindow.NumpadMode.Pencil), 0);
        modeButtons[2].SetAction(() -> pres.SelectMode(PlayWindow.NumpadMode.Set), 0);
        modeButtons[3].SetAction(() -> pres.SelectMode(PlayWindow.NumpadMode.Colors), 0);

        MenuButton menuButton;
        menuButton = new MenuButton(style.GetTransform(new UITransform(70, 30, 120, 40), Style.Anchor.Upper_Left),
                style.GetScaled(10.0));
        style.SetButtonColors(menuButton, 4);
        menuButton.SetText("Menu");
        menuButton.SetTextStyle(style.GetFont(5));
        menuButton.SetAction(() -> pres.MainMenu(), 0);
        menuButton.SetRenderPriority(65);
        handler.AddObject(menuButton);

        timerLabel = new MenuLabel(style.GetPoint(new Point2D.Double(40, -200), Style.Anchor.Left));
        timerLabel.SetAlignment(MenuLabel.Alignment.Left);
        timerLabel.SetFont(style.GetFont(1));
        timerLabel.SetColor(style.GetColor(Style.ColorPalette.Body3.value+2));
        timerLabel.SetText("");
        timerLabel.SetVisibility(false);
        handler.AddObject(timerLabel);

        checkButton = new MenuButton(style.GetTransform(new UITransform( 140+35, -150+25, 270, 50), Style.Anchor.Left),
                style.GetScaled(10.0));
        style.SetButtonColors(checkButton, 4);
        checkButton.SetText("Check Solution");
        checkButton.SetTextStyle(style.GetFont(7));
        checkButton.SetAction(() -> pres.ClickCheckSolution(), 0);
        checkButton.SetVisibility(false);
        handler.AddObject(checkButton);

        messageLabel = new MenuLabel(style.GetPoint(new Point2D.Double(40, -100), Style.Anchor.Left));
        messageLabel.SetFont(style.GetFont(5));
        messageLabel.SetAlignment(MenuLabel.Alignment.Upper_Left);
        messageLabel.SetVisibility(false);
        handler.AddObject(messageLabel);

        style.SetParametersOffset(40, -60);
        style.SetParametersAnchor(Style.Anchor.Left);
        style.StartParameters();
        style.GetBooleanParameter(pres.GetConflictsParameter(), "Show Conflicts");
        style.GetBooleanParameter(pres.GetCheckParameter(), "Auto Check");
        menuSettings = style.GetParameterWindow();
        menuSettings.SetVisible(false);

        lowGraphicsObject = new UIImage();
        lowGraphicsObject.SetRenderPriority(-1);
        topGraphicsObject = new UIImage();
        topGraphicsObject.SetRenderPriority(1);
        handler.AddObject(lowGraphicsObject);
        handler.AddObject(topGraphicsObject);

        startPanel = new MenuPanel(style.GetTransform(new UITransform(0, 0, 1000, 1000), Style.Anchor.Center));
        startPanel.SetPanel(true);
        startPanel.SetRoundness(style.GetScaled(30.0));
        startPanel.SetScreenSize(width, height);
        startPanel.SetColor(style.GetColor(Style.ColorPalette.Body2.value-2));
        startPanel.SetRenderPriority(64);
        handler.AddObject(startPanel);

        startButton = new MenuButton(style.GetTransform(new UITransform(0, 0, 200, 100), Style.Anchor.Center),
                style.GetScaled(15.0));
        style.SetButtonColors(startButton, 4);
        startButton.SetText("Start");
        startButton.SetTextStyle(style.GetFont(1));
        startButton.SetRenderPriority(65);
        startButton.SetAction(() -> pres.Start(), 0);
        handler.AddObject(startButton);
    }
    //region Main Functions

    public void Render()
    {
        BufferStrategy bs = this.getBufferStrategy();
        if(bs == null)
        {
            this.createBufferStrategy(3);
            return;
        }
        Graphics g = bs.getDrawGraphics();
        g.drawImage(background, 0, 0, null);

        handler.Render(g);
        g.dispose();
        bs.show();
    }

    public void Tick(double delta)
    {
        inputManager.SaveMouseState();

        for(KeyEvent event : inputManager.GetKeyInput())
        {
            if(event.getID() == KeyEvent.KEY_PRESSED)
            {
                switch (event.getKeyCode())
                {
                    case KeyEvent.VK_0:
                        pres.PressNumpad(0);
                        break;
                    case KeyEvent.VK_1:
                        pres.PressNumpad(1);
                        break;
                    case KeyEvent.VK_2:
                        pres.PressNumpad(2);
                        break;
                    case KeyEvent.VK_3:
                        pres.PressNumpad(3);
                        break;
                    case KeyEvent.VK_4:
                        pres.PressNumpad(4);
                        break;
                    case KeyEvent.VK_5:
                        pres.PressNumpad(5);
                        break;
                    case KeyEvent.VK_6:
                        pres.PressNumpad(6);
                        break;
                    case KeyEvent.VK_7:
                        pres.PressNumpad(7);
                        break;
                    case KeyEvent.VK_8:
                        pres.PressNumpad(8);
                        break;
                    case KeyEvent.VK_9:
                        pres.PressNumpad(9);
                        break;
                    case KeyEvent.VK_BACK_SPACE:
                    case KeyEvent.VK_DELETE:
                        pres.ClearNumpad();
                        break;
                    case KeyEvent.VK_D:
                        pres.SelectMode(PlayWindow.NumpadMode.Digit);
                        break;
                    case KeyEvent.VK_P:
                        pres.SelectMode(PlayWindow.NumpadMode.Pencil);
                        break;
                    case KeyEvent.VK_S:
                        pres.SelectMode(PlayWindow.NumpadMode.Set);
                        break;
                    case KeyEvent.VK_C:
                        pres.SelectMode(PlayWindow.NumpadMode.Colors);
                        break;
                    case KeyEvent.VK_UP:
                        pres.MoveSelection(0, -1);
                        break;
                    case KeyEvent.VK_DOWN:
                        pres.MoveSelection(0, 1);
                        break;
                    case KeyEvent.VK_LEFT:
                        pres.MoveSelection(-1, 0);
                        break;
                    case KeyEvent.VK_RIGHT:
                        pres.MoveSelection(1, 0);
                        break;
                }
            }
        }

        Point mousePos = inputManager.GetMousePosition();
       // cellSelector.SetTransform(new UITransform(mousePos.x, mousePos.y, 30, 30));
        int clickMask = 0;
        for(int i = 0; i < 3; i++)
        {
            if(inputManager.GetMouse(i+1))
                clickMask += (1 << i);
        }

        handler.Tick(delta);
        if(showTime) {
            long timeSpent = System.currentTimeMillis() - timerStart;
            long seconds = timeSpent/1000, minutes = timeSpent/(1000*60), hours = timeSpent/(1000*60*60), days = timeSpent/(1000*60*60*24);
            seconds %= 60;
            minutes %= 60;
            hours %= 24;
            String timeText = "";
            if(days > 0)
                timeText = Integer.valueOf((int)days).toString()+":";
            timeText += Integer.valueOf((int)hours/10).toString() + Integer.valueOf((int)hours%10).toString() + ":";
            timeText += Integer.valueOf((int)minutes/10).toString() + Integer.valueOf((int)minutes%10).toString() + ":";
            timeText += Integer.valueOf((int)seconds/10).toString() + Integer.valueOf((int)seconds%10).toString();
            timerLabel.SetText(timeText);
        }

        handler.UpdateMouse(mousePos.x, mousePos.y, clickMask);
        handler.PushChange();
    }

    //endregion

    public void Start()
    {
        handler.RemoveObject(startPanel);
        handler.RemoveObject(startButton);
        timerLabel.SetVisibility(true);
        checkButton.SetVisibility(true);
        menuSettings.SetVisible(true);
        timerStart = System.currentTimeMillis();
        showTime = true;
    }
    public void Finish()
    {
        timerEnd = System.currentTimeMillis();
        showTime = false;
    }
    public void ShowMessage(String text, boolean win)
    {
        messageLabel.SetColor(style.GetColor((win)? Style.ColorPalette.Accent1.value+1 : Style.ColorPalette.Accent2.value+1));
        messageLabel.SetText(text);
        messageLabel.SetVisibility(true);
    }
    public void HideMessage()
    {
        messageLabel.SetVisibility(false);
    }
    public void SetCheckButtonState(boolean state)
    {
        if(!state)
        {
            style.SetButtonColors(checkButton, 4);
            checkButton.SetText("Check Solution");
        }
        else
        {
            style.SetButtonColors(checkButton, 5);
            checkButton.SetText("Continue Solving");
        }
    }

    private void ClickCell(int x, int y)
    {
        if(inputManager.IsHeld(KeyEvent.VK_CONTROL))
        {
            pres.ToggleCell(x, y);
        }
        else
        {
            pres.SelectCell(x, y);
        }
    }

    //region Input Actions
    public void ConstructBoard()
    {
        if(cells != null)
        {
            for(int i = 0; i < cells.length; i++)
            {
                for(int j = 0; j < cells[i].length; j++)
                {
                    handler.RemoveObject(cells[i][j]);
                }
            }
        }
        Board board = pres.GetBoard();
        dimensionX = board.GetWidth();
        dimensionY = board.GetHeight();
        cells = new UICell[dimensionX][dimensionY];
        double size = 1080*0.9/ Math.max(dimensionX, dimensionY);

        boardPosX = 1920/2.0 - dimensionX/2.0 * size;
        boardPosY = 1080/2.0 - dimensionY/2.0 * size;
        cellSize = size;

        for(int i = 0; i < dimensionX; i++)
        {
            double posX = 0;
            if(dimensionX > 1)
                posX = i-(dimensionX-1)/2.0;
            for(int j = 0; j < dimensionY; j++)
            {
                double posY = 0;
                if(dimensionY > 1)
                    posY = j-(dimensionY-1)/2.0;

                cells[i][j] = new UICell(style.GetTransform(new UITransform(posX*size, posY*size, size, size), Style.Anchor.Center));
                cells[i][j].SetFont(style.GetFont(2));
                cells[i][j].SetRenderPriority(0);
                cells[i][j].SetSelectionWidth(style.GetScaled(10));
                cells[i][j].SetSelectionColor(style.GetColor(Style.ColorPalette.Accent1.value));
                handler.AddObject(cells[i][j]);

                int finalI = i;
                int finalJ = j;
                cells[i][j].SetAction(() -> ClickCell(finalI, finalJ));

                switch (board.GetCell(i, j).GetType())
                {
                    case Regular:
                        cells[i][j].SetTextColor(style.GetColor(Style.ColorPalette.Body3.value));
                        break;
                    case Frozen:
                        cells[i][j].SetTextColor(style.GetColor(Style.ColorPalette.Body1.value+2));
                        break;
                }
            }
        }
    }
    public void SelectNumpad(int mask, boolean immediate)
    {
        for(int i = 0; i < 10; i++)
        {
            if((mask & (1 << i)) > 0)
                style.SetButtonColors(digitButtons[i], 1);
            else
                style.SetButtonColors(digitButtons[i], 0);
        }
        if(immediate)
            for(int i = 0; i < 10; i++)
                digitButtons[i].ResetColors();
    }
    public void SelectMode(int index)
    {
        style.SetButtonColors(modeButtons[index], 3);
    }
    public void UnselectMode(int index)
    {
        style.SetButtonColors(modeButtons[index], 2);
    }

    public void DeselectCells()
    {
        for(int i = 0; i < dimensionX; i++)
        {
            for(int j = 0; j < dimensionY; j++)
            {
                cells[i][j].SetSelect(false);
            }
        }
    }

    public void SelectCell(int x, int y)
    {
        DeselectCells();
        cells[x][y].SetSelect(true);
    }
    public void SelectCells(ArrayList<Point> cells)
    {
        DeselectCells();
        for(Point p : cells)
        {
            this.cells[p.x][p.y].SetSelect(true);
        }
    }

    public void CreateGraphics(ArrayList<Constraint> constraints, ArrayList<Graphic> graphics)
    {
        this.constraints = new ArrayList<>();
        this.graphics = new ArrayList<>();

        for(Constraint constraint : constraints)
        {
            UIConstraint ui = constraint.Convert(this);
            this.constraints.add(ui);
            this.graphics.add((RenderObject)ui);
        }
        for(Graphic graphic : graphics)
        {
            this.graphics.add(graphic.Convert(this));
        }
        this.graphics.sort(Comparator.naturalOrder());
        UpdateGraphics();
    }
    public void UpdateGraphics()
    {
        lowGraphics = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        topGraphics = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = lowGraphics.getGraphics();
        boolean f = false;
        for(RenderObject ro : graphics)
        {
            if(!f && ro.GetRenderPriority() > 0)
            {
                g.dispose();
                g = topGraphics.getGraphics();
                f = true;
            }
            ro.Render(g);
        }
        g.dispose();
        lowGraphicsObject.ChangeImage(lowGraphics);
        topGraphicsObject.ChangeImage(topGraphics);
    }

    public enum ConstraintsState
    {
        Idle, Ok, Wrong
    };
    public void UpdateConstraint(int index, ConstraintsState state)
    {
        switch (state)
        {
            case Idle:
                constraints.get(index).SetIdle();
                break;
            case Ok:
                constraints.get(index).SetOk();
                break;
            case Wrong:
                constraints.get(index).SetWrong();
                break;
        }
        UpdateGraphics();
    }
    public void UpdateConstraints(ConstraintsState[] values)
    {
        for(int i = 0; i < values.length; i++)
        {
            switch (values[i])
            {
                case Idle:
                    constraints.get(i).SetIdle();
                    break;
                case Ok:
                    constraints.get(i).SetOk();
                    break;
                case Wrong:
                    constraints.get(i).SetWrong();
                    break;
            }
        }
        UpdateGraphics();
    }

    //endregion

    //region Access Functions

    public Point2D.Double GetNodePos(int x, int y)
    {
        return new Point2D.Double(boardPosX + cellSize*x, boardPosY + cellSize*y);
    }

    public Point2D.Double GetCellPos(int x, int y)
    {
        return new Point2D.Double(boardPosX + cellSize*(x+0.5), boardPosY + cellSize*(y+0.5));
    }

    public Point GetSize()
    {
        return new Point(dimensionX, dimensionY);
    }

    public Style GetStyle()
    {
        return style;
    }

    public UICell GetCell(int x, int y)
    {
        if(x < 0 || y < 0 || x >= dimensionX || y >= dimensionY)
            return null;
        return cells[x][y];
    }

    public MenuButton GetModeButton(int index)
    {
        return modeButtons[index];
    }

    //endregion
}
