package main.ui;

public class UITransform
{
    public double x, y, width, height;

    public UITransform()
    {
        x = y = width = height = 0;
    }

    public UITransform(double x, double y, double width, double height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}
