package main.sudoku;

import main.ui.RenderObject;
import main.ui.UITransform;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

public class UISelector extends RenderObject
{
    private double posX, posY, sizeX, sizeY, thickness, padding;
    private Color color;

    public UISelector(double x, double y, double width, double height)
    {
        posX = x; posY = y;
        sizeX = width;
        sizeY = height;

        color = new Color(27, 42, 92);

        thickness = 5;
        padding = 5;
    }

    public void SetTransform(UITransform transform)
    {
        posX = transform.x;
        posY = transform.y;
        sizeX = transform.width;
        sizeY = transform.height;
    }

    public void SetColor(Color color)
    {
        this.color = color;
    }

    public void SetRoundness(double thickness, double padding)
    {
        this.thickness = thickness;
        this.padding = padding;
    }

    @Override
    public void Tick(double delta) {

    }

    @Override
    public void Render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));

        Area rectSmall = new Area(new RoundRectangle2D.Double(posX - sizeX / 2.0, posY - sizeY / 2.0,
                sizeX, sizeY, padding, padding) {
        });
        Area rectBig = new Area(new RoundRectangle2D.Double(posX-thickness - sizeX / 2.0, posY-thickness - sizeY / 2.0,
                sizeX+(thickness)*2, sizeY+(thickness)*2, padding+thickness, padding+thickness) {
        });
        rectBig.subtract(rectSmall);
        g2d.setColor(color);
        g2d.fill(rectBig);
    }
}
