package main.frames;

import main.FileLoader;
import main.ObjectsHandler;
import main.ParameterHolder;
import main.Window;
import main.ui.*;
import main.ui.graphics.UIImage;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class MainMenuView extends Canvas implements MenuView
{
    private MainMenu pres;
    private Window window;
    private ObjectsHandler handler;
    private InputManager inputManager;
    private int width, height;

    private FileLoader loader;
    private Style style;
    private MenuButton playButton, createButton, aboutButton, exitButton;
    private Image background;

    private BufferedImage emptyFileImage, noPreview, noSelected;

    private MenuGroup playWindow, createWindow, fromFileWindow, fromPresetWindow, newGridWindow;
    private UIImage playSudokuRender, editSudokuRender;
    private MenuLabel playSudokuName, playSudokuAuthor, playSudokuDescription,
            editSudokuName, editSudokuAuthor, editSudokuDescription;
    private MenuButton[] presetButtons;

    private MenuInputField selectedField;

    public MainMenuView(MainMenu pres, Window window, int width, int height)
    {
        this.pres = pres;
        handler = new ObjectsHandler();

        this.width = width;
        this.height = height;
        this.window = window;

        loader = FileLoader.getInstance();
        inputManager = new InputManager(window, this);
        background = loader.ReadImage("background.jpg").getScaledInstance(width, height, 0);
        emptyFileImage = loader.ReadImage("empty_file.png");
        noPreview = loader.ReadImage("no_preview.png");
        noSelected = loader.ReadImage("no_selected.png");
        style = Style.getInstance();
        style.SetHandler(handler);

        style.SetSize(width, height);
        style.LoadPalette(loader);

        //TODO Set scaled roundness

        playButton = new MenuButton(style.GetTransform(new UITransform(200, 500, 300, 100), Style.Anchor.Upper_Left), 20);
        style.SetButtonColors(playButton, 4);
        playButton.ResetColors();
        playButton.SetText("Play");
        playButton.SetTextStyle(style.GetFont(0));
        playButton.SetAction(() -> pres.PlayWindow(true), 0);
        handler.AddObject(playButton);

        createButton = new MenuButton(style.GetTransform(new UITransform(200, 620, 300, 100), Style.Anchor.Upper_Left), 20);
        style.SetButtonColors(createButton, 0);
        createButton.SetText("Create");
        createButton.SetTextStyle(style.GetFont(0));
        createButton.SetAction(() -> pres.EditorWindow(true), 0);
        handler.AddObject(createButton);

        aboutButton = new MenuButton(style.GetTransform(new UITransform(200, 740, 300, 100), Style.Anchor.Upper_Left), 20);
        style.SetButtonColors(aboutButton, 0);
        aboutButton.SetText("About");
        aboutButton.SetTextStyle(style.GetFont(0));
        aboutButton.SetAction(() -> pres.About(), 0);
        handler.AddObject(aboutButton);

        exitButton = new MenuButton(style.GetTransform(new UITransform(200, 860, 300, 100), Style.Anchor.Upper_Left), 20);
        style.SetButtonColors(exitButton, 1);
        exitButton.SetText("Exit");
        exitButton.SetTextStyle(style.GetFont(1));
        exitButton.SetAction(() -> pres.Exit(), 0);
        handler.AddObject(exitButton);

        // Play Window
        {
            playWindow = new MenuGroup();

            MenuPanel windowPanel = new MenuPanel(style.GetTransform(new UITransform(0, 0, 1000, 600), Style.Anchor.Center));
            windowPanel.SetScreenSize(width, height);
            windowPanel.SetRoundness(style.GetScaled(20));
            windowPanel.SetColor(style.GetColor(Style.ColorPalette.Body1.value - 1));
            windowPanel.SetPanel(true);
            windowPanel.SetRenderPriority(1);
            handler.AddObject(windowPanel);
            playWindow.AddObject(windowPanel);

            MenuButton openButton = new MenuButton(style.GetTransform(new UITransform(-430, 270, 120, 40), Style.Anchor.Center),
                    style.GetScaled(10.0));
            style.SetButtonColors(openButton, 1);
            openButton.SetText("Open");
            openButton.SetTextStyle(style.GetFont(4));
            openButton.SetAction(() -> pres.OpenPlayFile(), 0);
            openButton.SetRenderPriority(2);
            handler.AddObject(openButton);
            playWindow.AddObject(openButton);

            MenuButton playButton = new MenuButton(style.GetTransform(new UITransform(-305, 270, 120, 40), Style.Anchor.Center),
                    style.GetScaled(10.0));
            style.SetButtonColors(playButton, 1);
            playButton.SetText("Play");
            playButton.SetTextStyle(style.GetFont(4));
            playButton.SetAction(() -> pres.Play(), 0);
            playButton.SetRenderPriority(2);
            handler.AddObject(playButton);
            playWindow.AddObject(playButton);

            MenuButton closeButton = new MenuButton(style.GetTransform(new UITransform(430, 270, 120, 40), Style.Anchor.Center),
                    style.GetScaled(10.0));
            style.SetButtonColors(closeButton, 1);
            closeButton.SetText("Close");
            closeButton.SetTextStyle(style.GetFont(4));
            closeButton.SetAction(() -> pres.PlayWindow(false), 0);
            closeButton.SetRenderPriority(2);
            handler.AddObject(closeButton);
            playWindow.AddObject(closeButton);

            playSudokuRender = new UIImage();
            playSudokuRender.SetTransform(style.GetTransform(new UITransform(-240, 0, 490, 490), Style.Anchor.Center));
            playSudokuRender.SetImage(noSelected);
            playSudokuRender.SetRenderPriority(2);
            handler.AddObject(playSudokuRender);
            playWindow.AddObject(playSudokuRender);

            playSudokuName = new MenuLabel(style.GetPoint(new Point2D.Double(10, -250), Style.Anchor.Center));
            playSudokuName.SetAlignment(MenuLabel.Alignment.Upper_Left);
            playSudokuName.SetFont(style.GetFont(7));
            playSudokuName.SetText("Open a .saf file");
            playSudokuName.SetRenderPriority(2);
            playSudokuName.SetColor(style.GetColor(Style.ColorPalette.Body3.value+2));
            handler.AddObject(playSudokuName);
            playWindow.AddObject(playSudokuName);

            playSudokuAuthor = new MenuLabel(style.GetPoint(new Point2D.Double(10, -200), Style.Anchor.Center));
            playSudokuAuthor.SetAlignment(MenuLabel.Alignment.Upper_Left);
            playSudokuAuthor.SetFont(style.GetFont(8));
            playSudokuAuthor.SetText("");
            playSudokuAuthor.SetRenderPriority(2);
            playSudokuAuthor.SetColor(style.GetColor(Style.ColorPalette.Body3.value+1));
            handler.AddObject(playSudokuAuthor);
            playWindow.AddObject(playSudokuAuthor);

            playSudokuDescription = new MenuLabel(style.GetPoint(new Point2D.Double(10, -160), Style.Anchor.Center));
            playSudokuDescription.SetAlignment(MenuLabel.Alignment.Upper_Left);
            playSudokuDescription.SetFont(style.GetFont(5));
            playSudokuDescription.SetText("");
            playSudokuDescription.SetRenderPriority(2);
            playSudokuDescription.SetColor(style.GetColor(Style.ColorPalette.Body1.value+2));
            handler.AddObject(playSudokuDescription);
            playWindow.AddObject(playSudokuDescription);
        }
        playWindow.SetVisible(false);

        // Editor Window
        {
            createWindow = new MenuGroup();
            fromFileWindow = new MenuGroup();
            fromPresetWindow = new MenuGroup();
            newGridWindow = new MenuGroup();

            MenuPanel windowPanel = new MenuPanel(style.GetTransform(new UITransform(0, 0, 1000, 600), Style.Anchor.Center));
            windowPanel.SetScreenSize(width, height);
            windowPanel.SetRoundness(style.GetScaled(20));
            windowPanel.SetColor(style.GetColor(Style.ColorPalette.Body1.value - 1));
            windowPanel.SetPanel(true);
            windowPanel.SetRenderPriority(1);
            handler.AddObject(windowPanel);
            createWindow.AddObject(windowPanel);

            MenuButton fromButton = new MenuButton(style.GetTransform(new UITransform(-430, 270, 120, 40), Style.Anchor.Center),
                    style.GetScaled(10.0));
            style.SetButtonColors(fromButton, 1);
            fromButton.SetText("From File");
            fromButton.SetTextStyle(style.GetFont(4));
            fromButton.SetAction(() -> pres.FromFile(), 0);
            fromButton.SetRenderPriority(2);
            handler.AddObject(fromButton);
            createWindow.AddObject(fromButton);

            MenuButton presetButton = new MenuButton(style.GetTransform(new UITransform(-290, 270, 150, 40), Style.Anchor.Center),
                    style.GetScaled(10.0));
            style.SetButtonColors(presetButton, 1);
            presetButton.SetText("From Preset");
            presetButton.SetTextStyle(style.GetFont(4));
            presetButton.SetAction(() -> pres.FromPreset(), 0);
            presetButton.SetRenderPriority(2);
            handler.AddObject(presetButton);
            createWindow.AddObject(presetButton);

            MenuButton newButton = new MenuButton(style.GetTransform(new UITransform(-135, 270, 150, 40), Style.Anchor.Center),
                    style.GetScaled(10.0));
            style.SetButtonColors(newButton, 1);
            newButton.SetText("New Board");
            newButton.SetTextStyle(style.GetFont(4));
            newButton.SetAction(() -> pres.NewBoard(), 0);
            newButton.SetRenderPriority(2);
            handler.AddObject(newButton);
            createWindow.AddObject(newButton);

            MenuButton editButton = new MenuButton(style.GetTransform(new UITransform(305, 270, 120, 40), Style.Anchor.Center),
                    style.GetScaled(10.0));
            style.SetButtonColors(editButton, 1);
            editButton.SetText("Ok");
            editButton.SetTextStyle(style.GetFont(4));
            editButton.SetAction(() -> pres.Editor(), 0);
            editButton.SetRenderPriority(2);
            handler.AddObject(editButton);
            createWindow.AddObject(editButton);

            MenuButton closeButton = new MenuButton(style.GetTransform(new UITransform(430, 270, 120, 40), Style.Anchor.Center),
                    style.GetScaled(10.0));
            style.SetButtonColors(closeButton, 1);
            closeButton.SetText("Close");
            closeButton.SetTextStyle(style.GetFont(4));
            closeButton.SetAction(() -> pres.EditorWindow(false), 0);
            closeButton.SetRenderPriority(2);
            handler.AddObject(closeButton);
            createWindow.AddObject(closeButton);

            //region From File
            MenuButton openFileButton = new MenuButton(style.GetTransform(new UITransform(-430, 225, 120, 40), Style.Anchor.Center),
                style.GetScaled(10.0));
            style.SetButtonColors(openFileButton, 1);
            openFileButton.SetText("Select");
            openFileButton.SetTextStyle(style.GetFont(4));
            openFileButton.SetAction(() -> pres.OpenEditorFile(), 0);
            openFileButton.SetRenderPriority(2);
            handler.AddObject(openFileButton);
            fromFileWindow.AddObject(openFileButton);

            editSudokuRender = new UIImage();
            editSudokuRender.SetTransform(style.GetTransform(new UITransform(-240, -25, 490, 445), Style.Anchor.Center));
            editSudokuRender.SetImage(noSelected);
            editSudokuRender.SetRenderPriority(2);
            handler.AddObject(editSudokuRender);
            fromFileWindow.AddObject(editSudokuRender);

            editSudokuName = new MenuLabel(style.GetPoint(new Point2D.Double(10, -250), Style.Anchor.Center));
            editSudokuName.SetAlignment(MenuLabel.Alignment.Upper_Left);
            editSudokuName.SetFont(style.GetFont(7));
            editSudokuName.SetText("Open a .saf file");
            editSudokuName.SetRenderPriority(2);
            editSudokuName.SetColor(style.GetColor(Style.ColorPalette.Body3.value+2));
            handler.AddObject(editSudokuName);
            fromFileWindow.AddObject(editSudokuName);

            editSudokuAuthor = new MenuLabel(style.GetPoint(new Point2D.Double(10, -200), Style.Anchor.Center));
            editSudokuAuthor.SetAlignment(MenuLabel.Alignment.Upper_Left);
            editSudokuAuthor.SetFont(style.GetFont(8));
            editSudokuAuthor.SetText("");
            editSudokuAuthor.SetRenderPriority(2);
            editSudokuAuthor.SetColor(style.GetColor(Style.ColorPalette.Body3.value+1));
            handler.AddObject(editSudokuAuthor);
            fromFileWindow.AddObject(editSudokuAuthor);

            editSudokuDescription = new MenuLabel(style.GetPoint(new Point2D.Double(10, -160), Style.Anchor.Center));
            editSudokuDescription.SetAlignment(MenuLabel.Alignment.Upper_Left);
            editSudokuDescription.SetFont(style.GetFont(5));
            editSudokuDescription.SetText("");
            editSudokuDescription.SetRenderPriority(2);
            editSudokuDescription.SetColor(style.GetColor(Style.ColorPalette.Body1.value+2));
            handler.AddObject(editSudokuDescription);
            fromFileWindow.AddObject(editSudokuDescription);
            //endregion
            //region From Preset
            String[] presetPaths = loader.GetSudokuPresets();
            if(presetPaths != null) {
                presetButtons = new MenuButton[presetPaths.length];
                int y = 0;
                for (int i = 0; i < presetPaths.length; i++) {
                    MenuButton presetSelect = new MenuButton(style.GetTransform(new UITransform(-410.0 + 400 * y, -210.0 + 160 * (i % 3), 150, 150),
                            Style.Anchor.Center), style.GetScaled(10.0));
                    presetSelect.SetText("");
                    style.SetButtonColors(presetSelect, 0);
                    presetSelect.SetRenderPriority(2);
                    int finalI = i;
                    presetSelect.SetAction(() -> {
                        pres.SelectPreset(presetPaths[finalI]);
                        selectPreset(finalI);
                    }, 0);
                    fromPresetWindow.AddObject(presetSelect);
                    handler.AddObject(presetSelect);
                    presetButtons[i] = presetSelect;

                    UIImage presetImage = new UIImage();
                    presetImage.SetTransform(style.GetTransform(new UITransform(-410.0 + 400 * y, -210.0 + 160 * (i % 3), 140, 140),
                            Style.Anchor.Center));
                    presetImage.SetRenderPriority(2);
                    fromPresetWindow.AddObject(presetImage);
                    handler.AddObject(presetImage);

                    MenuLabel presetLabel = new MenuLabel(style.GetPoint(new Point2D.Double(-325.0 + 400 * y, -290.0 + 160 * (i % 3)), Style.Anchor.Center));
                    presetLabel.SetAlignment(MenuLabel.Alignment.Upper_Left);
                    presetLabel.SetColor(style.GetColor(Style.ColorPalette.Body3.value + 2));
                    presetLabel.SetFont(style.GetFont(7));
                    presetLabel.SetRenderPriority(2);
                    fromPresetWindow.AddObject(presetLabel);
                    handler.AddObject(presetLabel);

                    MenuLabel presetDescription = new MenuLabel(style.GetPoint(new Point2D.Double(-325.0 + 400 * y, -290.0+40 + 160 * (i % 3)), Style.Anchor.Center));
                    presetDescription.SetAlignment(MenuLabel.Alignment.Upper_Left);
                    presetDescription.SetColor(style.GetColor(Style.ColorPalette.Body1.value + 2));
                    presetDescription.SetFont(style.GetFont(5));
                    presetDescription.SetRenderPriority(2);
                    fromPresetWindow.AddObject(presetDescription);
                    handler.AddObject(presetDescription);

                    pres.GetPreset(presetImage, presetLabel, presetDescription, presetPaths[i]);

                    if (i % 3 == 2)
                        y++;
                }
            }
            //endregion
            //region New Board
            style.SetParametersOffset(-500+10, -100);
            style.SetParametersAnchor(Style.Anchor.Center);
            style.StartParameters();
            ParameterHolder<Integer> boardWidth = pres.GetBoardDimension(false), boardHeight = pres.GetBoardDimension(true);
            style.GetIntegerParameter(this, boardWidth, "Board Width", 1, 25, 9);
            style.GetIntegerParameter(this, boardHeight, "Board Height", 1, 25, 9);
            newGridWindow = style.GetParameterWindow();
            newGridWindow.SetRenderPriority(2);
            //endregion
        }
        createWindow.SetVisible(false);
        fromFileWindow.SetVisible(false);
        fromPresetWindow.SetVisible(false);
        newGridWindow.SetVisible(false);
    }

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
        ArrayList<KeyEvent> events = inputManager.GetKeyInput();

        inputManager.SaveMouseState();

        for(KeyEvent event : events)
        {
            if (selectedField != null) {
                if (event.getID() == KeyEvent.KEY_PRESSED) {
                    selectedField.KeyPress(event);
                } else if (event.getID() == KeyEvent.KEY_TYPED) {
                    selectedField.Type(event.getKeyChar());
                }
            }
        }

        Point mousePos = inputManager.GetMousePosition();
        int clickMask = 0;
        for(int i = 0; i < 3; i++)
        {
            if(inputManager.GetMouse(i+1))
                clickMask += (1 << i);
        }

        handler.Tick(delta);
        handler.UpdateMouse(mousePos.x, mousePos.y, clickMask);
        handler.PushChange();
    }

    public void PlayWindow(boolean value)
    {
        playWindow.SetVisible(value);
    }
    public void EditWindow(boolean value, int num)
    {
        createWindow.SetVisible(value);
        if(!value)
        {
            fromFileWindow.SetVisible(false);
            fromPresetWindow.SetVisible(false);
            newGridWindow.SetVisible(false);
        }
        else
        {
            switch (num){
                case 0:
                    fromFileWindow.SetVisible(true);
                    break;
                case 1:
                    fromPresetWindow.SetVisible(true);
                    break;
                case 2:
                    newGridWindow.SetVisible(true);
                    break;
            }
        }
    }
    public void EditPanel(int prev, int next)
    {
        switch (prev){
            case 0:
                fromFileWindow.SetVisible(false);
                break;
            case 1:
                fromPresetWindow.SetVisible(false);
                break;
            case 2:
                newGridWindow.SetVisible(false);
                break;
        }
        switch (next){
            case 0:
                fromFileWindow.SetVisible(true);
                break;
            case 1:
                fromPresetWindow.SetVisible(true);
                break;
            case 2:
                newGridWindow.SetVisible(true);
                break;
        }
    }
    private void selectPreset(int ind)
    {
        for(int i = 0; i < presetButtons.length; i++)
        {
            style.SetButtonColors(presetButtons[i], 0);
        }
        style.SetButtonColors(presetButtons[ind], 1);
    }

    public void PlayEmptyFile()
    {
        playSudokuRender.SetImage(emptyFileImage);
        playSudokuName.SetText("Corrupted File");
        playSudokuAuthor.SetText("");
        playSudokuDescription.SetText("This file is\nnot a proper\nsudoku file.\nplease select\na proper file.");
    }

    public void PlayFilePreview(String name, String author, String description, BufferedImage preview)
    {
        if(preview == null)
            playSudokuRender.SetImage(noPreview);
        else
            playSudokuRender.SetImage(preview);
        if(name == "" || name == null)
            playSudokuName.SetText("Unknown Sudoku");
        else
            playSudokuName.SetText(name);
        if(author == "" || author == null)
            playSudokuAuthor.SetText("Unknown Author");
        else
            playSudokuAuthor.SetText("by: " + author);
        if(description == "" || description == null)
            playSudokuDescription.SetText("No description.");
        else
            playSudokuDescription.SetText(description);
    }

    public void EditEmptyFile() {
        editSudokuRender.SetImage(emptyFileImage);
        editSudokuName.SetText("Corrupted File");
        editSudokuAuthor.SetText("");
        editSudokuDescription.SetText("This file is\nnot a proper\nsudoku file.\nplease select\na proper file.");
    }

    public void EditFilePreview(String name, String author, String description, BufferedImage preview)
    {
        if(preview == null)
            editSudokuRender.SetImage(noPreview);
        else
            editSudokuRender.SetImage(preview);
        if(name == "" || name == null)
            editSudokuName.SetText("Unknown Sudoku");
        else
            editSudokuName.SetText(name);
        if(author == "" || author == null)
            editSudokuAuthor.SetText("Unknown Author");
        else
            editSudokuAuthor.SetText("by: " + author);
        if(description == "" || description == null)
            editSudokuDescription.SetText("No description.");
        else
            editSudokuDescription.SetText(description);
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
}
