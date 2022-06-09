package main.ui;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class MenuLabel extends RenderObject {
    private double posX, posY;
    private double width, height, fontHeight;
    private double[] linesWidth, linesHeight;
    private Color color;
    private Font font;
    private String text;
    private String[] lines;
    public enum Alignment{
        Upper_Left(-1, -1), Top(0, -1), Upper_Right(1, -1),
        Left(-1, 0), Center(0, 0), Right(1, 0),
        Lower_Left(-1, 1), Bottom(0, 1),Lower_Right(1, 1);

        public int x, y;
        Alignment(int x, int y)
        {
            this.x = x;
            this.y = y;
        }
    }
    private Alignment alignment;

    public MenuLabel(double x, double y)
    {
        posX = x; posY = y;
        color = new Color(0, 0, 0);
        font = new Font("Arial", Font.PLAIN, 20);
        SetText("Label");
        alignment = Alignment.Center;
    }
    public MenuLabel(Point2D.Double point)
    {
        this(point.x, point.y);
    }

    public void SetPosition(double x, double y)
    {
        posX = x;
        posY = y;
    }
    public void SetColor(Color color)
    {
        this.color = color;
    }
    public void SetFont(Font font)
    {
        this.font = font;
        calculateSize();
    }
    private void split()
    {
        ArrayList<String> strings = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for(int i = 0; i < text.length(); i++)
        {
            if(text.charAt(i) == '\n')
            {
                strings.add(current.toString());
                current = new StringBuilder();
            }
            else
                current.append(text.charAt(i));
        }
        strings.add(current.toString());
        lines = strings.toArray(String[]::new);
    }
    public void SetText(String text)
    {
        if(text == null)
            text = "";
        this.text = text;
        split();
        calculateSize();
    }
    private void calculateSize()
    {
        linesHeight = new double[lines.length];
        linesWidth = new double[lines.length];
        FontRenderContext frc = new FontRenderContext(null, true, true);
        width = 0; height = 0;
        for(int i = 0; i < lines.length; i++)
        {
            if(lines[i].length() == 0)
            {
                linesWidth[i] = 0;
                continue;
            }
            LineMetrics metrics = font.getLineMetrics(lines[i], frc);
            TextLayout tx = new TextLayout(lines[i], font, frc);
            fontHeight = metrics.getHeight();
            linesWidth[i] = tx.getBounds().getWidth();
            width = Math.max(width, linesWidth[i]);
        }
        height = lines.length*fontHeight;
        for(int i = 0; i < lines.length; i++) {
            linesHeight[i] = fontHeight;
        }
    }
    public double GetFontHeight()
    {
        return fontHeight;
    }
    public void SetAlignment(Alignment alignment)
    {
        this.alignment = alignment;
    }

    public double GetHeight()
    {
        return height;
    }
    public Point2D.Double GetCarrotPosition(int index)
    {
        if(text == null || text.equals("") || index == 0)
            return new Point2D.Double(0, -(1+alignment.y)/2.0*height);
        int line = 0;
        double posY = 0;
        while(line < lines.length && index > lines[line].length())
        {
            index -= lines[line].length()+1;
            posY += linesHeight[line];
            line++;
        }
        if(line == lines.length) {
            line = lines.length - 1;
            index = lines[line].length();
            posY -= linesHeight[line];
        }
        if(lines[line].length() == 0)
        {
            return new Point2D.Double(-(alignment.x+1)/2.0*linesWidth[line], posY-(1+alignment.y)/2.0*height);
        }
        TextHitInfo info = TextHitInfo.leading(index);
        FontRenderContext frc = new FontRenderContext(null, true, true);
        TextLayout tx = new TextLayout(lines[line], font, frc);
        double posX = tx.getCaretInfo(info)[0];
        posX -= (alignment.x+1)/2.0*linesWidth[line];
        posY -= (alignment.y+1)/2.0*height;
        return new Point2D.Double(posX, posY);
    }
    public int GetCaret(double x, double y)
    {
        int line = 0, position = 0;
        y -= posY-(alignment.y+1)/2.0*height;
        while(line < lines.length-1 && y > linesHeight[line])
        {
            y -= linesHeight[line];
            position += lines[line].length()+1;
            line++;
        }
        if(lines[line] == null || lines[line].equals(""))
            return 0;
        FontRenderContext frc = new FontRenderContext(null, true, true);
        TextLayout tx = new TextLayout(lines[line], font, frc);
        double ofs = posX-(alignment.x+1)/2.0*linesWidth[line];
        int l = 0, r = lines[line].length();
        while(l < r)
        {
            int m = (l+r+1) / 2;
            TextHitInfo info = TextHitInfo.leading(m);
            if(tx.getCaretInfo(info)[0]+ofs > x)
                r = m-1;
            else
                l = m;
        }
        return position+l;
    }

    @Override
    public void Tick(double delta) {

    }

    @Override
    public void Render(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;

        if(text == null || text.equals(""))
            return;
        g2d.setColor(color);
        g2d.setFont(font);
        float x = (float)posX, y = (float)(posY-(alignment.y + 1) * height / 2.0);
        for(int i = 0; i < lines.length; i++) {
            double X = x-(alignment.x + 1) * linesWidth[i] / 2.0;
            y += linesHeight[i];
            g2d.drawString(lines[i], (float)X, y);

        }
    }
}
