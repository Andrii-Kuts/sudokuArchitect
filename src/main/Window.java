package main;

import main.frames.EditorWindow;
import main.frames.MainMenu;
import main.frames.PlayWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

public class Window extends Canvas
{
    private JFrame frame;
    private WindowFrame script;
    private Component component;
    private GraphicsDevice device;

    private int width, height;
    private boolean fullscreen;

    public Window(int width, int height, boolean fullscreen, String title)
    {
        device = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];

        this.width = width;
        this.height = height;
        this.fullscreen = fullscreen;
        frame = new JFrame(title);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        LoadMainMenu();
    }

    public void UpdateWindowFormat()
    {
        frame.getContentPane().setPreferredSize(new Dimension(width, height));
        frame.getContentPane().setMaximumSize(new Dimension(width, height));
        frame.getContentPane().setMinimumSize(new Dimension(width, height));

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.dispose();

        if(fullscreen)
        {
            frame.setUndecorated(true);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        }
        else
        {
            frame.setUndecorated(false);
            frame.setExtendedState(JFrame.NORMAL);
        }

        device.setFullScreenWindow((fullscreen)? frame : null);
        frame.setVisible(true);
    }

    public boolean isFullscreen()
    {
        return fullscreen;
    }

    public JFrame getFrame()
    {
        return frame;
    }

    private void StopFrame()
    {
        if(component != null)
            frame.remove(component);
    }

    private void RunFrame(WindowFrame script)
    {
        this.script = script;
        component = script.getFrame();
        frame.add(component);
        UpdateWindowFormat();
        script.start();
    }

    public void CloseProgram()
    {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    public void LoadMainMenu()
    {
        StopFrame();
        RunFrame(new MainMenu(width, height, this));
    }
    public void LoadPlayWindow()
    {
        LoadPlayWindow(null);
    }
    public void LoadPlayWindow(String path)
    {
        StopFrame();
        RunFrame(new PlayWindow(this, width, height, path));
    }
    public void LoadEditWindow(String path, boolean newFile)
    {
        StopFrame();
        RunFrame(new EditorWindow(this, width, height, path, newFile));
    }
    public void LoadEditWindow(int dimensionX, int dimensionY)
    {
        StopFrame();
        RunFrame(new EditorWindow(this, width, height, null, true, dimensionX, dimensionY));
    }
}
