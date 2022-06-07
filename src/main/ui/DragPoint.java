package main.ui;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class DragPoint extends DragRectangle
{
    protected Color idleColor, hoverColor, pressColor, currentColor;
    private boolean[] isPressed;
    private boolean isHovered;
    private double thickness;

    public DragPoint()
    {
        super();
        isHovered = false;
        isPressed = new boolean[]{false, false, false};

        idleColor = new Color(255, 255, 255);
        hoverColor = new Color(106, 159, 245);
        pressColor = new Color(38, 60, 212);

        currentColor = new Color(idleColor.getRGB());
    }

    public void SetColors(Color idleColor, Color hoverColor, Color pressColor)
    {
        this.idleColor = idleColor;
        this.hoverColor = hoverColor;
        this.pressColor = pressColor;
    }
    public void SetThickness(double thickness)
    {
        this.thickness = thickness;
    }

    public void OnMouseHover(float x, float y) {
        super.OnMouseHover(x, y);
        isHovered = true;
    }
    public void OnMouseLeave() {
        super.OnMouseLeave();
        isHovered = false;
        isPressed[0] = isPressed[1] = isPressed[2] = false;
    }
    public void OnClick(float x, float y, int button) {
        super.OnClick(x, y, button);
        isPressed[button] = true;
    }
    public void OnRelease(float x, float y, int button) {
        super.OnRelease(x, y, button);
        isPressed[button] = false;
    }
    public boolean Intersects(float x, float y) {
        return super.Intersects(x, y);
    }

    public void Tick(double delta)
    {
        super.Tick(delta);
        if(isPressed[0])
            currentColor = pressColor;
        else if(isHovered)
            currentColor = hoverColor;
        else
            currentColor = idleColor;
    }
    public void Render(Graphics g)
    {
        super.Render(g);

        Graphics2D g2d = (Graphics2D)g;
        Rectangle2D.Double rect = new Rectangle2D.Double(posX-sizeX/2.0+thickness, posY-sizeY/2.0+thickness, sizeX-thickness*2.0, sizeY-thickness*2.0);
        g2d.setColor(currentColor);
        g2d.fill(rect);
    }
}
