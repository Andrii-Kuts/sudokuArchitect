package main.ui.graphics;

import main.ui.RenderObject;

import java.awt.*;
import java.awt.geom.Line2D;

public class UILine extends RenderObject {
    private double pos1x, pos1y, pos2x, pos2y, thickness;
    private Color color;

    public UILine(double pos1x, double pos1y, double pos2x, double pos2y) {
        this.pos1x = pos1x;
        this.pos1y = pos1y;
        this.pos2x = pos2x;
        this.pos2y = pos2y;

        thickness = 5;
        color = new Color(10, 10, 10);
    }
    public UILine()
    {
        this(0, 0, 0, 0);
    }

    public void SetPosition(double pos1x, double pos1y, double pos2x, double pos2y)
    {
        this.pos1x = pos1x;
        this.pos1y = pos1y;
        this.pos2x = pos2x;
        this.pos2y = pos2y;
    }
    public void SetPointPosition(int index, double x, double y)
    {
        if(index == 0) {
            this.pos1x = x;
            this.pos1y = y;
        }
        else {
            this.pos2x = x;
            this.pos2y = y;
        }
    }
    public void SetThickness(double val)
    {
        thickness = val;
    }
    public void SetColor(Color color)
    {
        this.color = color;
    }

    public void Render(Graphics g)
    {
        Graphics2D g2d = (Graphics2D)g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(color);
        Line2D line = new Line2D.Double(pos1x, pos1y, pos2x, pos2y);
        Stroke stroke = new BasicStroke((float)thickness, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
        g2d.setStroke(stroke);
        g2d.draw(line);
    }

    public void Tick(double delta)
    {

    }
}
