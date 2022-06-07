package main.ui;

import main.ObjectsHandler;

import java.awt.*;
import java.util.Random;

public abstract class RenderObject implements Comparable<RenderObject>
{
    private int renderPriority, nextPriority, addElement;
    private boolean visible, draw = true;
    private int id;
    private static int counter;
    private ObjectsHandler handler;

    public RenderObject()
    {
        id = counter++;
        renderPriority = nextPriority = 0;
        visible = true;
    }

    public abstract void Tick(double delta);
    public abstract void Render(Graphics g);

    public void SetRenderPriority(int newPriority){
        nextPriority = newPriority;
        if(handler != null) {
            handler.UpdateObject(this);
        }
        else {
            renderPriority = nextPriority;
        }
    }
    public void ConfirmRenderPriority()
    {
        renderPriority = nextPriority;
    }
    public int GetRenderPriority()
    {
        return renderPriority;
    }
    public int GetPreviousPriority()
    {
        return nextPriority;
    }
    public void SetVisibility(boolean visibility) {
        visible = visibility;
    }
    public boolean GetVisibility() {
        return visible;
    }
    public void SetAddTag(int newTag) {
        addElement = newTag;
    }
    public int GetAddTag() {
        return addElement;
    }
    public void SetHandler(ObjectsHandler handler)
    {
        this.handler = handler;
    }
    public void SetDraw(boolean state)
    {
        draw = state;
    }
    public boolean GetDraw()
    {
        return draw;
    }

    @Override
    public int compareTo(RenderObject obj)
    {
        if(renderPriority < obj.renderPriority)
            return -1;
        if(renderPriority > obj.renderPriority)
            return 1;
        if(id < obj.id)
            return -1;
        if(id > obj.id)
            return 1;
        return 0;
    }

}
