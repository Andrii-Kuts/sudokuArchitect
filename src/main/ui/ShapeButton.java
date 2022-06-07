package main.ui;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

public class ShapeButton extends MenuButton
{
    private Shape shape;
    private Color shapeColor;

    public ShapeButton(double x, double y, double width, double height, double roundness)
    {
        super(x, y, width, height, roundness);
        shape = null;
        shapeColor = null;
    }

    public void SetShape(Shape shape)
    {
        if(shape == null){
            this.shape = null;
            return;
        }
        Area a = new Area(shape);
        a.transform(AffineTransform.getTranslateInstance(posX, posY));
        this.shape = a;
    }
    public void SetShapeColor(Color color){
        shapeColor = color;
    }

    public void Render(Graphics g)
    {
        super.Render(g);
        if(shape != null)
        {
            Graphics2D g2d = (Graphics2D) g;
            Color color = textColor;
            if(shapeColor != null)
                color = shapeColor;
            g2d.setColor(color);
            g2d.fill(shape);
        }
    }
}
