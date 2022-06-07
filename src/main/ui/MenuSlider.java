package main.ui;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.function.Consumer;

public class MenuSlider extends RenderObject implements Button, DragHost
{
    private double pos1x, pos1y, pos2x, pos2y, thickness, circleSize, value;
    private Point2D.Double differenceVector;
    private double pos1Projection, pos2Projection;
    private Color color;
    private DragCircle square;
    private Consumer<Double> action;

    public MenuSlider(double pos1x, double pos1y, double pos2x, double pos2y)
    {
        this.pos1x = pos1x;
        this.pos1y = pos1y;
        this.pos2x = pos2x;
        this.pos2y = pos2y;
        UpdateVectors();

        thickness = 10.0;
        circleSize = 20.0;
        value = 0;
        color = new Color(132, 132, 132);

        square = new DragCircle();
        square.SetHost(this);
        ChangePos(0, 0);
    }

    public void SetPosition(double pos1x, double pos1y, double pos2x, double pos2y)
    {
        this.pos1x = pos1x;
        this.pos1y = pos1y;
        this.pos2x = pos2x;
        this.pos2y = pos2y;
        UpdateVectors();
    }
    public void SetThickness(double lineThickness, double dotsSize)
    {
        thickness = lineThickness;
        circleSize = dotsSize;
    }
    public void SetColor(Color color)
    {
        this.color = color;
    }
    public void SetAction(Consumer<Double> action)
    {
        this.action = action;
    }
    public DragCircle GetDrag()
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
        value = differenceVector.x*x + differenceVector.y*y;
        value = (value-pos1Projection)/(pos2Projection-pos1Projection);
        if(value < 0)
            value = 0;
        if(value > 1)
            value = 1;
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

        Line2D line = new Line2D.Double(pos1x, pos1y, pos2x, pos2y);
        Stroke stroke = new BasicStroke((float)thickness, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
        g2d.setStroke(stroke);
        g2d.setColor(color);
        g2d.draw(line);

        Ellipse2D.Double dot1 = new Ellipse2D.Double(pos1x-circleSize/2.0, pos1y-circleSize/2.0, circleSize, circleSize);
        g2d.fill(dot1);
        Ellipse2D.Double dot2 = new Ellipse2D.Double(pos2x-circleSize/2.0, pos2y-circleSize/2.0, circleSize, circleSize);
        g2d.fill(dot2);

        square.Render(g);
    }


}
