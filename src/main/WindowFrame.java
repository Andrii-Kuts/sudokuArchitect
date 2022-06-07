package main;

import java.awt.*;

public interface WindowFrame extends Runnable
{
    void start();
    void stop();
    Canvas getFrame();
}
