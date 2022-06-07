package main.frames;

import main.FileLoader;
import main.ObjectsHandler;
import main.Window;
import main.sudoku.Board;
import main.sudoku.SudokuView;
import main.sudoku.graphics.*;
import main.ui.*;
import main.ui.graphics.UICellClue;
import main.ui.graphics.UIConstraint;
import main.ui.graphics.UIImage;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class EditorWindowView extends Canvas implements SudokuView, MenuView {
    private EditorWindow pres;
    private main.Window window;
    private FileLoader loader;
    private InputManager inputManager;
    private Style style;
    private ObjectsHandler handler;

    private int width, height;
    private Image background;

    private int dimensionX, dimensionY;
    private double boardPosX, boardPosY, cellSize;
    private UICell[][] cells;
    private UICellClue[][] cellClues;

    private ArrayList<RenderObject> graphics, constraints;
    private ArrayList<MenuGraphic> graphicButtons;
    private ArrayList<MenuConstraint> constraintButtons;

    MenuScrollWindow scrollWindow;

    private MenuInputField selectedField;

    private double numpadSize;
    private MenuGroup[] numpadPanels;
    private MenuButton[] digitButtons, modeButtons;
    private ShapeButton[] symbolButtons, patternButtons;
    private MenuButton symbolsLeft, symbolsRight, patternsLeft, patternsRight, turnOffCell;
    private MenuLabel symbolsLabel, patternsLabel;
    private MenuInputField cluesField;
    private MenuButton[] cluesButtons;

    private MenuGroup settingsGroup;
    private MenuButton settingsButton;

    private MenuButton addElementButton;
    private MenuGroup addElementWindow;

    public EditorWindowView(EditorWindow pres, Window window, int width, int height) {
        this.pres = pres;
        this.window = window;
        loader = FileLoader.getInstance();
        style = Style.getInstance();
        style.LoadPalette(loader);
        style.SetSize(width, height);
        style.GenerateShapes();
        inputManager = new InputManager(window, this);
        this.window = window;
        handler = new ObjectsHandler();
        style.SetHandler(handler);

        this.width = width;
        this.height = height;
        background = loader.ReadImage("background.jpg").getScaledInstance(width, height, 0);

        selectedField = null;

        UITransform transform = style.GetTransform(new UITransform(450, 0, 0, 0), Style.Anchor.Center);
        double digitsSize = style.GetScaled(55), posX = transform.x, posY = transform.y, pad = style.GetScaled(5);
        numpadSize = digitsSize;

        numpadPanels = new MenuGroup[4];
        //Digits Numpad
        {
            numpadPanels[0] = new MenuGroup();
            digitButtons = new MenuButton[11];
            String digitButtonNames = "1234567890<";
            int index = 0;
            for (int x = 0; x < 4; x++) {
                for (int y = 0; y < 3; y++) {
                    if (x == 3 && y == 0)
                        continue;
                    digitButtons[index] = new MenuButton(posX + y * (digitsSize + pad), posY + (x - 1.5) * (digitsSize + pad),
                            digitsSize, digitsSize, style.GetScaled(10));
                    style.SetButtonColors(digitButtons[index], 0);
                    digitButtons[index].SetTextStyle(style.GetFont(1));
                    digitButtons[index].SetText(String.valueOf(digitButtonNames.charAt(index)));
                    int finalIndex = index;
                    if (index < 10)
                        digitButtons[index].SetAction(() -> pres.PressNumpad((finalIndex + 1) % 10), 0);
                    else
                        digitButtons[index].SetAction(() -> pres.ClearNumpad(), 0);

                    handler.AddObject(digitButtons[index]);
                    numpadPanels[0].AddObject(digitButtons[index]);
                    index++;
                }
            }
        }
        //Symbols Numpad
        {
            numpadPanels[1] = new MenuGroup();
            symbolButtons = new ShapeButton[6];
            int ind = 0;
            for (int y = 0; y < 2; y++) {
                for (int x = 0; x < 3; x++) {
                    symbolButtons[ind] = new ShapeButton(posX + x * (digitsSize + pad), posY + (y - 1.5) * (digitsSize + pad),
                            digitsSize, digitsSize, 10);
                    style.SetButtonColors(symbolButtons[ind], 0);
                    int finalInd = ind;
                    symbolButtons[ind].SetAction(() -> pres.ClickSymbol(finalInd), 0);
                    handler.AddObject(symbolButtons[ind]);
                    numpadPanels[1].AddObject(symbolButtons[ind]);
                    ind++;
                }
            }
            symbolsLeft = new MenuButton(posX, posY + (0.5) * (digitsSize + pad), digitsSize, digitsSize,
                    style.GetScaled(10.0));
            style.SetButtonColors(symbolsLeft, 2);
            symbolsLeft.SetAction(() -> pres.MoveSymbolsPage(-1), 0);
            handler.AddObject(symbolsLeft);
            numpadPanels[1].AddObject(symbolsLeft);

            symbolsRight = new MenuButton(posX + 2 * (digitsSize + pad), posY + (0.5) * (digitsSize + pad), digitsSize, digitsSize, 
                    style.GetScaled(10));
            style.SetButtonColors(symbolsRight, 2);
            symbolsRight.SetAction(() -> pres.MoveSymbolsPage(1), 0);
            handler.AddObject(symbolsRight);
            numpadPanels[1].AddObject(symbolsRight);

            symbolsLabel = new MenuLabel(posX + (digitsSize + pad), posY + (0.5) * (digitsSize + pad));
            symbolsLabel.SetColor(style.GetColor(Style.ColorPalette.Body3.value + 1));
            symbolsLabel.SetFont(style.GetFont(4));
            symbolsLabel.SetAlignment(MenuLabel.Alignment.Center);
            handler.AddObject(symbolsLabel);
            numpadPanels[1].AddObject(symbolsLabel);

            MenuButton symbolsClear = new MenuButton(posX + (digitsSize + pad), posY + (1.5) * (digitsSize + pad), digitsSize, digitsSize, 
                    style.GetScaled(10));
            style.SetButtonColors(symbolsClear, 2);
            symbolsClear.SetAction(() -> pres.ClearSymbol(), 0);
            symbolsClear.SetText("x");
            symbolsClear.SetTextStyle(style.GetFont(0));
            handler.AddObject(symbolsClear);
            numpadPanels[1].AddObject(symbolsClear);

            MenuButton symbolsDelete = new MenuButton(posX + 2 * (digitsSize + pad), posY + (1.5) * (digitsSize + pad), digitsSize, digitsSize,  
                    style.GetScaled(10));
            style.SetButtonColors(symbolsDelete, 2);
            symbolsDelete.SetAction(() -> pres.ClearNumpad(), 0);
            symbolsDelete.SetText("<");
            symbolsDelete.SetTextStyle(style.GetFont(0));
            handler.AddObject(symbolsDelete);
            numpadPanels[1].AddObject(symbolsDelete);
        }
        // Clues Window
        {
            numpadPanels[2] = new MenuGroup();
            cluesField = new MenuInputField(posX + digitsSize + pad, posY - (1.5) * (digitsSize + pad), digitsSize * 3 + pad * 2, digitsSize);
            style.SetInputField(cluesField, digitsSize, 0);
            cluesField.SetPlaceholder("Enter Clue");
            cluesField.SetSelectAction(() -> SelectInputField(cluesField));
            cluesField.SetDeselectAction(() -> UpdateCluesText());
            handler.AddObject(cluesField);
            numpadPanels[2].AddObject(cluesField);

            cluesButtons = new MenuButton[4];
            int ind = 0;
            for (int x = 0; x < 2; x++) {
                for (int y = 0; y < 2; y++) {
                    cluesButtons[ind] = new MenuButton(posX + x * (digitsSize + pad), posY + (y - 0.5) * (digitsSize + pad), digitsSize, digitsSize,
                            style.GetScaled(10));
                    style.SetButtonColors(cluesButtons[ind], 0);
                    int finalInd = ind;
                    cluesButtons[ind].SetAction(() -> pres.SelectClue(finalInd), 0);
                    handler.AddObject(cluesButtons[ind]);
                    numpadPanels[2].AddObject(cluesButtons[ind]);
                    ind++;
                }
            }
        }
        //Patterns Numpad
        {
            numpadPanels[3] = new MenuGroup();
            patternButtons = new ShapeButton[6];
            int ind = 0;
            for (int y = 0; y < 2; y++) {
                for (int x = 0; x < 3; x++) {
                    patternButtons[ind] = new ShapeButton(posX + x * (digitsSize + pad), posY + (y - 1.5) * (digitsSize + pad),
                            digitsSize, digitsSize,  style.GetScaled(10));
                    style.SetButtonColors(patternButtons[ind], 0);
                    int finalInd = ind;
                    patternButtons[ind].SetAction(() -> pres.ClickPattern(finalInd), 0);
                    handler.AddObject(patternButtons[ind]);
                    numpadPanels[3].AddObject(patternButtons[ind]);
                    ind++;
                }
            }
            patternsLeft = new MenuButton(posX, posY + (0.5) * (digitsSize + pad), digitsSize, digitsSize,  style.GetScaled(10));
            style.SetButtonColors(patternsLeft, 2);
            patternsLeft.SetAction(() -> pres.MovePatternsPage(-1), 0);
            handler.AddObject(patternsLeft);
            numpadPanels[3].AddObject(patternsLeft);

            patternsRight = new MenuButton(posX + 2 * (digitsSize + pad), posY + (0.5) * (digitsSize + pad), digitsSize, digitsSize,
                    style.GetScaled(10));
            style.SetButtonColors(patternsRight, 2);
            patternsRight.SetAction(() -> pres.MovePatternsPage(1), 0);
            handler.AddObject(patternsRight);
            numpadPanels[3].AddObject(patternsRight);

            patternsLabel = new MenuLabel(posX + (digitsSize + pad), posY + (0.5) * (digitsSize + pad));
            patternsLabel.SetColor(style.GetColor(Style.ColorPalette.Body3.value + 1));
            patternsLabel.SetFont(style.GetFont(4));
            patternsLabel.SetAlignment(MenuLabel.Alignment.Center);
            handler.AddObject(patternsLabel);
            numpadPanels[3].AddObject(patternsLabel);

            MenuButton patternsClear = new MenuButton(posX + (digitsSize + pad), posY + (1.5) * (digitsSize + pad), digitsSize, digitsSize,
                    style.GetScaled(10));
            style.SetButtonColors(patternsClear, 2);
            patternsClear.SetAction(() -> pres.ClearPattern(), 0);
            patternsClear.SetText("x");
            patternsClear.SetTextStyle(style.GetFont(0));
            handler.AddObject(patternsClear);
            numpadPanels[3].AddObject(patternsClear);

            MenuButton patternsDelete = new MenuButton(posX + 2 * (digitsSize + pad), posY + (1.5) * (digitsSize + pad), digitsSize, digitsSize,
                    style.GetScaled(10));
            style.SetButtonColors(patternsDelete, 2);
            patternsDelete.SetAction(() -> pres.ClearNumpad(), 0);
            patternsDelete.SetText("<");
            patternsDelete.SetTextStyle(style.GetFont(0));
            handler.AddObject(patternsDelete);
            numpadPanels[3].AddObject(patternsDelete);
        }

        //region Numpad
        for (int i = 1; i < 4; i++)
            numpadPanels[i].SetVisible(false);
        modeButtons = new MenuButton[4];
        String modeNames = "dscp";
        for (int x = 0; x < 4; x++) {
            modeButtons[x] = new MenuButton(posX + 3 * (digitsSize + pad), posY + (x - 1.5) * (digitsSize + pad), digitsSize, digitsSize,
                    style.GetScaled(10));
            style.SetButtonColors(modeButtons[x], 2);
            modeButtons[x].SetTextStyle(style.GetFont(1));
            modeButtons[x].SetText(String.valueOf(modeNames.charAt(x)));
            handler.AddObject(modeButtons[x]);
        }
        modeButtons[0].SetAction(() -> pres.SelectMode(EditorWindow.NumpadMode.Digit), 0);
        modeButtons[1].SetAction(() -> pres.SelectMode(EditorWindow.NumpadMode.Symbol), 0);
        modeButtons[2].SetAction(() -> pres.SelectMode(EditorWindow.NumpadMode.Clue), 0);
        modeButtons[3].SetAction(() -> pres.SelectMode(EditorWindow.NumpadMode.Pattern), 0);

         turnOffCell = new MenuButton(posX, posY + 1.5 * (digitsSize + pad),
                digitsSize, digitsSize,  style.GetScaled(10));
         turnOffCell.SetText("");
         turnOffCell.SetAction(() -> pres.ToggleOutside(), 0);
         style.SetButtonColors(turnOffCell, 2);
         handler.AddObject(turnOffCell);

        //endregion

        //region Main Bar
        MenuButton loadButton, saveButton, saveAsButton;
        loadButton = new MenuButton(style.GetTransform(new UITransform(65, 20, 120, 35), Style.Anchor.Upper_Left),
                style.GetScaled(10));
        style.SetButtonColors(loadButton, 4);
        loadButton.SetText("Menu");
        loadButton.SetTextStyle(style.GetFont(5));
        loadButton.SetAction(() -> pres.MainMenu(), 0);
        handler.AddObject(loadButton);

        saveButton = new MenuButton(style.GetTransform(new UITransform(65, 60, 120, 35), Style.Anchor.Upper_Left),
                style.GetScaled(10));
        style.SetButtonColors(saveButton, 4);
        saveButton.SetText("Save");
        saveButton.SetTextStyle(style.GetFont(5));
        saveButton.SetAction(() -> pres.Save(), 0);
        handler.AddObject(saveButton);

        saveAsButton = new MenuButton(style.GetTransform(new UITransform(65, 100, 120, 35), Style.Anchor.Upper_Left),
                style.GetScaled(10));
        style.SetButtonColors(saveAsButton, 4);
        saveAsButton.SetText("Save As");
        saveAsButton.SetTextStyle(style.GetFont(5));
        saveAsButton.SetAction(() -> pres.SaveAs(), 0);
        handler.AddObject(saveAsButton);
        //endregion

        settingsButton = new MenuButton(style.GetTransform(new UITransform(120, -40, 200, 40), Style.Anchor.Lower_Left),
                style.GetScaled(10));
        style.SetButtonColors(settingsButton, 0);
        settingsButton.SetText("General Settings");
        settingsButton.SetTextStyle(style.GetFont(5));
        settingsButton.SetAction(() -> pres.SelectSettings(), 0);
        handler.AddObject(settingsButton);

        addElementButton = new MenuButton(style.GetTransform(new UITransform(80, 425, 120, 40), Style.Anchor.Left),
                style.GetScaled(10.0));
        style.SetButtonColors(addElementButton, 0);
        addElementButton.SetText("Add");
        addElementButton.SetTextStyle(style.GetFont(5));
        handler.AddObject(addElementButton);

        addElementWindow = new MenuGroup();
        {
            MenuPanel panel = new MenuPanel(style.GetTransform(new UITransform(0, 0, 1000, 800), Style.Anchor.Center));
            panel.SetRoundness(style.GetScaled(10.0));
            panel.SetPanel(true);
            panel.SetColor(style.GetColor(Style.ColorPalette.Body1.value-1));
            panel.SetScreenSize(width, height);
            panel.SetRenderPriority(64);
            addElementWindow.AddObject(panel);
            handler.AddObject(panel);

            AtomicBoolean selectMode = new AtomicBoolean(false);
            AtomicInteger graphicIndex = new AtomicInteger(0), constraintIndex = new AtomicInteger(0);

            MenuGroup graphicWindow = new MenuGroup(), constraintWindow = new MenuGroup();

            //region Graphic Window
            {
                UIImage graphicPreview = new UIImage();
                graphicPreview.SetTransform(style.GetTransform(new UITransform(395 - 100 + 10+20, -295 + 50, 200, 200), Style.Anchor.Center));
                graphicPreview.SetRenderPriority(65);
                graphicWindow.AddObject(graphicPreview);
                handler.AddObject(graphicPreview);

                MenuLabel graphicName = new MenuLabel(style.GetPoint(new Point2D.Double(395 - 100 + 10+20, -190 + 50), Style.Anchor.Center));
                graphicName.SetFont(style.GetFont(4));
                graphicName.SetColor(style.GetColor(Style.ColorPalette.Body3.value + 1));
                graphicName.SetAlignment(MenuLabel.Alignment.Top);
                graphicName.SetRenderPriority(65);
                graphicWindow.AddObject(graphicName);
                handler.AddObject(graphicName);

                MenuLabel graphicDescription = new MenuLabel(style.GetPoint(new Point2D.Double(395 - 200 + 10+20, -190 + 50+40), Style.Anchor.Center));
                graphicDescription.SetFont(style.GetFont(5));
                graphicDescription.SetColor(style.GetColor(Style.ColorPalette.Body1.value+2));
                graphicDescription.SetAlignment(MenuLabel.Alignment.Upper_Left);
                graphicDescription.SetRenderPriority(65);
                graphicWindow.AddObject(graphicDescription);
                handler.AddObject(graphicDescription);

                MenuScrollWindow graphicScrollWindow = new MenuScrollWindow(style.GetTransform(new UITransform(-150, 0, 690, 700),
                        Style.Anchor.Center));
                graphicScrollWindow.SetPadding(style.GetScaled(5.0));
                graphicScrollWindow.SetScrollThickness(style.GetScaled(20.0));
                graphicScrollWindow.SetColors(style.GetColor(Style.ColorPalette.Body2.value - 1), style.GetColor(Style.ColorPalette.Body1.value - 2));
                MenuScroller scroller = graphicScrollWindow.GetScroller();
                scroller.SetColor(style.GetColor(Style.ColorPalette.Body2.value + 1));
                scroller.GetDrag().SetColor(style.GetColor(Style.ColorPalette.Body2.value - 1));
                graphicScrollWindow.SetRenderPriority(65);

                int ofs = -340, ind = 0;
                GraphicsManager graphicsManager = GraphicsManager.getInstance();
                Class[] graphicsClasses = graphicsManager.GetGraphicsClasses();
                GraphicInfo[] graphicInfo = graphicsManager.GetGraphicsInfo();
                graphicPreview.SetImage(graphicInfo[0].image);
                graphicName.SetText(graphicInfo[0].name);
                graphicDescription.SetText(graphicInfo[0].description);
                for (int i = 0; i < graphicsClasses.length; i++) {
                    if (graphicsClasses[i] == CellClues.class)
                        continue;
                    int x = ind % 6;
                    ImageButton graphicButton = new ImageButton(style.GetTransform(new UITransform(-440.0 + 5 + x * 110, ofs + 55, 100, 100),
                            Style.Anchor.Center), style.GetScaled(5.0));
                    graphicButton.SetText("");
                    graphicButton.SetImage(graphicInfo[i].image);
                    graphicButton.SetRenderPriority(66);
                    style.SetButtonColors(graphicButton, 0);
                    int finalI = i;
                    graphicButton.SetAction(() -> {
                        graphicIndex.set(finalI);
                        graphicPreview.SetImage(graphicInfo[finalI].image);
                        graphicName.SetText(graphicInfo[finalI].name);
                        graphicDescription.SetText(graphicInfo[finalI].description);
                    }, 0);
                    ind++;
                    if (ind % 6 == 0)
                        ofs += 110;
                    graphicScrollWindow.AddObject(graphicButton);
                }
                graphicScrollWindow.SetObjectsHeight(ofs + 55 + 340);

                graphicWindow.AddObject(graphicScrollWindow);
                handler.AddObject(graphicScrollWindow);
            }
            //endregion

            //region Constraint Window
            {
                UIImage constraintPreview = new UIImage();
                constraintPreview.SetTransform(style.GetTransform(new UITransform(395 - 100 + 10+20, -295 + 50, 200, 200), Style.Anchor.Center));
                constraintPreview.SetRenderPriority(65);
                constraintWindow.AddObject(constraintPreview);
                handler.AddObject(constraintPreview);

                MenuLabel constraintName = new MenuLabel(style.GetPoint(new Point2D.Double(395 - 100 + 10+20, -190 + 50), Style.Anchor.Center));
                constraintName.SetFont(style.GetFont(4));
                constraintName.SetColor(style.GetColor(Style.ColorPalette.Body3.value + 1));
                constraintName.SetAlignment(MenuLabel.Alignment.Top);
                constraintName.SetRenderPriority(65);
                constraintWindow.AddObject(constraintName);
                handler.AddObject(constraintName);

                MenuLabel constraintDescription = new MenuLabel(style.GetPoint(new Point2D.Double(395 - 200 + 10+20, -190 + 50+40), Style.Anchor.Center));
                constraintDescription.SetFont(style.GetFont(5));
                constraintDescription.SetColor(style.GetColor(Style.ColorPalette.Body1.value+2));
                constraintDescription.SetAlignment(MenuLabel.Alignment.Upper_Left);
                constraintDescription.SetRenderPriority(65);
                constraintWindow.AddObject(constraintDescription);
                handler.AddObject(constraintDescription);

                MenuScrollWindow constraintScrollWindow = new MenuScrollWindow(style.GetTransform(new UITransform(-150, 0, 690, 700),
                        Style.Anchor.Center));
                constraintScrollWindow.SetPadding(style.GetScaled(5.0));
                constraintScrollWindow.SetScrollThickness(style.GetScaled(20.0));
                constraintScrollWindow.SetColors(style.GetColor(Style.ColorPalette.Body2.value - 1), style.GetColor(Style.ColorPalette.Body1.value - 2));
                MenuScroller scroller = constraintScrollWindow.GetScroller();
                scroller.SetColor(style.GetColor(Style.ColorPalette.Body2.value + 1));
                scroller.GetDrag().SetColor(style.GetColor(Style.ColorPalette.Body2.value - 1));
                constraintScrollWindow.SetRenderPriority(65);

                int ofs = -340, ind = 0;
                GraphicsManager graphicsManager = GraphicsManager.getInstance();
                Class[] constraintClasses = graphicsManager.GetConstraintGraphicsClasses();
                GraphicInfo[] constraintsInfo = graphicsManager.GetConstraintsInfo();
                constraintPreview.SetImage(constraintsInfo[0].image);
                constraintName.SetText(constraintsInfo[0].name);
                constraintDescription.SetText(constraintsInfo[0].description);
                for (int i = 0; i < constraintClasses.length; i++) {
                    int x = ind % 6;
                    ImageButton constraintButton = new ImageButton(style.GetTransform(new UITransform(-440.0 + 5 + x * 110, ofs + 55, 100, 100),
                            Style.Anchor.Center), style.GetScaled(5.0));
                    constraintButton.SetText("");
                    constraintButton.SetImage(constraintsInfo[i].image);
                    constraintButton.SetRenderPriority(66);
                    style.SetButtonColors(constraintButton, 0);
                    int finalI = i;
                    constraintButton.SetAction(() -> {
                        constraintIndex.set(finalI);
                        constraintPreview.SetImage(constraintsInfo[finalI].image);
                        constraintName.SetText(constraintsInfo[finalI].name);
                        constraintDescription.SetText(constraintsInfo[finalI].description);
                    }, 0);
                    ind++;
                    if (ind % 6 == 0)
                        ofs += 110;
                    constraintScrollWindow.AddObject(constraintButton);
                }
                constraintScrollWindow.SetObjectsHeight(ofs + 55 + 340);

                constraintWindow.AddObject(constraintScrollWindow);
                handler.AddObject(constraintScrollWindow);
            }
            //endregion

            graphicWindow.SetVisible(false);
            constraintWindow.SetVisible(false);

            MenuButton selectGraphic, selectConstraint, okButton, cancelButton;
            selectGraphic = new MenuButton(style.GetTransform(new UITransform(-430, -375, 120, 30), Style.Anchor.Center),
                    style.GetScaled(10.0));
            style.SetButtonColors(selectGraphic, 1);
            selectGraphic.SetText("Graphic");
            selectGraphic.SetTextStyle(style.GetFont(5));
            selectGraphic.SetRenderPriority(65);
            addElementWindow.AddObject(selectGraphic);
            handler.AddObject(selectGraphic);

            selectConstraint = new MenuButton(style.GetTransform(new UITransform(-305, -375, 120, 30), Style.Anchor.Center),
                    style.GetScaled(10.0));
            style.SetButtonColors(selectConstraint, 0);
            selectConstraint.SetText("Constraint");
            selectConstraint.SetTextStyle(style.GetFont(5));
            selectConstraint.SetRenderPriority(65);
            addElementWindow.AddObject(selectConstraint);
            handler.AddObject(selectConstraint);

            okButton = new MenuButton(style.GetTransform(new UITransform(310, 375, 120, 30), Style.Anchor.Center),
                    style.GetScaled(10.0));
            style.SetButtonColors(okButton, 4);
            okButton.SetText("Ok");
            okButton.SetTextStyle(style.GetFont(5));
            okButton.SetRenderPriority(65);
            addElementWindow.AddObject(okButton);
            handler.AddObject(okButton);

            cancelButton = new MenuButton(style.GetTransform(new UITransform(435, 375, 120, 30), Style.Anchor.Center),
                    style.GetScaled(10.0));
            style.SetButtonColors(cancelButton, 0);
            cancelButton.SetText("Cancel");
            cancelButton.SetTextStyle(style.GetFont(5));
            cancelButton.SetRenderPriority(65);
            addElementWindow.AddObject(cancelButton);
            handler.AddObject(cancelButton);

            addElementButton.SetAction(() -> {
                addElementWindow.SetVisible(true);
                if(selectMode.get()) {
                    constraintWindow.SetVisible(true);
                } else {
                    graphicWindow.SetVisible(true);
                }
            }, 0);
            selectGraphic.SetAction(() -> {
                selectMode.set(false);
                graphicWindow.SetVisible(true);
                constraintWindow.SetVisible(false);
                style.SetButtonColors(selectGraphic, 1);
                style.SetButtonColors(selectConstraint, 0);
            }, 0);
            selectConstraint.SetAction(() -> {
                selectMode.set(true);
                graphicWindow.SetVisible(false);
                constraintWindow.SetVisible(true);
                style.SetButtonColors(selectGraphic, 0);
                style.SetButtonColors(selectConstraint, 1);
            }, 0);
            okButton.SetAction(() -> {
                if(selectMode.get()) {
                    pres.AddConstraint(constraintIndex.get());
                } else {
                    pres.AddGraphic(graphicIndex.get());
                }
                addElementWindow.SetVisible(false);
                graphicWindow.SetVisible(false);
                constraintWindow.SetVisible(false);
            }, 0);
            cancelButton.SetAction(() -> {
                addElementWindow.SetVisible(false);
                graphicWindow.SetVisible(false);
                constraintWindow.SetVisible(false);
            }, 0);
        }
        addElementWindow.SetVisible(false);
    }

    public void Render() {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(3);
            return;
        }
        Graphics g = bs.getDrawGraphics();
        g.drawImage(background, 0, 0, null);

        handler.Render(g);
        g.dispose();
        bs.show();
    }

    public void Tick(double delta) {
        inputManager.SaveMouseState();

        for (KeyEvent event : inputManager.GetKeyInput()) {
            if (selectedField != null) {
                if (event.getID() == KeyEvent.KEY_PRESSED) {
                    selectedField.KeyPress(event);
                } else if (event.getID() == KeyEvent.KEY_TYPED) {
                    selectedField.Type(event.getKeyChar());
                }
            } else {
                if (event.getID() == KeyEvent.KEY_PRESSED) {
                    switch (event.getKeyCode()) {
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
                            pres.SelectMode(EditorWindow.NumpadMode.Digit);
                            break;
                        case KeyEvent.VK_S:
                            pres.SelectMode(EditorWindow.NumpadMode.Symbol);
                            break;
                        case KeyEvent.VK_C:
                            pres.SelectMode(EditorWindow.NumpadMode.Clue);
                            break;
                        case KeyEvent.VK_P:
                            pres.SelectMode(EditorWindow.NumpadMode.Pattern);
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
        }

        Point mousePos = inputManager.GetMousePosition();
        // cellSelector.SetTransform(new UITransform(mousePos.x, mousePos.y, 30, 30));
        int clickMask = 0;
        for (int i = 0; i < 3; i++) {
            if (inputManager.GetMouse(i + 1))
                clickMask += (1 << i);
        }

        handler.Tick(delta);
        handler.UpdateMouse(mousePos.x, mousePos.y, clickMask);
        handler.PushChange();
    }

    public void SelectInputField(MenuInputField field) {
        if (selectedField != null)
            selectedField.Deselect();
        selectedField = field;
        style.SetButtonColors(selectedField.getButton(), 1);
        selectedField = field;
    }

    public void DeselectInputField() {
        if (selectedField == null)
            return;
        style.SetButtonColors(selectedField.getButton(), 0);
        selectedField = null;
    }
    public void ClearInputField()
    {
        if(selectedField != null)
            selectedField.Deselect();
    }

    public void UpdateCluesText() {
        pres.SetClueText(cluesField.GetText());
        DeselectInputField();
    }

    private void ClickCell(int x, int y) {
        if (inputManager.IsHeld(KeyEvent.VK_CONTROL)) {
            pres.ToggleCell(x, y);
        } else {
            pres.SelectCell(x, y);
        }
    }

    public void ConstructBoard() {
        if (cells != null) {
            for (int i = 0; i < cells.length; i++) {
                for (int j = 0; j < cells[i].length; j++) {
                    handler.RemoveObject(cells[i][j]);
                }
            }
        }
        Board board = pres.GetBoard();
        dimensionX = board.GetWidth();
        dimensionY = board.GetHeight();
        cells = new UICell[dimensionX][dimensionY];
        cellClues = new UICellClue[dimensionX][dimensionY];
        double size =  1080 * 0.7 / Math.max(dimensionX, dimensionY), shiftX = 0, shiftY = 120;

        boardPosX = 1920 / 2.0 - dimensionX / 2.0 * size + shiftX;
        boardPosY = 1080 / 2.0 - dimensionY / 2.0 * size + shiftY;
        cellSize = size;

        for (int i = 0; i < dimensionX; i++) {
            double posX = 0;
            if (dimensionX > 1)
                posX = i - (dimensionX - 1) / 2.0;
            for (int j = 0; j < dimensionY; j++) {
                double posY = 0;
                if (dimensionY > 1)
                    posY = j - (dimensionY - 1) / 2.0;

                cells[i][j] = new UICell(style.GetTransform(new UITransform(posX * size + shiftX, posY * size + shiftY, size, size), Style.Anchor.Center));
                cells[i][j].SetFont(style.GetFont(2));
                cells[i][j].SetRenderPriority(0);
                cells[i][j].SetSelectionWidth(style.GetScaled(10));
                cells[i][j].SetSelectionColor(style.GetColor(Style.ColorPalette.Accent1.value));
                cells[i][j].SetColors(new Color[]{new Color(253, 253, 253, 0)});
                handler.AddObject(cells[i][j]);

                cellClues[i][j] = new UICellClue(style.GetTransform(new UITransform(posX * size + shiftX, posY * size + shiftY, size, size), Style.Anchor.Center));
                cellClues[i][j].SetFont(style.GetFont(2));
                cellClues[i][j].SetRenderPriority(-1);
                handler.AddObject(cellClues[i][j]);

                int finalI = i;
                int finalJ = j;
                cells[i][j].SetAction(() -> ClickCell(finalI, finalJ));
            }
        }
    }

    public void CreateGraphics(ArrayList<Constraint> constraints, ArrayList<Graphic> graphics) {
        double scrollerPosition = 0;
        if(scrollWindow != null)
            scrollerPosition = scrollWindow.GetScroller().GetValue();
        if(this.constraints != null) {
            for(RenderObject ro : this.constraints) {
                handler.RemoveObject(ro);
            }
        }
        if(this.graphics != null) {
            for(RenderObject ro : this.graphics ) {
                if(ro instanceof UICellClue)
                    continue;
                handler.RemoveObject(ro);
            }
        }
        this.constraints = new ArrayList<>();
        this.graphics = new ArrayList<>();
        graphicButtons = new ArrayList<>();
        constraintButtons = new ArrayList<>();

        scrollWindow = new MenuScrollWindow(style.GetTransform(new UITransform(240, 0, 450, 800), Style.Anchor.Left));
        scrollWindow.SetScrollThickness(style.GetScaled(20.0));
        scrollWindow.SetColors(style.GetColor(Style.ColorPalette.Body2.value-1), style.GetColor(Style.ColorPalette.Body1.value-2));
        MenuScroller scroller = scrollWindow.GetScroller();
        scroller.SetColor(style.GetColor(Style.ColorPalette.Body2.value+1));
        scroller.GetDrag().SetColor(style.GetColor(Style.ColorPalette.Body2.value-1));
        int ofs = 0;
        for (int i = 0; i < constraints.size(); i++)
        {
            Constraint constraint = constraints.get(i);
            UIConstraint ui = constraint.Convert(this);
            constraint.GenerateUI(this);
            ui.SetIdle();
            this.constraints.add((RenderObject) ui);
            handler.AddObject((RenderObject) ui);
            MenuConstraint constraintButton = style.GetConstraintButton(constraint.GetName());
            int finalI = i;
            constraintButton.GetEditButton().SetAction(() -> pres.SelectConstraint(finalI), 0);
            constraintButton.GetToggleButton().SetAction(() -> pres.ToggleConstraint(finalI), 0);
            constraintButton.GetDeleteButton().SetAction(() -> pres.DeleteConstraint(finalI), 0);
            constraintButton.SetPos(style.GetTransform(new UITransform(225, -370 + ofs, 400, 35), Style.Anchor.Left));
            ofs += 40;
            constraintButtons.add(constraintButton);
            scrollWindow.AddObject(constraintButton);
        }
        for (int i = 0; i < graphics.size(); i++) {
            Graphic graphic = graphics.get(i);
            RenderObject ro = graphic.Convert(this);
            graphic.GenerateUI(this);
            if(!(graphic instanceof CellClues)) {
                this.graphics.add(ro);
                handler.AddObject(ro);
                MenuGraphic graphicButton = style.GetGraphicButton(graphic.GetName());
                int finalI = i;
                System.out.println(finalI);
                graphicButton.GetEditButton().SetAction(() -> pres.SelectGraphics(finalI), 0);
                graphicButton.GetToggleButton().SetAction(() -> pres.ToggleGraphic(finalI), 0);
                graphicButton.GetDeleteButton().SetAction(() -> pres.DeleteGraphic(finalI), 0);
                graphicButton.SetPos(style.GetTransform(new UITransform(225, -370 + ofs, 400, 35), Style.Anchor.Left));
                ofs += 40;
                graphicButtons.add(graphicButton);
                scrollWindow.AddObject(graphicButton);
            }
            else {
                CellClues clue = (CellClues)graphic;
                this.graphics.add(cellClues[clue.x][clue.y]);
                graphicButtons.add(null);
            }
        }
        scrollWindow.SetObjectsHeight(style.GetScaled(ofs));
        scrollWindow.SetOffset(scrollerPosition);
        handler.AddObject(scrollWindow);
    }
    public void UpdateGraphics(ArrayList<Graphic> graphics)
    {
        for(int i = 0; i < graphics.size(); i++)
        {
            graphics.get(i).Convert(this, this.graphics.get(i));
        }
    }
    public void UpdateGraphic(Graphic graphic, int index)
    {
        graphic.Convert(this, this.graphics.get(index));
    }
    public void UpdateConstraint(Constraint constraint, int index)
    {
        constraint.Convert(this, this.constraints.get(index));
    }
    public void ToggleGraphic(int index, boolean state)
    {
        style.SetButtonColors(graphicButtons.get(index).GetToggleButton(), (state) ? 0 : 1);
        graphics.get(index).SetVisibility(state);
    }
    public void SelectGraphic(int index)
    {
        graphicButtons.get(index).SetColor(style.GetColor(Style.ColorPalette.Body2.value));
    }
    public void DeselectGraphic(int index)
    {
        graphicButtons.get(index).SetColor(style.GetColor(Style.ColorPalette.Body2.value-1));
    }
    public void SelectConstraint(int index) {
        constraintButtons.get(index).SetColor(style.GetColor(Style.ColorPalette.Body3.value));
    }
    public void DeselectConstraint(int index) {
        constraintButtons.get(index).SetColor(style.GetColor(Style.ColorPalette.Body3.value-1));
    }
    public void ToggleConstraint(int index, boolean state) {
        style.SetButtonColors(constraintButtons.get(index).GetToggleButton(), (state) ? 2 : 3);
        constraints.get(index).SetVisibility(state);
    }
    public BufferedImage RenderBoard()
    {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        ArrayList<RenderObject> graphics = new ArrayList<>();
        graphics.addAll(this.graphics);
        graphics.addAll(this.constraints);
        graphics.sort(Comparator.naturalOrder());

        boolean renderedBoard = false;

        for(RenderObject ro : graphics) {
            if (ro.GetRenderPriority() > 0) {
                if (!renderedBoard) {
                    for(int i = 0; i < dimensionX; i++) {
                        for(int j = 0; j < dimensionY; j++) {
                            if(cells[i][j].GetVisibility())
                                cells[i][j].Render(g);
                        }
                    }
                    renderedBoard = true;
                }
            }
            if(ro.GetVisibility())
                ro.Render(g);
        }
        if (!renderedBoard) {
            for(int i = 0; i < dimensionX; i++) {
                for(int j = 0; j < dimensionY; j++) {
                    if(cells[i][j].GetVisibility())
                        cells[i][j].Render(g);
                }
            }
        }
        g.dispose();

        return image;
    }
    public UICell GetCell(int x, int y)
    {
        return cells[x][y];
    }
    public void SetCellClue(int x, int y, CellClues cellClues)
    {
        cellClues.UpdateUI(this, this.cellClues[x][y]);
    }
    public void SetSymbol(int index, int symbol)
    {
        Shape shape = style.GetShape(symbol, numpadSize);
        symbolButtons[index].SetShape(shape);
    }
    public void SetSymbolLabel(String text)
    {
        symbolsLabel.SetText(text);
    }
    public void SetPattern(int index, int pattern)
    {
        Shape shape = style.GetPattern(pattern, numpadSize*0.9);
        patternButtons[index].SetShape(shape);
    }
    public void SetPatternLabel(String text)
    {
        patternsLabel.SetText(text);
    }
    public void DeselectCells()
    {
        for(int x = 0; x < dimensionX; x++)
        {
            for(int y = 0; y < dimensionY; y++)
            {
                cells[x][y].SetSelect(false);
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
    public void SelectNumpad(int digit, boolean immediate)
    {
        for(int i = 0; i < 10; i++)
        {
            style.SetButtonColors(digitButtons[i], 0);
        }
        if(digit > 0)
            style.SetButtonColors(digitButtons[digit-1], 1);
        if(immediate)
            for(int i = 0; i < 10; i++)
                digitButtons[i].ResetColors();
    }
    public void SelectMode(int mode)
    {
        for(int i = 0; i < 4; i++) {
            numpadPanels[i].SetVisible(false);
            style.SetButtonColors(modeButtons[i], 2);
        }
        if(mode < 0) {
            for(int i = 0; i < 4; i++)
            {
                modeButtons[i].SetVisibility(false);
            }
            turnOffCell.SetVisibility(false);
            return;
        }
        turnOffCell.SetVisibility(true );
        for(int i = 0; i < 4; i++)
        {
            modeButtons[i].SetVisibility(true);
        }
        numpadPanels[mode].SetVisible(true);
        style.SetButtonColors(modeButtons[mode], 3);
    }
    public void SetClueText(String text)
    {
        cluesField.SetText(text);
    }
    public void SetClueIndex(int index)
    {
        for(int i = 0; i < 4; i++)
        {
            style.SetButtonColors(cluesButtons[i], 0);
        }
        style.SetButtonColors(cluesButtons[index], 1);
    }
    public void SelectTurnOff(boolean state)
    {
        if(state) {
            style.SetButtonColors(turnOffCell, 2);
        } else {
            style.SetButtonColors(turnOffCell, 3);
        }
    }
    public void SelectSettings() {
        settingsGroup.SetVisible(true);
        style.SetButtonColors(settingsButton, 1);
    }
    public void DeselectSettings() {
        settingsGroup.SetVisible(false);
        style.SetButtonColors(settingsButton, 0);
    }
    public void SetSettingsGroup(MenuGroup group)
    {
        settingsGroup = group;
    }

    public Point2D.Double GetNodePos(int x, int y) {
        return new Point2D.Double(boardPosX + cellSize*x, boardPosY + cellSize*y);
    }
    public Point2D.Double GetCellPos(int x, int y) {
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


}
