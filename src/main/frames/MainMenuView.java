package main.frames;

import main.*;
import main.Window;
import main.ui.*;
import main.ui.graphics.UIImage;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

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

    private MenuButton settingsButton;
    private MenuGroup settingsWindow;
    private UserSettings settings;
    private int settingsWidth, settingsHeight, settingsPalette;
    private boolean settingsFullScreen, settingsAntialias;

    private boolean renderingAntialias = true;

    private MenuInputField selectedField;
    private int maxValue = 4;

    public MainMenuView(MainMenu pres, Window window, int width, int height)
    {
        this(pres, window, width, height, false);
    }

    public MainMenuView(MainMenu pres, Window window, int width, int height, boolean showSettings)
    {
        this.pres = pres;
        handler = new ObjectsHandler();

        this.width = width;
        this.height = height;
        this.window = window;

        loader = FileLoader.getInstance();
        settings = UserSettings.getInstance();
        settingsWidth = settings.GetScreenWidth();
        settingsHeight = settings.GetScreenHeight();
        settingsFullScreen = settings.GetFullscreen();
        settingsAntialias = renderingAntialias = settings.GetAntialias();
        settingsPalette = settings.GetColorPalette();
        inputManager = new InputManager(window, this);
        background = loader.LoadBackground().getScaledInstance(width, height, 0);
        emptyFileImage = loader.ReadImage("empty_file.png");
        noPreview = loader.ReadImage("no_preview.png");
        noSelected = loader.ReadImage("no_selected.png");
        style = Style.getInstance();
        style.SetHandler(handler);

        style.SetSize(width, height);
        style.LoadPalette(loader);

        playButton = new MenuButton(style.GetTransform(new UITransform(200, -180, 300, 80), Style.Anchor.Left),
                style.GetScaled(20.0));
        style.SetButtonColors(playButton, 4);
        playButton.ResetColors();
        playButton.SetText("Play");
        playButton.SetTextStyle(style.GetFont(0));
        playButton.SetBetterDraw(true);
        playButton.SetAction(() -> pres.PlayWindow(true), 0);
        handler.AddObject(playButton);

        createButton = new MenuButton(style.GetTransform(new UITransform(200, -90, 300, 80), Style.Anchor.Left),
                style.GetScaled(20.0));
        style.SetButtonColors(createButton, 0);
        createButton.SetText("Create");
        createButton.SetTextStyle(style.GetFont(0));
        createButton.SetBetterDraw(true);
        createButton.SetAction(() -> pres.EditorWindow(true), 0);
        handler.AddObject(createButton);

        aboutButton = new MenuButton(style.GetTransform(new UITransform(200, 0, 300, 80), Style.Anchor.Left), 20);
        style.SetButtonColors(aboutButton, 0);
        aboutButton.SetText("About");
        aboutButton.SetTextStyle(style.GetFont(0));
        aboutButton.SetBetterDraw(true);
        aboutButton.SetAction(() -> pres.About(), 0);
        handler.AddObject(aboutButton);

        settingsButton = new MenuButton(style.GetTransform(new UITransform(200, 90, 300, 80), Style.Anchor.Left),
                style.GetScaled(20.0));
        style.SetButtonColors(settingsButton, 0);
        settingsButton.SetText("Settings");
        settingsButton.SetTextStyle(style.GetFont(0));
        settingsButton.SetBetterDraw(true);
        settingsButton.SetAction(() -> settingsWindow.SetVisible(true), 0);
        handler.AddObject(settingsButton);

        exitButton = new MenuButton(style.GetTransform(new UITransform(200, 180, 300, 80), Style.Anchor.Left),
                style.GetScaled(20.0));
        style.SetButtonColors(exitButton, 1);
        exitButton.SetBetterDraw(true);
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

            MenuLabel playingLabel = new MenuLabel(style.GetPoint(new Point2D.Double(0, -300), Style.Anchor.Center));
            playingLabel.SetRenderPriority(3);
            playingLabel.SetAlignment(MenuLabel.Alignment.Top);
            playingLabel.SetFont(style.GetFont(7));
            playingLabel.SetText("Play");
            playingLabel.SetColor(style.GetColor(Style.ColorPalette.Body1.value+2));
            playWindow.AddObject(playingLabel);
            handler.AddObject(playingLabel);

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
            style.SetButtonColors(playButton, 4);
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

            MenuLabel editLabel = new MenuLabel(style.GetPoint(new Point2D.Double(0, -300), Style.Anchor.Center));
            editLabel.SetRenderPriority(3);
            editLabel.SetAlignment(MenuLabel.Alignment.Top);
            editLabel.SetFont(style.GetFont(7));
            editLabel.SetText("Editor");
            editLabel.SetColor(style.GetColor(Style.ColorPalette.Body1.value+2));
            createWindow.AddObject(editLabel);
            handler.AddObject(editLabel);

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
            style.SetButtonColors(editButton, 4);
            editButton.SetText("Ok");
            editButton.SetTextStyle(style.GetFont(4));
            editButton.SetAction(() -> pres.Editor(), 0);
            editButton.SetRenderPriority(3);
            handler.AddObject(editButton);
            createWindow.AddObject(editButton);

            MenuButton closeButton = new MenuButton(style.GetTransform(new UITransform(430, 270, 120, 40), Style.Anchor.Center),
                    style.GetScaled(10.0));
            style.SetButtonColors(closeButton, 1);
            closeButton.SetText("Close");
            closeButton.SetTextStyle(style.GetFont(4));
            closeButton.SetAction(() -> pres.EditorWindow(false), 0);
            closeButton.SetRenderPriority(3);
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
                    MenuButton presetSelect = new MenuButton(style.GetTransform(new UITransform(-410.0 + 475 * y, -210.0+40 + 160 * (i % 3), 150, 150),
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
                    presetImage.SetTransform(style.GetTransform(new UITransform(-410.0 + 475 * y, -210.0+40 + 160 * (i % 3), 140, 140),
                            Style.Anchor.Center));
                    presetImage.SetRenderPriority(2);
                    fromPresetWindow.AddObject(presetImage);
                    handler.AddObject(presetImage);

                    MenuLabel presetLabel = new MenuLabel(style.GetPoint(new Point2D.Double(-325.0 + 475 * y, -290.0+40 + 160 * (i % 3)), Style.Anchor.Center));
                    presetLabel.SetAlignment(MenuLabel.Alignment.Upper_Left);
                    presetLabel.SetColor(style.GetColor(Style.ColorPalette.Body3.value + 2));
                    presetLabel.SetFont(style.GetFont(7));
                    presetLabel.SetRenderPriority(2);
                    fromPresetWindow.AddObject(presetLabel);
                    handler.AddObject(presetLabel);

                    MenuLabel presetDescription = new MenuLabel(style.GetPoint(new Point2D.Double(-325.0 + 475 * y, -290.0+40+40 + 160 * (i % 3)), Style.Anchor.Center));
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

        // Settings Window
        {
            settingsWindow = new MenuGroup();

            MenuPanel panel = new MenuPanel(style.GetTransform(new UITransform(0, 0, 1000, 600), Style.Anchor.Center));
            panel.SetRenderPriority(2);
            panel.SetScreenSize(width, height);
            panel.SetPanel(true);
            panel.SetRoundness(style.GetScaled(10.0));
            panel.SetColor(style.GetColor(Style.ColorPalette.Body1.value-1));
            settingsWindow.AddObject(panel);
            handler.AddObject(panel);

            MenuLabel settingsLabel = new MenuLabel(style.GetPoint(new Point2D.Double(0, -300), Style.Anchor.Center));
            settingsLabel.SetRenderPriority(3);
            settingsLabel.SetAlignment(MenuLabel.Alignment.Top);
            settingsLabel.SetFont(style.GetFont(7));
            settingsLabel.SetText("Settings");
            settingsLabel.SetColor(style.GetColor(Style.ColorPalette.Body1.value+2));
            settingsWindow.AddObject(settingsLabel);
            handler.AddObject(settingsLabel);

            MenuLabel widthLabel = new MenuLabel(style.GetPoint(new Point2D.Double(-450, -250+10), Style.Anchor.Center));
            widthLabel.SetRenderPriority(3);
            widthLabel.SetAlignment(MenuLabel.Alignment.Upper_Left);
            widthLabel.SetFont(style.GetFont(4));
            widthLabel.SetText("Screen Width");
            widthLabel.SetColor(style.GetColor(Style.ColorPalette.Body3.value+1));
            settingsWindow.AddObject(widthLabel);
            handler.AddObject(widthLabel);

            MenuInputField widthInput = new MenuInputField(style.GetTransform(new UITransform(-375.0, -185.0+10, 150, 50),
                    Style.Anchor.Center));
            style.SetInputField(widthInput, 50, 0);
            widthInput.SetChecker(Character::isDigit);
            widthInput.SetSelectAction(() -> SelectInputField(widthInput));
            widthInput.SetFont(style.GetFont(5));
            widthInput.SetRenderPriority(3);
            settingsWindow.AddObject(widthInput);
            handler.AddObject(widthInput);

            MenuLabel heightLabel = new MenuLabel(style.GetPoint(new Point2D.Double(-450, -125.0), Style.Anchor.Center));
            heightLabel.SetRenderPriority(3);
            heightLabel.SetAlignment(MenuLabel.Alignment.Upper_Left);
            heightLabel.SetFont(style.GetFont(4));
            heightLabel.SetText("Screen Height");
            heightLabel.SetColor(style.GetColor(Style.ColorPalette.Body3.value+1));
            settingsWindow.AddObject(heightLabel);
            handler.AddObject(heightLabel);

            MenuInputField heightInput = new MenuInputField(style.GetTransform(new UITransform(-375.0, -60, 150, 50),
                    Style.Anchor.Center));
            style.SetInputField(heightInput, 50, 0);
            heightInput.SetChecker(Character::isDigit);
            heightInput.SetSelectAction(() -> SelectInputField(heightInput));
            heightInput.SetFont(style.GetFont(5));
            heightInput.SetRenderPriority(3);
            settingsWindow.AddObject(heightInput);
            handler.AddObject(heightInput);

            MenuLabel fullscreenLabel = new MenuLabel(style.GetPoint(new Point2D.Double(-450, 0), Style.Anchor.Center));
            fullscreenLabel.SetRenderPriority(3);
            fullscreenLabel.SetAlignment(MenuLabel.Alignment.Upper_Left);
            fullscreenLabel.SetFont(style.GetFont(4));
            fullscreenLabel.SetText("Fullscreen");
            fullscreenLabel.SetColor(style.GetColor(Style.ColorPalette.Body3.value+1));
            settingsWindow.AddObject(fullscreenLabel);
            handler.AddObject(fullscreenLabel);

            MenuButton fullscreenButton = new MenuButton(style.GetTransform(new UITransform(-450+25, 65, 50, 50), Style.Anchor.Center),
                    style.GetScaled(10.0));
            fullscreenButton.SetText("");
            fullscreenButton.SetRenderPriority(3);
            settingsWindow.AddObject(fullscreenButton);
            handler.AddObject(fullscreenButton);

            MenuLabel fullToggleLabel = new MenuLabel(style.GetPoint(new Point2D.Double(-450+55, 65), Style.Anchor.Center));
            fullToggleLabel.SetRenderPriority(3);
            fullToggleLabel.SetAlignment(MenuLabel.Alignment.Left);
            fullToggleLabel.SetFont(style.GetFont(8));
            fullToggleLabel.SetColor(style.GetColor(Style.ColorPalette.Body1.value+2));
            settingsWindow.AddObject(fullToggleLabel);
            handler.AddObject(fullToggleLabel);

            MenuLabel antialiasLabel = new MenuLabel(style.GetPoint(new Point2D.Double(-450, 100), Style.Anchor.Center));
            antialiasLabel.SetRenderPriority(3);
            antialiasLabel.SetAlignment(MenuLabel.Alignment.Upper_Left);
            antialiasLabel.SetFont(style.GetFont(4));
            antialiasLabel.SetText("Anti-Aliasing");
            antialiasLabel.SetColor(style.GetColor(Style.ColorPalette.Body3.value+1));
            settingsWindow.AddObject(antialiasLabel);
            handler.AddObject(antialiasLabel);

            MenuButton antialiasButton = new MenuButton(style.GetTransform(new UITransform(-450+25, 165, 50, 50), Style.Anchor.Center),
                    style.GetScaled(10.0));
            antialiasButton.SetText("");
            antialiasButton.SetRenderPriority(3);
            settingsWindow.AddObject(antialiasButton);
            handler.AddObject(antialiasButton);

            MenuLabel antialiasToggleLabel = new MenuLabel(style.GetPoint(new Point2D.Double(-450+55, 165), Style.Anchor.Center));
            antialiasToggleLabel.SetRenderPriority(3);
            antialiasToggleLabel.SetAlignment(MenuLabel.Alignment.Left);
            antialiasToggleLabel.SetFont(style.GetFont(8));
            antialiasToggleLabel.SetColor(style.GetColor(Style.ColorPalette.Body1.value+2));
            settingsWindow.AddObject(antialiasToggleLabel);
            handler.AddObject(antialiasToggleLabel);

            //320 180
            //640 360
            //768 1024
            //1280 720
            //1280 1024
            //1366 768
            //1440 900
            //1536 864
            //1600 900
            //1920 1080
            //2560 1440
            int[] widthPresets = new int[]{
                    320, 640, 768, 1280, 1280, 1366, 1440, 1536, 1600, 1920, 2560
            };
            int[] heightPresets = new int[]{
                    180, 360, 1024, 720, 1024, 768, 900, 864, 900, 1080, 1440
            };

            MenuButton[] screenSizePresetButtons = new MenuButton[widthPresets.length];
            for(int i = 0; i < widthPresets.length; i++)
            {
                int x = i%3, y = i/3;
                MenuButton presetButton = new MenuButton(style.GetTransform(new UITransform(-50 + x*160, -185 + y*60, 150, 50),
                        Style.Anchor.Center), style.GetScaled(10.0));
                presetButton.SetTextStyle(style.GetFont(5));
                presetButton.SetText(widthPresets[i] + "x" + heightPresets[i]);
                presetButton.SetRenderPriority(3);
                settingsWindow.AddObject(presetButton);
                handler.AddObject(presetButton);

                screenSizePresetButtons[i] = presetButton;
            }

            double paletteXOffset = -120, paletteYOffset = 40;
            Style.Anchor parametersAnchor = Style.Anchor.Center;
            ParameterHolder<Integer> paletteParam = new ParameterHolder<Integer>() {
                public void Set(Integer value) {
                    settingsPalette = value;
                }
                public Integer Get() {
                    return settingsPalette;
                }
            };

            Point2D.Double pos = style.GetPoint(new Point2D.Double(paletteXOffset, paletteYOffset), parametersAnchor);
            MenuLabel label = new MenuLabel(pos.x, pos.y);
            label.SetAlignment(MenuLabel.Alignment.Upper_Left);
            label.SetFont(style.GetFont(4));
            label.SetColor(style.GetColor(Style.ColorPalette.Body3.value+1));
            label.SetText("Color Palette");
            paletteYOffset += 30.0;

            MenuInputField inputField =
                    new MenuInputField(style.GetTransform(new UITransform(paletteXOffset+100, paletteYOffset+25, 200, 45),
                    parametersAnchor));
            inputField.SetThickness(style.GetScaled(5.0));
            inputField.SetRoundness(style.GetScaled(10.0));
            inputField.SetTextFieldColor(style.GetColor(Style.ColorPalette.Body1.value-2));
            MenuButton fieldButton = inputField.getButton();
            style.SetButtonColors(fieldButton, 0);
            inputField.SetPlaceholder("Enter Integer");
            inputField.SetFonts(style.GetFont(5), style.GetFont(6));
            inputField.SetText(paletteParam.Get().toString());
            inputField.SetMultiline(true);
            MenuParameter<Integer> param = new MenuParameter<Integer>() {
                private int value = paletteParam.Get();
                public void SetParameterValue(Integer val)
                {
                    value = val;
                    if(value < 0)
                        value = 0;
                    else if(value > maxValue)
                        value = maxValue;
                    paletteParam.Set(value);
                    inputField.SetText(((Integer)value).toString());
                }
                public Integer GetParameterValue() {
                    return value;
                }
            };

            inputField.SetSelectAction(() -> SelectInputField(inputField));
            inputField.SetDeselectAction(() -> {
                String value = inputField.GetText();
                if(value == null || value.equals("")) {
                    param.SetParameterValue(0);
                    return;
                }
                int num = 0, st = 0;
                boolean rev = false;
                if(value.charAt(0) == '-')
                {
                    st++; rev = true;
                }
                for(int i = st; i < value.length(); i++)
                {
                    char c = value.charAt(i);
                    if(!Character.isDigit(c))
                    {
                        param.SetParameterValue(0);
                        return;
                    }
                    num = num*10 + (c-'0');
                }
                if(rev)
                    num = -num;
                param.SetParameterValue(num);
                DeselectInputField();
            });

            MenuButton decButton = new MenuButton(style.GetTransform(new UITransform(paletteXOffset+225, paletteYOffset+25, 45, 45),
                    parametersAnchor), style.GetScaled(10.0));
            style.SetButtonColors(decButton, 0);
            decButton.SetTextStyle(style.GetFont(5));
            decButton.SetText("-");
            decButton.SetAction(() -> {
                int value = param.GetParameterValue()-1;
                if(value < 0)
                    value = 0;
                param.SetParameterValue(value);
            }, 0);

            MenuButton addButton = new MenuButton(style.GetTransform(new UITransform(paletteXOffset+275, paletteYOffset+25, 45, 45),
                    parametersAnchor), style.GetScaled(10.0));
            style.SetButtonColors(addButton, 0);
            addButton.SetTextStyle(style.GetFont(5));
            addButton.SetText("+");
            addButton.SetAction(() -> {
                int value = param.GetParameterValue()+1;
                if(value > maxValue)
                    value = maxValue;
                param.SetParameterValue(value);
            }, 0);

            MenuButton defButton = new MenuButton(style.GetTransform(new UITransform(paletteXOffset+325, paletteYOffset+25, 45, 45),
                    parametersAnchor), style.GetScaled(10.0));
            style.SetButtonColors(defButton, 0);
            defButton.SetTextStyle(style.GetFont(5));
            defButton.SetText("x");
            defButton.SetAction(() -> {
                param.SetParameterValue(0);
            }, 0);

            label.SetRenderPriority(3);
            inputField.SetRenderPriority(3);
            addButton.SetRenderPriority(3);
            decButton.SetRenderPriority(3);
            defButton.SetRenderPriority(3);

            handler.AddObject(label);
            handler.AddObject(inputField);
            handler.AddObject(addButton);
            handler.AddObject(decButton);
            handler.AddObject(defButton);

            settingsWindow.AddObject(label);
            settingsWindow.AddObject(inputField);
            settingsWindow.AddObject(addButton);
            settingsWindow.AddObject(decButton);
            settingsWindow.AddObject(defButton);

            ParameterHolder<Integer> widthParam = new ParameterHolder<Integer>() {
                public void Set(Integer value) {
                    settingsWidth = value;
                    widthInput.SetText(String.valueOf(value));
                    for(int i = 0; i < screenSizePresetButtons.length; i++)
                    {
                        style.SetButtonColors(screenSizePresetButtons[i],
                                (settingsWidth == widthPresets[i] && settingsHeight == heightPresets[i])? 1 : 0);
                    }
                }
                public Integer Get() {
                    return settingsWidth;
                }
            };
            ParameterHolder<Integer> heightParam = new ParameterHolder<Integer>() {
                public void Set(Integer value) {
                    settingsHeight = value;
                    heightInput.SetText(String.valueOf(value));
                    for(int i = 0; i < screenSizePresetButtons.length; i++)
                    {
                        style.SetButtonColors(screenSizePresetButtons[i],
                                (settingsWidth == widthPresets[i] && settingsHeight == heightPresets[i])? 1 : 0);
                    }
                }
                public Integer Get() {
                    return settingsHeight;
                }
            };
            ParameterHolder<Boolean> fullscreenParam = new ParameterHolder<Boolean>() {
                public void Set(Boolean value) {
                    settingsFullScreen = value;
                    style.SetButtonColors(fullscreenButton, (value)? 1 : 0);
                    fullToggleLabel.SetText((value)? "Yes" : "No");
                }
                public Boolean Get() {
                    return settingsFullScreen;
                }
            };
            ParameterHolder<Boolean> antialiasParam = new ParameterHolder<Boolean>() {
                public void Set(Boolean value) {
                    settingsAntialias = value;
                    style.SetButtonColors(antialiasButton, (value)? 1 : 0);
                    antialiasToggleLabel.SetText((value)? "Yes" : "No");
                }
                public Boolean Get() {
                    return settingsAntialias;
                }
            };
            widthParam.Set(settingsWidth);
            heightParam.Set(settingsHeight);
            fullscreenParam.Set(settingsFullScreen);
            antialiasParam.Set(settingsAntialias);

            widthInput.SetDeselectAction(() -> {
                String txt = widthInput.GetText();
                int val = 1280;
                try{
                    val = Integer.parseInt(txt);
                } catch (Exception e) {
                    val = 1280;
                }
                if(val < 320)
                    val = 320;
                if(val > 3840)
                    val = 3840;
                widthParam.Set(val);
                DeselectInputField();
            });
            heightInput.SetDeselectAction(() -> {
                String txt = heightInput.GetText();
                int val = 720;
                try{
                    val = Integer.parseInt(txt);
                } catch (Exception e) {
                    val = 720;
                }
                if(val < 180)
                    val = 180;
                if(val > 2160)
                    val = 2160;
                heightParam.Set(val);
                DeselectInputField();
            });
            fullscreenButton.SetAction(() -> fullscreenParam.Set(!fullscreenParam.Get()), 0);
            antialiasButton.SetAction(() -> antialiasParam.Set(!antialiasParam.Get()), 0);
            for(int i = 0; i < screenSizePresetButtons.length; i++)
            {
                int finalI = i;
                screenSizePresetButtons[i].SetAction(() -> {
                    widthParam.Set(widthPresets[finalI]);
                    heightParam.Set(heightPresets[finalI]);
                }, 0);
            }

            MenuButton closeButton = new MenuButton(style.GetTransform(new UITransform(415.0, 265.0, 150, 50), Style.Anchor.Center),
                    style.GetScaled(10.0));
            style.SetButtonColors(closeButton, 1);
            closeButton.SetRenderPriority(3);
            closeButton.SetTextStyle(style.GetFont(4));
            closeButton.SetText("Close");
            closeButton.SetAction(() -> settingsWindow.SetVisible(false), 0);
            settingsWindow.AddObject(closeButton);
            handler.AddObject(closeButton);

            MenuButton applyButton = new MenuButton(style.GetTransform(new UITransform(255.0, 265.0, 150, 50), Style.Anchor.Center),
                    style.GetScaled(10.0));
            style.SetButtonColors(applyButton, 4);
            applyButton.SetRenderPriority(3);
            applyButton.SetTextStyle(style.GetFont(4));
            applyButton.SetText("Apply");
            applyButton.SetAction(() -> pres.ApplySettings(settingsWidth, settingsHeight, settingsFullScreen, settingsAntialias, settingsPalette), 0);
            settingsWindow.AddObject(applyButton);
            handler.AddObject(applyButton);
        }
        settingsWindow.SetVisible(showSettings);
    }

    public void Render()
    {
        BufferStrategy bs = this.getBufferStrategy();
        if(bs == null)
        {
            try {
                this.createBufferStrategy(3);
            }
            catch (Exception e)
            {
                System.err.println("Could not create buffer strategy");
                return;
            }
        }
        try {
            if (bs == null || bs.getDrawGraphics() == null)
                return;
        }
        catch (Exception e)
        {
            System.err.println("Could not create buffer strategy");
            return;
        }
        Graphics g = bs.getDrawGraphics();
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
        g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                (renderingAntialias)? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF));
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
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
        playSudokuDescription.SetText("This file is not a proper sudoku\nfile. please select a proper file.");
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
        editSudokuDescription.SetText("This file is not a proper sudoku\nfile. please select a proper file.");
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
