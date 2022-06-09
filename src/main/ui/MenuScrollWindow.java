package main.ui;

import main.ObjectsHandler;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;

public class MenuScrollWindow extends ObjectsHandler
{
    private double posX, posY, sizeX, sizeY, roundness, padding, scrollThickness, objectsHeight;
    private Color backColor, frontColor;
    private MenuScroller scroller;

    private double offset;

    private RoundRectangle2D.Double rect, rect2;

    public MenuScrollWindow(double x, double y, double width, double height)
    {
        super();
        scrollThickness = 30.0;
        scroller = new MenuScroller();
        scroller.SetAction((val) -> SetOffset(val));
        scroller.GetDrag().SetActions(() -> SetDrag(scroller.GetDrag()), () -> SetDrag(null));
        SetPos(x, y, width, height);
        roundness = 10.0;
        padding = 5.0;

        backColor = new Color(116, 116, 116);
        frontColor = new Color(255, 255, 255);
        objectsHeight = height;

        rect = new RoundRectangle2D.Double(posX-sizeX/2.0, posY-sizeY/2.0, sizeX, sizeY, roundness, roundness);
        rect2 = new RoundRectangle2D.Double(posX-sizeX/2.0+padding, posY-sizeY/2.0+padding,
                sizeX-padding*2.0-scrollThickness, sizeY-padding*2.0, roundness, roundness);
    }
    public MenuScrollWindow(UITransform transform)
    {
        this(transform.x, transform.y, transform.width, transform.height);
    }

    public void SetPos(double x, double y, double width, double height)
    {
        posX = x;
        posY = y;
        sizeX = width;
        sizeY = height;
        scroller.SetPosition(x+width/2.0-scrollThickness/2.0, y, scrollThickness, sizeY);
        rect = new RoundRectangle2D.Double(posX-sizeX/2.0, posY-sizeY/2.0, sizeX, sizeY, roundness, roundness);
        rect2 = new RoundRectangle2D.Double(posX-sizeX/2.0+padding, posY-sizeY/2.0+padding,
                sizeX-padding*2.0-scrollThickness, sizeY-padding*2.0, roundness, roundness);
    }
    public void SetRoundness(double value)
    {
        roundness = value;
        rect = new RoundRectangle2D.Double(posX-sizeX/2.0, posY-sizeY/2.0, sizeX, sizeY, roundness, roundness);
        rect2 = new RoundRectangle2D.Double(posX-sizeX/2.0+padding, posY-sizeY/2.0+padding,
                sizeX-padding*2.0-scrollThickness, sizeY-padding*2.0, roundness, roundness);
    }
    public void SetPadding(double value){
        padding = value;
        rect2 = new RoundRectangle2D.Double(posX-sizeX/2.0+padding, posY-sizeY/2.0+padding,
                sizeX-padding*2.0-scrollThickness, sizeY-padding*2.0, roundness, roundness);
    }
    public void SetScrollThickness(double thickness)
    {
        scrollThickness = thickness;
        scroller.SetPosition(posX+sizeX/2.0-scrollThickness/2.0, posY, scrollThickness, sizeY);
        rect2 = new RoundRectangle2D.Double(posX-sizeX/2.0+padding, posY-sizeY/2.0+padding,
                sizeX-padding*2.0-scrollThickness, sizeY-padding*2.0, roundness, roundness);
    }
    private void setOffset(double offset)
    {
        this.offset = (objectsHeight-sizeY+2*padding)*offset;
    }
    public void SetOffset(double offset)
    {
        this.offset = (objectsHeight-sizeY+2*padding)*offset;
        scroller.SetValue(offset);
    }
    public void SetObjectsHeight(double height)
    {
        objectsHeight = height;
        scroller.SetSize((sizeY-2*padding)*sizeY / objectsHeight);
    }
    public void SetColors(Color backColor, Color frontColor)
    {
        this.backColor = backColor;
        this.frontColor = frontColor;
    }
    public MenuScroller GetScroller()
    {
        return scroller;
    }

    @Override
    public void Render(Graphics g)
    {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setColor(backColor);
        g2d.fill(rect);

        g2d.setColor(frontColor);
        g2d.fill(rect2);
        scroller.Render(g);
        Shape oldClip = g2d.getClip();
        g2d.clip(rect2);
        g2d.transform(AffineTransform.getTranslateInstance(0, -offset));
        super.Render(g);
        g2d.transform(AffineTransform.getTranslateInstance(0, offset));
        g2d.setClip(oldClip);
    }
    public void Tick(double delta)
    {
        PushChange();
        super.Tick(delta);
    }
    public boolean UpdateMouse(float x, float y, int clickMask, boolean hit)
    {
        if(draggedObject != null)
        {
            draggedObject.OnMouseHover(x, y);
            for(int click = 0; click <= 2; click++)
            {
                if((clickMask & (1 << click)) > 0)
                    draggedObject.OnClick(x, y, click);
                else
                    draggedObject.OnRelease(x, y, click);
                if(draggedObject == null)
                    break;
            }
            return true;
        }
        if (hit || !scroller.Intersects(x, y)) {
            scroller.OnMouseLeave();
        }
        else {
            scroller.OnMouseHover(x, y);
            for (int click = 0; click <= 2; click++) {
                if ((clickMask & (1 << click)) > 0)
                    scroller.OnClick(x, y, click);
                else
                    scroller.OnRelease(x, y, click);
            }
            hit = true;
        }
        if(x < posX-sizeX/2.0+padding || y < posY-sizeY/2.0+padding || x > posX+sizeX/2.0-padding-scrollThickness || y >  posY+sizeY/2.0-padding)
        {
            x = -1; y = -1;
        }
        hit = super.UpdateMouse(x, y+(float)offset, clickMask, hit);
        return hit;
    }
}
