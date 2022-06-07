package main.ui;

import java.util.ArrayList;

public class MenuGroup
{
    private ArrayList<RenderObject> objects;

    public MenuGroup()
    {
        objects = new ArrayList<>();
    }
    public MenuGroup(RenderObject ro)
    {
        this();
        objects.add(ro);
    }

    public void AddObject(RenderObject ro)
    {
        objects.add(ro);
    }
    public void RemoveObject(RenderObject ro)
    {
        objects.remove(ro);
    }
    public void SetVisible(boolean value)
    {
        for(RenderObject ro : objects)
        {
            ro.SetVisibility(value);
        }
    }
    public void SetRenderPriority(int prior)
    {
        for(RenderObject ro : objects)
        {
            ro.SetRenderPriority(prior);
        }
    }
}
