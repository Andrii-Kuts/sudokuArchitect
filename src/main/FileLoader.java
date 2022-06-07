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
import java.util.ArrayList;
import java.util.stream.Collectors;

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
            ClassLoader classLoader = getClass().getClassLoader();
            URL url = classLoader.getResource("fonts");
            String path = url.getPath();
            File[] fonts = new File(path).listFiles();
            for (File file : fonts)
            {
                LoadFont(file.getName());
            }
        }
        catch (Exception e)
        {
            System.err.println("Something went wrong while loading fonts");
            e.printStackTrace();
        }
    }

    public String[] GetSudokuPresets()
    {
        try {
            ArrayList<String> res = new ArrayList<>();
            ClassLoader classLoader = getClass().getClassLoader();
            URL url = classLoader.getResource("presets");
            String path = url.getPath();
            File[] presets = new File(path).listFiles();
            for (File file : presets)
            {
                res.add(file.getPath());
            }
            return res.toArray(String[]::new);
        }
        catch (Exception e)
        {
            System.err.println("Something went wrong while loading fonts");
            e.printStackTrace();
            return null;
        }
    }
}
