package main.sudoku;

import main.DecodeError;
import main.sudoku.graphics.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Board
{
    private int width, height;
    private String title, author, description, ruleSet;
    private BufferedImage preview, picture;
    private int[][] uniqueSolution;
    private Cell[][] cells;
    private ArrayList<SudokuConstraint> constraints;
    private ArrayList<Constraint> constraintsGraphics;
    private ArrayList<Graphic> graphics;
    private SudokuController controller;
    private static Class[] constraintsClasses, constraintGraphicsClasses, graphicsClasses;

    public Board(SudokuController controller)
    {
        GraphicsManager graphicsManager = GraphicsManager.getInstance();
        constraintsClasses = graphicsManager.GetConstraintClasses();
        constraintGraphicsClasses = graphicsManager.GetConstraintGraphicsClasses();
        graphicsClasses = graphicsManager.GetGraphicsClasses();

        this.controller = controller;
        width = height = 9;
        title = "New Sudoku";
        author = "Jajceslav";
        cells = new Cell[width][height];
        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                cells[i][j] = new Cell();
            }
        }
        constraints = new ArrayList<>();
        graphics = new ArrayList<>();
        constraintsGraphics = new ArrayList<>();
        Rect rect = new Rect(0, 0, 9, 9);
        rect.priority = -2;
        rect.color = new Color(255, 255, 255);
        graphics.add(rect);

        for(int i = 0; i < 9; i++)
        {
            UniqueSet us = new UniqueSet(this);
            us.AddLine(new BoardPosition(i, 0), new BoardPosition(i, 8));
            constraints.add(us);

            UniqueLine uq = new UniqueLine(i, 0, i, 8);
            uq.renderIdle = false;
            uq.thickness = 4;
            uq.priority = -1;
            constraintsGraphics.add(uq);
        }
        for(int i = 0; i < 9; i++)
        {
            UniqueSet us = new UniqueSet(this);
            us.AddLine(new BoardPosition(0, i), new BoardPosition(8, i));
            constraints.add(us);

            UniqueLine uq = new UniqueLine(0, i, 8, i);
            uq.renderIdle = false;
            uq.thickness = 4;
            uq.priority = -1;
            constraintsGraphics.add(uq);
        }
        for(int x = 0; x < 3; x++)
        {
            for(int y = 0; y < 3; y++)
            {
                UniqueSet us = new UniqueSet(this);
                us.AddRectangle(new BoardPosition(x*3, y*3), new BoardPosition(x*3+2, y*3+2));
                constraints.add(us);

                UniqueRect ur = new UniqueRect(x*3, y*3, x*3+2, y*3+2);
                ur.priority = 1;
                ur.color = new Color(0, 0, 0, 0);
                ur.okColor = new Color(0, 200, 0, 80);
                ur.wrongColor = new Color(200, 0, 0, 80);
                constraintsGraphics.add(ur);
            }
        }
        for(int i = 0; i <= 9; i++)
        {
            double thickness = 2;
            if(i % 3 == 0)
                thickness = 5.0;

            Line line = new Line(i, 0, i, 9);
            line.priority = 2;
            line.thickness = thickness;
            graphics.add(line);

            line = new Line(0, i, 9, i);
            line.priority = 2;
            line.thickness = thickness;
            graphics.add(line);
        }
    }

    public void CreateNew(int dimX, int dimY)
    {
        width = dimX;
        height = dimY;
        title = "New Sudoku";
        author = "Jajceslav";
        cells = new Cell[width][height];
        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                cells[i][j] = new Cell();
            }
        }
        constraints = new ArrayList<>();
        graphics = new ArrayList<>();
        constraintsGraphics = new ArrayList<>();
        Rect rect = new Rect(0, 0, dimX, dimY);
        rect.priority = -2;
        rect.color = new Color(255, 255, 255);
        graphics.add(rect);

        for(int i = 0; i <= width; i++)
        {
            double thickness = 2;

            Line line = new Line(i, 0, i, height);
            line.priority = 2;
            line.thickness = thickness;
            graphics.add(line);
        }
        for(int i = 0; i <= height; i++)
        {
            double thickness = 2;

            Line line = new Line(0, i, width, i);
            line.priority = 2;
            line.thickness = thickness;
            graphics.add(line);
        }
    }

    public boolean CheckBoard()
    {
        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                if(cells[i][j].GetType() == Cell.CellType.Regular && cells[i][j].GetValue() == 0)
                    return false;
            }
        }
        for(SudokuConstraint cs : constraints)
        {
            if(!cs.CheckBoard())
                return false;
        }
        return true;
    }

    //region Loading & Saving
    private int GetConstraintId(Class c)
    {
        for(int i = 0; i < constraintsClasses.length; i++)
        {
            if(constraintsClasses[i] == c)
                return i;
        }
        return -1;
    }
    private int GetConstraintGraphicId(Class c)
    {
        for(int i = 0; i < constraintGraphicsClasses.length; i++)
        {
            if(constraintGraphicsClasses[i] == c)
                return i;
        }
        return -1;
    }
    private int GetGraphicId(Class c)
    {
        for(int i = 0; i < graphicsClasses.length; i++)
        {
            if(graphicsClasses[i] == c)
                return i;
        }
        return -1;
    }

    private void putString(ByteBuffer buffer, String string)
    {
        if(string == null)
        {
            buffer.putInt(0);
            return;
        }
        buffer.putInt(string.length());
        buffer.put(string.getBytes(StandardCharsets.UTF_8));
    }
    private String getString(ByteBuffer buffer)
    {
        int len = buffer.getInt();
        if(len == 0)
        {
            return null;
        }
        byte[] bytes = new byte[len];
        buffer.get(bytes);
        return new String(bytes);
    }
    private byte[] imageToBytes(BufferedImage image, String format) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ImageIO.write(image, format, stream);
            return stream.toByteArray();
        }
        catch (Exception e) {
            System.err.println("Something went wrong while converting image to bytes.");
            e.printStackTrace();
            return null;
        }
    }
    private BufferedImage bytesToImage(byte[] bytes) {
        try {
            InputStream stream = new ByteArrayInputStream(bytes);
            BufferedImage image = ImageIO.read(stream);
            return image;
        }
        catch (Exception e) {
            System.err.println("Something went wrong while converting bytes to image.");
            e.printStackTrace();
            return null;
        }
    }
    private void putImage(ByteBuffer buffer, BufferedImage image)
    {
        if(image == null)
        {
            buffer.putInt(0);
            return;
        }
        byte[] bytes = imageToBytes(image, "png");
        buffer.putInt(bytes.length);
        buffer.put(bytes);
    }
    private BufferedImage getImage(ByteBuffer buffer)
    {
        int len = buffer.getInt();
        if(len == 0)
        {
            return null;
        }
        byte[] bytes = new byte[len];
        buffer.get(bytes);
        BufferedImage image = bytesToImage(bytes);
        return image;
    }
    public void BoardLoad(String path) throws Exception{
        if(!path.endsWith(".saf"))
            throw new DecodeError();
        File file = new File(path);
        FileInputStream stream = null;
        stream = new FileInputStream(file);
        ByteBuffer buffer = null;
        buffer = ByteBuffer.wrap(stream.readAllBytes());
        width = buffer.getInt();
        height = buffer.getInt();
        System.out.println("Loading board, width: " + width + ", height: " + height);
        title = getString(buffer);
        System.out.println("Title: " + title);
        author = getString(buffer);
        System.out.println("Author: " + author);
        description = getString(buffer);
        ruleSet = getString(buffer);
        preview = getImage(buffer);
        picture = getImage(buffer);
        byte hasUniqueSolution = buffer.get();
        if(hasUniqueSolution == 1) {
            uniqueSolution = new int[width][height];
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    uniqueSolution[i][j] = buffer.getInt();
                }
            }
        }
        cells = new Cell[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                cells[i][j] = new Cell();
                cells[i][j].Decode(buffer);
            }
        }
        constraints = new ArrayList<>();
        int n = buffer.getInt();
        if(n < 0)
            throw new DecodeError();
        for(int i = 0; i < n; i++)
        {
            int id = buffer.getInt();
            if(id < 0 || id >= constraintsClasses.length)
                throw new DecodeError();
            Class<?> cl = constraintsClasses[id];
            Constructor<?> constructor = cl.getConstructor(Board.class);
            SudokuConstraint sc = (SudokuConstraint)constructor.newInstance(this);
            sc.Decode(buffer);
            constraints.add(sc);
        }
        constraintsGraphics = new ArrayList<>();
        n = buffer.getInt();
        if(n < 0)
            throw new DecodeError();
        for(int i = 0; i < n; i++)
        {
            int id = buffer.getInt();
            if(id < 0 || id >= constraintGraphicsClasses.length) {
                System.err.println(id);
                throw new DecodeError();
            }
            Class<?> cl = constraintGraphicsClasses[id];
            Constructor<?> constructor = cl.getConstructor();
            Constraint sc = (Constraint) constructor.newInstance(new Object[]{});
            sc.Decode(buffer);
            constraintsGraphics.add(sc);
        }
        graphics = new ArrayList<>();
        n = buffer.getInt();
        if(n < 0)
            throw new DecodeError();
        for(int i = 0; i < n; i++)
        {
            int id = buffer.getInt();
            if(id < 0 || id >= graphicsClasses.length)
                throw new DecodeError();
            Class<?> cl = graphicsClasses[id];
            Constructor<?> constructor = cl.getConstructor();
            Graphic sc = (Graphic) constructor.newInstance(new Object[]{});
            sc.Decode(buffer);
            graphics.add(sc);
        }
    }
    public void BoardSave(String path) {
        if(!path.endsWith(".saf"))
            path += ".saf";
        File file = new File(path);
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
        } catch (Exception e) {
            System.err.println("Board Save: couldn't find file " + path);
            e.printStackTrace();
            return;
        }
        ByteBuffer buffer = ByteBuffer.allocate(1024*1000);
        buffer.putInt(width);
        buffer.putInt(height);
        putString(buffer, title);
        putString(buffer, author);
        putString(buffer, description);
        putString(buffer, ruleSet);
        putImage(buffer, preview);
        putImage(buffer, picture);
        if(uniqueSolution != null) {
            buffer.put((byte)1);
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    buffer.putInt(uniqueSolution[i][j]);
                }
            }
        }
        else {
            buffer.put((byte)0);
        }
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                buffer.put(cells[i][j].Encode());
            }
        }
        buffer.putInt(constraints.size());
        for(SudokuConstraint c : constraints)
        {
            buffer.putInt(GetConstraintId(c.getClass()));
            buffer.put(c.Encode());
        }
        buffer.putInt(constraintsGraphics.size());
        for(Constraint c : constraintsGraphics)
        {
            buffer.putInt(GetConstraintGraphicId(c.getClass()));
            buffer.put(c.Encode());
        }
        buffer.putInt(graphics.size());
        for(Graphic c : graphics)
        {
            buffer.putInt(GetGraphicId(c.getClass()));
            buffer.put(c.Encode());
        }
        byte[] byteArray = new byte[buffer.position()];
        System.arraycopy(buffer.array(), 0, byteArray, 0, byteArray.length);
        try {
            stream.write(byteArray);
            stream.close();
        } catch (Exception e) {
            System.err.println("Board Save: couldn't save bytes");
            e.printStackTrace();
            return;
        }
        System.out.println("Save file to " + path + ". Total file size: " + byteArray.length + " bytes.");
    }
    //endregion

    //region Main Operations
    public boolean[] GetConstraintsState()
    {
        boolean[] value = new boolean[constraints.size()];
        for(int i = 0; i < constraints.size(); i++)
        {
            if(constraints.get(i).CheckBoard())
                value[i] = true;
            else
                value[i] = false;
        }
        return value;
    }
    public void SetDigit(int x, int y, int digit) {
        if(cells[x][y].GetType() != Cell.CellType.Regular)
            return;
        cells[x][y].SetDigit(digit);
        controller.UpdateCell(x, y, cells[x][y]);
        controller.UpdateConstraints(GetConstraintsState());
    }
    public void SetPencil(int x, int y, int mask){
        if(cells[x][y].GetType() != Cell.CellType.Regular)
            return;
        cells[x][y].SetPencil(mask);
        controller.UpdateCell(x, y, cells[x][y]);
    }
    public void SetSmall(int x, int y, int mask){
        if(cells[x][y].GetType() != Cell.CellType.Regular)
            return;
        cells[x][y].SetSmall(mask);
        controller.UpdateCell(x, y, cells[x][y]);
    }
    public void SetColors(int x, int y, int mask){
        if(cells[x][y].GetType() == Cell.CellType.Outside)
            return;
        cells[x][y].SetColors(mask);
        controller.UpdateCell(x, y, cells[x][y]);
    }
    public void SetGraphics(ArrayList<Graphic> graphics)
    {
        this.graphics = graphics;
    }
    public void SetConstraints(ArrayList<SudokuConstraint> constraints)
    {
        this.constraints = constraints;
    }
    public void SetConstraintGraphics(ArrayList<Constraint> constraints)
    {
        this.constraintsGraphics = constraints;
    }
    public void AddGraphic(Graphic g)
    {
        graphics.add(g);
    }
    public void RemoveGraphic(Graphic g)
    {
        graphics.remove(g);
    }
    public void SetTitle(String title) {
        this.title = title;
    }
    public void SetAuthor(String author) {
        this.author = author;
    }
    public void SetDescription(String description) {
        this.description = description;
    }
    public void SetRuleSet(String ruleSet) {
        this.ruleSet = ruleSet;
    }
    public void SetPreview(BufferedImage preview) {
        this.preview = preview;
    }
    public void SetFullImage(BufferedImage picture) {
        this.picture = picture;
    }
    public void SetUniqueSolution(int[][] solution)
    {
        uniqueSolution = solution;
    }

    public Cell GetCell(int x, int y)
    {
        return cells[x][y];
    }
    public ArrayList<SudokuConstraint> GetConstraints()
    {
        return constraints;
    }
    public ArrayList<Constraint> GetConstraintGraphics()
    {
        return constraintsGraphics;
    }
    public ArrayList<Graphic> GetGraphics()
    {
        return graphics;
    }
    public int GetWidth() {
        return width;
    }
    public int GetHeight(){
        return height;
    }
    public String GetTitle() {
        return title;
    }
    public String GetAuthor() {
        return author;
    }
    public String GetDescription() {
        return description;
    }
    public String GetRuleSet() {
        return ruleSet;
    }
    public BufferedImage GetPreview() {
        return preview;
    }
    public BufferedImage GetFullImage() {
        return picture;
    }


    //endregion
}
