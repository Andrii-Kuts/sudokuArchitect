package main.ui;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageButton extends MenuButton {
    private Image image;
    private double imageWidth, imageHeight;

    public ImageButton() {
        super();
        image = null;
        imageWidth = sizeX-roundness*2.0;
        imageHeight = sizeY-roundness*2.0;
    }

    public ImageButton(UITransform transform, double roundness)
    {
        super(transform, roundness);
        imageWidth = sizeX-roundness*2.0;
        imageHeight = sizeY-roundness*2.0;
    }

    public void SetImage(BufferedImage image)
    {
        this.image = image;
        this.image = this.image.getScaledInstance((int)imageWidth, (int)imageHeight, Image.SCALE_SMOOTH);
    }

    public void Render(Graphics g)
    {
        super.Render(g);
        g.drawImage(image, (int)(posX-imageWidth/2.0), (int)(posY-imageHeight/2.0), (int)imageWidth, (int)imageHeight, null);
    }
}
