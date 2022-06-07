package main.ui;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.function.Consumer;

public class MenuScroller extends RenderObject implements Button, DragHost
{
    private double pos1x, pos1y, pos2x, pos2y, posX, posY, sizeX, sizeY, value, squareSize;
    private boolean direction;
    private Point2D.Double differenceVector;
    private double pos1Projection, pos2Projection;
    private Color color;
    private DragRectangle square;
    private Consumer<Double> action;

    public MenuScroller(double posX, double posY, double sizeX, double sizeY)
    {
        this.posX = posX;
        this.posY = posY;
        this.sizeX = sizeX;
        this.sizeY = sizeY;

        //False : Vertical
        //True : Horizontal
        direction = false;
        squareSize = sizeY/2.0;

        value = 0;
        color = new Color(255, 0, 0);

        square = new DragRectangle();
        square.SetHost(this);
        UpdateSize();
        ChangePos(0, 0);
    }
    public MenuScroller()
    {
        this(0, 0, 10, 10);
    }

    public void SetPosition(double x, double y, double width, double height)
    {
        if(!direction) {
            squareSize = squareSize/sizeY*height;
        }
        else {
            squareSize = squareSize/sizeX*width;
        }
        posX = x;
        posY = y;
        sizeX = width;
        sizeY = height;
        UpdateSize();
        ChangePos(0, 0);
    }
    public void SetColor(Color color)
    {
        this.color = color;
    }
    public void SetAction(Consumer<Double> action)
    {
        this.action = action;
    }
    public DragRectangle GetDrag()
    {
        return square;
    }
    public double GetValue()
    {
        return value;
    }
    public void SetValue(double value)
    {
        this.value = value;
        SetDragPos();
    }
    private void UpdateSize()
    {
        if(direction)
        {
            pos1y = pos2y = posY;
            pos1x = posX+(squareSize-sizeX)/2.0;
            pos2x = posX-(squareSize-sizeX)/2.0;
            square.SetSize(squareSize, sizeY);
        }
        else
        {
            pos1x = pos2x = posX;
            pos1y = posY+(squareSize-sizeY)/2.0;
            pos2y = posY-(squareSize-sizeY)/2.0;
            square.SetSize(sizeX, squareSize);
        }
        UpdateVectors();
        ChangePos(0, 0);
    }
    public void SetSize(double size)
    {
        if(size < 0)
            size = 0;
        if(!direction) {
            if(size > sizeY)
                size = sizeY;
        }
        else {
            if(size > sizeX)
                size = sizeX;
        }
        squareSize = size;
        UpdateSize();
    }
    private void UpdateVectors()
    {
        differenceVector = new Point2D.Double(pos2x-pos1x, pos2y-pos1y);
        pos1Projection = differenceVector.x*pos1x + differenceVector.y*pos1y;
        pos2Projection = differenceVector.x*pos2x + differenceVector.y*pos2y;
    }

    private void SetDragPos()
    {
        square.SetPos(pos1x*(1-value) + pos2x*value, pos1y*(1-value) + pos2y*value);
    }
    public void ChangePos(double x, double y)
    {
        if(Math.abs(pos1Projection - pos2Projection) > 0.0001) {
            value = differenceVector.x * x + differenceVector.y * y;
            value = (value - pos1Projection) / (pos2Projection - pos1Projection);
            if (value < 0)
                value = 0;
            if (value > 1)
                value = 1;
        }
        else
            value = 0;
        if(action != null)
            action.accept(value);
        SetDragPos();
    }

    public void OnMouseHover(float x, float y) {
        square.OnMouseHover(x, y);
    }
    public void OnMouseLeave() {
        square.OnMouseLeave();
    }
    public void OnClick(float x, float y, int button) {
        square.OnClick(x, y, button);
    }
    public void OnRelease(float x, float y, int button) {
        square.OnRelease(x, y, button);
    }
    public boolean Intersects(float x, float y) {
        return square.Intersects(x, y);
    }

    public void Tick(double delta) {
        square.Tick(delta);
    }
    public void Render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Rectangle2D.Double rect = new Rectangle2D.Double(posX-sizeX/2.0, posY-sizeY/2.0, sizeX, sizeY);
        g2d.setColor(color);
        g2d.fill(rect);

        square.Render(g);
    }


}
