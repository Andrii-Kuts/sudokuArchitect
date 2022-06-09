package main;

public class LaunchAppScript
{
    public static void main(String[] args)
    {
        FileLoader.getInstance().LoadFonts();
        System.setProperty("sun.java2d.opengl", "true");
        UserSettings settings = UserSettings.getInstance();
        Window window = new Window(settings.GetScreenWidth(), settings.GetScreenHeight(), settings.GetFullscreen(), "Sudoku Architect");
    }
}
