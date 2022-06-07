package main.ui;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class MenuPanel extends RenderObject implements Button {
    private double posX, posY, sizeX, sizeY, roundness;
    private int width, height;
    private Color color, backgroundColor;
    private boolean isPanel;

    public MenuPanel(double x, double y, double width, double height) {
        posX = x;
        posY = y;
        sizeX = width;
        sizeY = height;

        roundness = 10;

        color = new Color(77, 171, 217);
        backgroundColor = new Color(3, 3, 3, 131);

        this.width = 1920;
        this.height = 1080;
        isPanel = false;
    }

    public MenuPanel() {
        this(0, 0, 0, 0);
    }
    public MenuPanel(UITransform transform)
    {
        this();
        SetTransform(transform);
    }

    public void SetTransform(double x, double y, double width, double height) {
        posX = x;
        posY = y;
        sizeX = width;
        sizeY = height;
    }

    public void SetTransform(UITransform transform) {
        posX = transform.x;
        posY = transform.y;
        sizeX = transform.width;
        sizeY = transform.height;
    }

    public void SetRoundness(double value) {
        roundness = value;
    }

    public void SetColor(Color color){
        this.color = color;
    }
    public void SetBackgroundColor(Color color){
        backgroundColor = color;
    }
    public void SetScreenSize(int width, int height){
        this.width = width;
        this.height = height;
    }
    public void SetPanel(boolean isPanel){
        this.isPanel = isPanel;
    }

    public void OnMouseHover(float x, float y) {

    }
    public void OnMouseLeave() {

    }
    public void OnClick(float x, float y, int button) {

    }
    public void OnRelease(float x, float y, int button) {

    }
    public boolean Intersects(float x, float y) {
        if(isPanel)
            return true;
        if(x < posX-sizeX/2.0 || x > posX+sizeX/2.0 || y < posY-sizeY/2.0 || y > posY+sizeY/2.0)
            return false;
        return true;
    }


    public void Tick(double delta) {

    }
    public void Render(Graphics g)
    {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if(isPanel)
        {
            g2d.setColor(backgroundColor);
            g2d.fillRect(0, 0, width, height);
        }
        RoundRectangle2D.Double rect = new RoundRectangle2D.Double(posX-sizeX/2.0, posY-sizeY/2.0, sizeX, sizeY, roundness, roundness);
        g2d.setColor(color);
        g2d.fill(rect);
    }
}
