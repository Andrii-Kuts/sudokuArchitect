package main;

import main.ui.*;
import main.ui.Button;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

public class ObjectsHandler extends RenderObject
{
    private ArrayList<RenderObject> objects;
    private ArrayList<RenderObject> buttons;
    private Queue<RenderObject> changeQueue;
    protected Button draggedObject;
    private MenuLabel fpsLabel;
    private DecimalFormat format = new DecimalFormat("###", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    private long st = 0;

    private static final boolean DEBUG = false;

    public ObjectsHandler()
    {
        objects = new ArrayList<>();
        buttons = new ArrayList<>();
        changeQueue = new ArrayDeque<>();
        draggedObject = null;

        if(DEBUG) {
            fpsLabel = new MenuLabel(5, 5);
            fpsLabel.SetColor(new Color(0, 0, 0));
            fpsLabel.SetAlignment(MenuLabel.Alignment.Left);
        }
    }

    public void Tick(double delta)
    {
        for(RenderObject ro : objects)
            ro.Tick(delta);
    }

    public void Render(Graphics g) {
        for (RenderObject ro : objects) {
            if (ro.GetVisibility())
                ro.Render(g);
        }
        if (DEBUG){
            long tm = System.currentTimeMillis() - st;
            fpsLabel.SetText(format.format(1000.0 / tm));
            fpsLabel.Render(g);
            st = System.currentTimeMillis();
        }
    }

    public void AddObject(RenderObject obj)
    {
        if(obj == null)
            return;
        obj.SetAddTag(1);
        changeQueue.add(obj);
    }
    public void UpdateObject(RenderObject obj)
    {
        if(obj == null)
            return;
        obj.SetAddTag(2);
        changeQueue.add(obj);
    }
    public void RemoveObject(RenderObject obj)
    {
        if(obj == null)
            return;
        obj.SetAddTag(0);
        changeQueue.add(obj);
    }

    public void ClearObjects(RenderObject obj)
    {
        for(RenderObject ro : objects)
        {
            ro.SetAddTag(0);
            changeQueue.add(ro);
        }
    }

    public void PushChange()
    {
        boolean changed = false, changedButtons = false;
        while(!changeQueue.isEmpty())
        {
            RenderObject ro = changeQueue.poll();
            boolean btn = (ro instanceof main.ui.Button) || (ro instanceof ObjectsHandler);
            if(ro.GetAddTag() == 1) {
                ro.SetHandler(this);
                objects.add(ro);
                if(btn) {
                    buttons.add(ro);
                    changedButtons = true;
                }
                changed = true;
            }
            else if(ro.GetAddTag() == 2) {
                objects.remove(ro);
                if(btn)
                    buttons.remove(ro);
                ro.ConfirmRenderPriority();
                objects.add(ro);
                if(btn) {
                    buttons.add(ro);
                    changedButtons = true;
                }
                changed = true;
            }
            else {
                ro.SetHandler(null);
                objects.remove(ro);
                if(btn)
                    buttons.remove(ro);
            }
        }
        if(changed)
            objects.sort(Comparator.naturalOrder());
        if(changedButtons)
            buttons.sort(Comparator.naturalOrder());
    }

    public void SetDrag(Button drag)
    {
        draggedObject = drag;
    }

    public boolean UpdateMouse(float x, float y, int clickMask)
    {
        return UpdateMouse(x, y, clickMask, false);
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
        for(int i = buttons.size()-1; i >= 0; i--)
        {
            RenderObject ro = buttons.get(i);
            if(ro.GetVisibility())
            {
                if(ro instanceof ObjectsHandler)
                {
                    ObjectsHandler oh = (ObjectsHandler)ro;
                    hit = oh.UpdateMouse(x, y, clickMask, hit);
                }
                else {
                    Button btn = (Button)ro;
                    if (hit || !btn.Intersects(x, y)) {
                        btn.OnMouseLeave();
                    } else {
                        btn.OnMouseHover(x, y);
                        for (int click = 0; click <= 2; click++) {
                            if ((clickMask & (1 << click)) > 0)
                                btn.OnClick(x, y, click);
                            else
                                btn.OnRelease(x, y, click);
                        }
                        hit = true;
                    }
                }
            }
        }
        return hit;
    }
}
