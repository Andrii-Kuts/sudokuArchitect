package main.ui;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public class MenuGraphic extends RenderObject implements Button{

    private double posX, posY, sizeX, sizeY, roundness, padding;
    private Color color;
    private MenuLabel nameLabel;
    private MenuButton editButton, toggleButton, deleteButton;

    private RoundRectangle2D.Double rect;

    public MenuGraphic(double x, double y, double width, double height)
    {
        posX = x;
        posY = y;
        sizeX = width;
        sizeY = height;
        color = new Color(0, 0, 0);

        roundness = 10.0;
        rect = new RoundRectangle2D.Double(posX-sizeX/2.0, posY-sizeY/2.0, sizeX, sizeY, roundness, roundness);
        padding = 5.0;

        nameLabel = new MenuLabel(x-width/2.0, y);
        nameLabel.SetAlignment(MenuLabel.Alignment.Left);

        editButton = new MenuButton(x+width/2.0-height*2.5, y, height, height, roundness);
        toggleButton = new MenuButton(x+width/2.0-height*1.5, y, height, height, roundness);
        deleteButton = new MenuButton(x+width/2.0-height*0.5, y, height, height, roundness);
    }
    public MenuGraphic(UITransform transform)
    {
        this(transform.x, transform.y, transform.width, transform.height);
    }
    public MenuGraphic()
    {
        this(0, 0, 0, 0);
    }

    public void SetPos(double x, double y, double width, double height)
    {
        posX = x;
        posY = y;
        sizeX = width;
        sizeY = height;
        rect = new RoundRectangle2D.Double(posX-sizeX/2.0, posY-sizeY/2.0, sizeX, sizeY, roundness, roundness);
        nameLabel.SetPosition(posX-sizeX/2.0+padding, posY);
        editButton.SetTransform(x+width/2.0-height*2.5+padding, y, height-padding*2, height-padding*2);
        toggleButton.SetTransform(x+width/2.0-height*1.5, y, height-padding*2, height-padding*2);
        deleteButton.SetTransform(x+width/2.0-height*0.5, y, height-padding*2, height-padding*2);
    }
    public void SetPos(UITransform transform) {
        SetPos(transform.x, transform.y, transform.width, transform.height);
    }
    public void SetPadding(double value)
    {
        padding = value;
        nameLabel.SetPosition(posX-sizeX/2.0+padding, posY);
        editButton.SetTransform(posX+sizeX/2.0-sizeY*2.5+padding, posY, sizeY-padding*2, sizeY-padding*2);
        toggleButton.SetTransform(posX+sizeX/2.0-sizeY*1.5, posY, sizeY-padding*2, sizeY-padding*2);
        deleteButton.SetTransform(posX+sizeX/2.0-sizeY*0.5, posY, sizeY-padding*2, sizeY-padding*2);
    }
    public void SetRoundness(double value) {
        roundness = value;
        rect = new RoundRectangle2D.Double(posX-sizeX/2.0, posY-sizeY/2.0, sizeX, sizeY, roundness, roundness);
    }
    public void SetColor(Color color) {
        this.color = color;
    }
    public MenuLabel GetLabel() {
        return nameLabel;
    }
    public MenuButton GetEditButton() {
        return editButton;
    }
    public MenuButton GetToggleButton() {
        return toggleButton;
    }
    public MenuButton GetDeleteButton() {
        return deleteButton;
    }

    public void Tick(double delta) {
        nameLabel.Tick(delta);
        editButton.Tick(delta);
        toggleButton.Tick(delta);
        deleteButton.Tick(delta);
    }

    @Override
    public void Render(Graphics g)
    {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(color);
        g2d.fill(rect);

        nameLabel.Render(g);
        editButton.Render(g);
        toggleButton.Render(g);
        deleteButton.Render(g);
    }

    public void OnMouseHover(float x, float y) {
        if(editButton.Intersects(x, y))
            editButton.OnMouseHover(x, y);
        else
            editButton.OnMouseLeave();

        if(toggleButton.Intersects(x, y))
            toggleButton.OnMouseHover(x, y);
        else
            toggleButton.OnMouseLeave();

        if(deleteButton.Intersects(x, y))
            deleteButton.OnMouseHover(x, y);
        else
            deleteButton.OnMouseLeave();
    }
    public void OnMouseLeave() {
        editButton.OnMouseLeave();
        toggleButton.OnMouseLeave();
        deleteButton.OnMouseLeave();
    }
    public void OnClick(float x, float y, int button) {
        if(editButton.Intersects(x, y))
            editButton.OnClick(x, y, button);
        else
            editButton.OnMouseLeave();

        if(toggleButton.Intersects(x, y))
            toggleButton.OnClick(x, y, button);
        else
            toggleButton.OnMouseLeave();

        if(deleteButton.Intersects(x, y))
            deleteButton.OnClick(x, y, button);
        else
            deleteButton.OnMouseLeave();
    }
    public void OnRelease(float x, float y, int button) {
        if(editButton.Intersects(x, y))
            editButton.OnRelease(x, y, button);
        else
            editButton.OnMouseLeave();

        if(toggleButton.Intersects(x, y))
            toggleButton.OnRelease(x, y, button);
        else
            toggleButton.OnMouseLeave();

        if(deleteButton.Intersects(x, y))
            deleteButton.OnRelease(x, y, button);
        else
            deleteButton.OnMouseLeave();
    }
    public boolean Intersects(float x, float y) {
        if(x < posX-sizeX/2.0 || x > posX+sizeX/2.0 || y < posY-sizeY/2.0 || y > posY+sizeY/2.0)
            return false;
        return true;
    }
}
