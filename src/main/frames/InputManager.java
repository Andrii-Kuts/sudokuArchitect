package main.frames;

import main.Window;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.TreeSet;

public class InputManager
{
    private Window window;
    private Canvas canvas;
    private KeyInput keyInput;

    private boolean[] mouseHeld, mouseHeldConfirmed;

    public InputManager(Window window, Canvas canvas)
    {
        this.window = window;
        this.canvas = canvas;
        keyInput = new KeyInput();

        canvas.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if(e.getButton() > 3)
                    return;
                mouseHeld[e.getButton()] = true;
            }
            public void mouseReleased(MouseEvent e) {
                if(e.getButton() > 3)
                    return;
                mouseHeld[e.getButton()] = false;
            }
        });
        canvas.addKeyListener(keyInput);
        mouseHeld = new boolean[4];
        mouseHeldConfirmed = new boolean[4];
    }

    private class KeyInput implements KeyListener
    {
        private ArrayList<KeyEvent> events;
        private TreeSet<Integer> pressedKeys;

        private KeyInput()
        {
            events = new ArrayList<>();
            pressedKeys = new TreeSet<>();
        }
        private ArrayList<KeyEvent> GetInput()
        {
            ArrayList<KeyEvent> res = new ArrayList<>();
            res.addAll(events);
            events.clear();
            return res;
        }
        private boolean IsHeld(int code)
        {
            return pressedKeys.contains(code);
        }
        @Override
        public void keyTyped(KeyEvent keyEvent) {
                events.add(keyEvent);
        }

        @Override
        public void keyPressed(KeyEvent keyEvent)
        {
            int size = pressedKeys.size();
            pressedKeys.add(keyEvent.getKeyCode());
            if(size != pressedKeys.size())
                events.add(keyEvent);
        }

        @Override
        public void keyReleased(KeyEvent keyEvent) {
            events.add(keyEvent);
            pressedKeys.remove(keyEvent.getKeyCode());
        }
    }

    public ArrayList<KeyEvent> GetKeyInput()
    {
        return keyInput.GetInput();
    }

    public boolean GetMouse(int ind)
    {
        return mouseHeldConfirmed[ind];
    }

    public Point GetMousePosition()
    {
        Point res = MouseInfo.getPointerInfo().getLocation();
        if(true) {
            SwingUtilities.convertPointFromScreen(res, window.getFrame().getContentPane());
        }
        return res;
    }

    public void SaveMouseState()
    {
        System.arraycopy(mouseHeld, 0, mouseHeldConfirmed, 0, 4);
    }

    public boolean IsHeld(int code)
    {
        return keyInput.IsHeld(code);
    }
}
