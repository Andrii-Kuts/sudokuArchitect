package main.ui.graphics;

import main.ui.RenderObject;
import main.ui.UITransform;

import java.awt.*;

public class UIImage extends RenderObject {
    private Image image;
    private double posX, posY, sizeX, sizeY;

    public UIImage(Image image)
    {
        posX = posY = 0;
        if(image != null) {
            sizeX = image.getWidth(null);
            sizeY = image.getHeight(null);
            posX = sizeX/2.0;
            posY = sizeY/2.0;
        }
        this.image = image;
    }

    public UIImage()
    {
        this(null);
    }

    private void rescaleImage()
    {
        if(image == null)
            return;
        double scX = sizeX/image.getWidth(null), scY = sizeY/image.getHeight(null);
        if(scY < scX)
            scX = scY;
        image = image.getScaledInstance((int)(scX*image.getWidth(null)), (int)(scX* image.getHeight(null)), Image.SCALE_SMOOTH);
    }

    public void ChangeImage(Image image)
    {
        this.image = image;
        sizeX = image.getWidth(null);
        sizeY = image.getHeight(null);
        posX = sizeX/2.0;
        posY = sizeY/2.0;
    }

    public void SetImage(Image image)
    {
        this.image = image;
        rescaleImage();
    }

    public void SetTransform(UITransform transform)
    {
        posX = transform.x;
        posY = transform.y;
        sizeX = transform.width;
        sizeY = transform.height;
        rescaleImage();
    }
    @Override
    public void Tick(double delta) {

    }

    @Override
    public void Render(Graphics g) {
        if(image != null)
            g.drawImage(image, (int)(posX-sizeX/2.0), (int)(posY-sizeY/2.0),null);
    }
}
