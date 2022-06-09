package main.ui.graphics;

import main.ui.RenderObject;
import main.ui.UITransform;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class UIRect extends RenderObject {
    private double posX, posY, sizeX, sizeY, roundness;
    private Color color;

    public UIRect(double x, double y, double width, double height) {
        posX = x;
        posY = y;
        sizeX = width;
        sizeY = height;
        roundness = 10;

        color = new Color(193, 193, 193);
    }
    public UIRect()
    {
        this(0, 0, 0, 0);
    }
    public UIRect(UITransform transform)
    {
        this();
        SetTransform(transform);
    }

    public void SetTransform(UITransform transform)
    {
        posX = transform.x;
        posY = transform.y;
        sizeX = transform.width;
        sizeY = transform.height;
    }
    public void SetRoundness(double value) {
        roundness = value;
    }
    public void SetColor(Color color){
        this.color = color;
    }

    public void Tick(double delta) {

    }
    public void Render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        RoundRectangle2D.Double rect = new RoundRectangle2D.Double(posX - sizeX/2.0, posY - sizeY/2.0, sizeX, sizeY, roundness, roundness);
        g2d.setColor(color);
        g2d.fill(rect);
    }
}
