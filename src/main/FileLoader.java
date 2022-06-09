package main;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileLoader
{
    private static FileLoader instance;

    private FileLoader()
    {

    }

    public static FileLoader getInstance()
    {
        if(instance == null)
            instance = new FileLoader();
        return instance;
    }

    public BufferedImage ReadImage(String path)
    {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream input = classLoader.getResourceAsStream(path);
        try {
            ImageInputStream stream = ImageIO.createImageInputStream(input);
            ImageReader reader = ImageIO.getImageReaders(stream).next();
            reader.setInput(stream);

            int width = reader.getWidth(0);
            int height = reader.getHeight(0);
            ImageTypeSpecifier spec = reader.getImageTypes(0).next();

            BufferedImage image = new BufferedImage(width, height, spec.getBufferedImageType());
            ImageReadParam param = reader.getDefaultReadParam();
            param.setDestination(image);

            image = reader.read(0, param);
            return image;
        }
        catch (Exception e)
        {
            System.err.println("Something went wrong while trying to read resource image " + path);
            e.printStackTrace();
        }
        return null;
    }

    public Color[] ReadColorArray(String path)
    {
        BufferedImage image = ReadImage(path);
        int width = image.getWidth(), height = image.getHeight();
        Color[] colors = new Color[width*height];
        for(int i = 0; i < height; i++)
        {
            for(int j = 0; j < width; j++)
            {
                colors[i*width+j] = new Color(image.getRGB(j, i), true);
            }
        }
        return colors;
    }

    public String LoadFile(String fileName)
    {
        try
        {
            ClassLoader classLoader = getClass().getClassLoader();
            System.out.println("Reading file: " + fileName);
            InputStream input = classLoader.getResourceAsStream(fileName);
            String text = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
            return text;
        }
        catch (Exception e)
        {
            System.err.println("Something went wrong while loading file " + fileName);
            e.printStackTrace();
        }
        return null;
    }

    public void LoadFont(String fontName)
    {
        try
        {
            ClassLoader classLoader = getClass().getClassLoader();
            System.out.println("Reading font " + "fonts" + File.separator + fontName);
            InputStream input = classLoader.getResourceAsStream("fonts" + File.separator + fontName);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, input));
        }
        catch (Exception e)
        {
            System.err.println("Something went wrong while loading font " + fontName);
            e.printStackTrace();
        }
    }

    public void LoadFonts()
    {
        try {
            InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("fonts");
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String name;
            int cnt = 0;
            while((name = reader.readLine()) != null)
            {
                cnt++;
                LoadFont(name);
            }
            if(cnt == 0)
                throw new Exception();
            return;
        }
        catch (Exception e)
        {
            System.err.println("Something went wrong while loading fonts from folder. Loading from fonts.txt now.");
        }
        try {
            InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("fonts.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String name;
            while ((name = reader.readLine()) != null) {
                LoadFont(name);
            }
            return;
        }
        catch (Exception e)
        {
            System.err.println("Something went wrong while loading fonts from fonts.txt. Giving up");
            e.printStackTrace();
        }
    }

    public String[] GetSudokuPresets()
    {
        try {
            ArrayList<String> paths = new ArrayList<>();
            InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("presets");
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String name;
            while((name = reader.readLine()) != null)
            {
                URL res = getClass().getClassLoader().getResource("presets/" + name);
                File file = Paths.get(res.toURI()).toFile();
                paths.add(file.getAbsolutePath());
            }
            if(paths.isEmpty())
                throw new Exception();
            return paths.toArray(String[]::new);
        }
        catch (Exception e)
        {
            System.err.println("Something went wrong while loading sudoku presets from folder. Reading from presets.txt.");
        }
        try {
            ArrayList<String> paths = new ArrayList<>();
            File folder = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
            String path = folder.getPath()+ File.separator+"Presets";
            folder = new File(path);
            for(File file : folder.listFiles())
            {
                if(file.getName().endsWith(".saf"))
                    paths.add(file.getAbsolutePath());
            }

            if(paths.isEmpty())
                throw new Exception();
            return paths.toArray(String[]::new);
        }
        catch (Exception e)
        {
            System.err.println("Something went wrong while loading sudoku presets from folder. Reading from presets.txt.");
            e.printStackTrace();
            return null;
        }
    }

    public BufferedImage LoadBackground()
    {
        String path = "background" + UserSettings.getInstance().GetColorPalette() + ".png";
        return ReadImage(path);
    }
}
