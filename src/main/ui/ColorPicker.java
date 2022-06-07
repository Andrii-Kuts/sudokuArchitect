package main.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.BiConsumer;

public class ColorPicker extends RenderObject implements Button
{
    private int posX, posY, selX, selY, width, height;
    private boolean drawX, drawY, selected = false;
    private BufferedImage image;
    private BiConsumer<Double, Double> action;
    private Runnable selectAction, deselectAction;

    public ColorPicker()
    {
        posX = posY = 0;
        width = height =0;
        image = null;
        selX = selY = 0;
        drawX = drawY = false;
        action = (x, y) -> {};
    }
    public ColorPicker(double x, double y)
    {
        this();
        posX = (int)x;
        posY = (int)y;
    }

    public void SetImage(BufferedImage image) {
        this.image = image;
    }
    public void SetPos(double x, double y) {
        posX = (int)x;
        posY = (int)y;
    }
    public void SetTransform(UITransform transform)
    {
        posX = (int)transform.x;
        posY = (int)transform.y;
        width = (int)transform.width;
        height = (int)transform.height;
    }
    public void SetAction(BiConsumer<Double, Double> action)
    {
        this.action = action;
    }
    public void SetDragActions(Runnable selectAction, Runnable deselectAction)
    {
        this.selectAction = selectAction;
        this.deselectAction = deselectAction;
    }
    public void Select(double x, double y)
    {
        selX = (int)x;
        selY = (int)y;
    }
    public void SetDraw(boolean x, boolean y)
    {
        drawX = x;
        drawY = y;
    }

    public Point GetSelect()
    {
        return new Point(selX, selY);
    }

    public void OnMouseHover(float x, float y) {

    }
    public void OnMouseLeave() {

    }
    public void OnClick(float x, float y, int button) {
        if(button != 0)
            return;
        if(!selected)
        {
            selected = true;
            selectAction.run();
        }
        selX = Math.max(Math.min((int)x-posX, width), 0);
        selY = Math.max(Math.min((int)y-posY, height), 0);
        action.accept((double)selX, (double)selY);
    }
    public void OnRelease(float x, float y, int button) {
        if(button != 0)
            return;
        if(selected)
        {
            selected = false;
            deselectAction.run();
        }
    }
    public boolean Intersects(float x, float y) {
        if(x < posX || x > posX+width || y < posY || y > posY+height)
            return false;
        return true;
    }

    public void Tick(double delta) {

    }
    public void Render(Graphics g) {
        g.setColor(new Color(255, 255, 255));
        g.fillRect(posX, posY, width, height);
        if(image != null)
            g.drawImage(image, posX, posY, width, height, null);
        Graphics2D g2d = (Graphics2D)g;
        if (drawX)
        {
            Rectangle rect = new Rectangle(posX+selX-1, posY, 2, height);
            g2d.setColor(new Color(0, 0, 0));
            g2d.fill(rect);
        }
        if (drawY)
        {
            Rectangle rect = new Rectangle(posX, posY+selY-1, width, 2);
            g2d.setColor(new Color(0, 0, 0));
            g2d.fill(rect);
        }
    }
}
