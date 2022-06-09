package main.ui;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

public class DragRectangle extends RenderObject implements Button
{
    protected double posX, posY, sizeX, sizeY, dx, dy;
    private Color color;
    private Runnable clickAction, releaseAction;
    private DragHost host;
    private boolean selected;

    public DragRectangle()
    {
        posX = 100; posY = 100;
        dx = 0; dy = 0;
        sizeX = 100;
        sizeY = 100;
        selected = false;
        color = new Color(35, 35, 35);
    }

    public void SetPos(double x, double y)
    {
        posX = x;
        posY = y;
    }
    public void SetActions(Runnable onClick, Runnable onRelease)
    {
        clickAction = onClick;
        releaseAction = onRelease;
    }
    public void SetHost(DragHost host)
    {
        this.host = host;
    }
    public void SetColor(Color color)
    {
        this.color = color;
    }
    public void SetSize(double width, double height)
    {
        sizeX = width;
        sizeY = height;
    }

    public void OnMouseHover(float x, float y) {

    }
    public void OnMouseLeave() {

    }
    public void OnClick(float x, float y, int button) {
        if(button != 0)
            return;
        if(selected)
        {
            if(host != null)
                host.ChangePos(x+dx, y+dy);
        }
        else
        {
            clickAction.run();
            dx = posX-x;
            dy = posY-y;
            selected = true;
        }
    }
    public void OnRelease(float x, float y, int button) {
        if(button != 0)
            return;
        selected = false;
        releaseAction.run();
    }
    public boolean Intersects(float x, float y) {
        if(x < posX-sizeX/2.0)
            return false;
        if(x > posX+sizeX/2.0)
            return false;
        if(y < posY-sizeY/2.0)
            return false;
        if(y > posY+sizeY/2.0)
            return false;
        return true;
    }

    @Override
    public void Tick(double delta) {

    }

    @Override
    public void Render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        Rectangle2D.Double rect = new Rectangle2D.Double(posX-sizeX/2.0, posY-sizeY/2.0, sizeX, sizeY);
        g2d.setColor(color);
        g2d.fill(rect);
    }
}
