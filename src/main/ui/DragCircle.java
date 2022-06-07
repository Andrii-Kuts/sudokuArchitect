package main.ui;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

public class DragCircle extends RenderObject implements Button
{
    private double posX, posY, size, dx, dy;
    private Color color;
    private Runnable clickAction, releaseAction;
    private DragHost host;
    private boolean selected;

    public DragCircle()
    {
        posX = 100; posY = 100;
        dx = 0; dy = 0;
        size = 100;
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
    public void SetSize(double size)
    {
        this.size = size;
    }

    @Override
    public void OnMouseHover(float x, float y) {

    }

    @Override
    public void OnMouseLeave() {

    }

    @Override
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

    @Override
    public void OnRelease(float x, float y, int button) {
        if(button != 0)
            return;
        selected = false;
        releaseAction.run();
    }

    @Override
    public boolean Intersects(float x, float y) {
        return (posX - x) * (posX - x) + (posY - y) * (posY - y) <= size * size / 4.0;
    }

    @Override
    public void Tick(double delta) {

    }

    @Override
    public void Render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Ellipse2D.Double rect = new Ellipse2D.Double(posX-size/2.0, posY-size/2.0, size, size);
        g2d.setColor(color);
        g2d.fill(rect);
    }
}
