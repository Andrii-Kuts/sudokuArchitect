package main;

public class LaunchAppScript
{
    public static void main(String[] args)
    {
        FileLoader.getInstance().LoadFonts();
        System.setProperty("sun.java2d.opengl", "true");
        Window window = new Window(640, 360, false, "Sudoku Architect");
    }
}
