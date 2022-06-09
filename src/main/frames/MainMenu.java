package main.frames;

import main.ParameterHolder;
import main.UserSettings;
import main.Window;
import main.WindowFrame;
import main.sudoku.Board;
import main.ui.MenuLabel;
import main.ui.graphics.UIImage;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;

public class MainMenu implements WindowFrame
{
    private MainMenuModel model;
    private MainMenuView view;
    private Window window;

    private boolean isRunning = false;
    private Thread thread;
    private ArrayList<Runnable> endActions;

    private String playFilePath, editFilePath, presetFilePath;
    private boolean newFile = false;
    private Board board;
    private int editorPanel = 0, boardHeight, boardWidth;
    private ParameterHolder<Integer> newBoardWidth, newBoardHeight;

    public MainMenu(int width, int height, Window window)
    {
        this(width, height, window, false);
    }
    public MainMenu(int width, int height, Window window, boolean showSettings)
    {
        this.window = window;
        model = new MainMenuModel();
        board = new Board(null);
        boardWidth = boardHeight = 9;
        newBoardWidth = new ParameterHolder<Integer>() {
            public void Set(Integer value) {
                boardWidth = value;
            }
            public Integer Get() {
                return boardWidth;
            }
        };
        newBoardHeight = new ParameterHolder<Integer>() {
            public void Set(Integer value) {
                boardHeight = value;
            }
            public Integer Get() {
                return boardHeight;
            }
        };
        view = new MainMenuView(this, window, width, height, showSettings);
        endActions = new ArrayList<>();

        playFilePath = null;
        editFilePath = null;
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
                Tick(delta/1000.0);
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

    public Canvas getFrame()
    {
        return view;
    }

    private void Tick(double delta)
    {
        view.Tick(delta);
    }

    public void PlayWindow(boolean value)
    {
        view.PlayWindow(value);
    }

    public void OpenPlayFile()
    {
        try {
            FileDialog dialog = new FileDialog(window.getFrame());
            dialog.setMode(FileDialog.LOAD);
            dialog.setVisible(true);
            String path = dialog.getDirectory() + dialog.getFile();
            if(dialog.getFile() == null)
            {
                return;
            }
            playFilePath = path;
            try{
                board.BoardLoad(playFilePath);
                view.PlayFilePreview(board.GetTitle(), board.GetAuthor(), board.GetDescription(), board.GetFullImage());
            }
            catch (Exception e)
            {
                System.err.println("Warning: File is not a proper .saf");
                e.printStackTrace();
                view.PlayEmptyFile();
            }
        }
        catch(Exception e)
        {
            System.err.println("Couldn't open file browser");
            e.printStackTrace();
            return;
        }
    }
    public void OpenEditorFile()
    {
        try {
            FileDialog dialog = new FileDialog(window.getFrame());
            dialog.setMode(FileDialog.LOAD);
            dialog.setVisible(true);
            if(dialog.getFile() == null)
                return;
            String path = dialog.getDirectory() + dialog.getFile();
            editFilePath = path;
            try{
                board.BoardLoad(editFilePath);
                view.EditFilePreview(board.GetTitle(), board.GetAuthor(), board.GetDescription(), board.GetFullImage());
            }
            catch (Exception e)
            {
                System.err.println("Warning: File is not a proper .saf");
                e.printStackTrace();
                view.EditEmptyFile();
            }
        }
        catch(Exception e)
        {
            System.err.println("Couldn't open file browser");
            e.printStackTrace();
            return;
        }
    }


    public void Play()
    {
        if(playFilePath == null || playFilePath.equals(""))
        {
            //TODO show message in menu
            System.err.println("Tried to open an empty file");
        }
        endActions.add(() -> window.LoadPlayWindow(playFilePath));
    }
    public void Editor()
    {
        if(editorPanel == 2)
        {
            endActions.add(() -> window.LoadEditWindow(boardWidth, boardHeight));
        }
        String path = null;
        if (editorPanel == 0) {
            path = editFilePath;
        } else if (editorPanel == 1) {
            path = presetFilePath;
        }
        if((path == null || path.equals("")) && !newFile)
        {
            //TODO show message in menu
            System.err.println("Tried to open an empty file");
        }
        String finalPath = path;
        endActions.add(() -> window.LoadEditWindow(finalPath, newFile));
    }

    public void About()
    {
        System.out.println("About Menu");
    }

    public void Exit()
    {
        System.out.println("Exiting");
        endActions.add(() -> window.CloseProgram());
    }

    public void GetPreset(UIImage image, MenuLabel label, MenuLabel description, String path)
    {
        try {
            System.out.println("Loading preset: " + path);
            board.BoardLoad(path);
            image.SetImage(board.GetPreview());
            label.SetText(board.GetTitle());
            description.SetText(board.GetDescription());
        }
        catch(Exception e)
        {
            e.printStackTrace();
            image.SetImage(null);
            label.SetText("Error");
        }
    }
    public ParameterHolder<Integer> GetBoardDimension(boolean ind)
    {
        if(!ind)
            return newBoardWidth;
        else
            return newBoardHeight;
    }
    public void SelectPreset(String path){
        presetFilePath = path;
    }
    public void FromFile() {
        newFile = false;
        if(editorPanel != 0)
            view.EditPanel(editorPanel, 0);
        editorPanel = 0;
    }
    public void FromPreset(){
        newFile = true;
        if(editorPanel != 1)
            view.EditPanel(editorPanel, 1);
        editorPanel = 1;
    }
    public void NewBoard(){
        newFile = true;
        if(editorPanel != 2)
            view.EditPanel(editorPanel, 2);
        editorPanel = 2;
    }

    public void EditorWindow(boolean state) {
        view.EditWindow(state, editorPanel);
    }

    public void ApplySettings(int width, int height, boolean fullscreen, boolean antialias, int palette)
    {
        UserSettings settings = UserSettings.getInstance();
        settings.SetScreenWidth(width);
        settings.SetScreenHeight(height);
        settings.SetFullscreen(fullscreen);
        settings.SetAntialias(antialias);
        settings.SetColorPalette(palette);

        window.SetScreenSize(width, height);
        window.SetFullscreen(fullscreen);
        endActions.add(() -> window.ReloadMainMenu());
    }
}
