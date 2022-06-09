package main;

import java.util.prefs.Preferences;

public class UserSettings
{
    private static UserSettings instance;
    private Preferences prefs;

    private UserSettings()
    {
        prefs = Preferences.userNodeForPackage(this.getClass());
    }

    public static UserSettings getInstance()
    {
        if(instance == null)
            instance = new UserSettings();
        return instance;
    }

    public int GetScreenWidth(){
        String value = prefs.get("screen_width", "1280");
        try
        {
            return Integer.parseInt(value);
        }
        catch (Exception e)
        {
            return 1280;
        }
    }
    public int GetScreenHeight(){
        String value = prefs.get("screen_height", "720");
        try
        {
            return Integer.parseInt(value);
        }
        catch (Exception e)
        {
            return 720;
        }
    }
    public boolean GetFullscreen(){
        String value = prefs.get("fullscreen", "false");
        try
        {
            return Boolean.parseBoolean(value);
        }
        catch (Exception e)
        {
            return false;
        }
    }
    public boolean GetAntialias(){
        String value = prefs.get("antialias", "true");
        try
        {
            return Boolean.parseBoolean(value);
        }
        catch (Exception e)
        {
            return true;
        }
    }
    public int GetColorPalette()
    {
        String value = prefs.get("palette", "0");
        try
        {
            return Integer.parseInt(value);
        }
        catch (Exception e)
        {
            return 0;
        }
    }

    public void SetScreenWidth(int value) {
        prefs.put("screen_width", String.valueOf(value));
    }
    public void SetScreenHeight(int value) {
        prefs.put("screen_height", String.valueOf(value));
    }
    public void SetFullscreen(boolean value) {
        prefs.put("fullscreen", String.valueOf(value));
    }
    public void SetAntialias(boolean value) {
        prefs.put("antialias", String.valueOf(value));
    }
    public void SetColorPalette(int value) {
        prefs.put("palette", String.valueOf(value));
    }
}
