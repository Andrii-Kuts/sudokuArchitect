package main.ui.graphics;

import main.ui.RenderObject;
import main.ui.UITransform;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;

public class UICellClue extends RenderObject
{
    private double posX, posY, sizeX, sizeY;
    private Shape shape, pattern;
    private String ULText, URText, LLText, LRText;
    private Font font;
    private Color shapeColor, textColor, patternColor;

    public UICellClue(double x,double y, double width, double height)
    {
        posX = x;
        posY = y;
        sizeX = width;
        sizeY = height;
        shape = null;
        ULText = URText = LLText = LRText = "";
        pattern = null;
        font = new Font("Arial", Font.PLAIN, 20);
        shapeColor = new Color(0, 0, 0);
        textColor = new Color(0, 0, 0);
    }
    public UICellClue(UITransform transform)
    {
        this(transform.x, transform.y, transform.width, transform.height);
    }

    public UICellClue()
    {
        this(0, 0, 0, 0);
    }

    public void SetTransform(double x, double y, double width, double height)
    {
        posX = x;
        posY = y;
        sizeX = width;
        sizeY = height;
    }
    public void SetTransform(UITransform transform)
    {
        SetTransform(transform.x, transform.y, transform.width, transform.height);
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
    public void SetTexts(String ul, String ur, String ll, String lr)
    {
        ULText = ul;
        URText = ur;
        LLText = ll;
        LRText = lr;
    }
    public void SetPattern(Shape image)
    {
        if(image == null){
            this.pattern = null;
            return;
        }
        Area a = new Area(image);
        a.transform(AffineTransform.getTranslateInstance(posX, posY));
        this.pattern = a;
    }
    public void SetFont(Font font)
    {
        this.font = font;
    }
    public void SetColors(Color symbolColor, Color textColor, Color patternColor)
    {
        shapeColor = symbolColor;
        this.textColor = textColor;
        this.patternColor = patternColor;
    }

    @Override
    public void Tick(double delta) {

    }

    @Override
    public void Render(Graphics g)
    {
        Graphics2D g2d = (Graphics2D) g;

        if(pattern != null)
        {
            g2d.setColor(patternColor);
            g2d.fill(pattern);
        }
        if(shape != null)
        {
            g2d.setColor(shapeColor);
            g2d.fill(shape);
        }
        if(!ULText.equals(""))
        {
            g2d.setColor(textColor);
            g2d.setFont(font);
            FontRenderContext frc = new FontRenderContext(null, false, false);
            TextLayout tx = new TextLayout(ULText, font, frc);
            int w = tx.getPixelBounds(frc, 0f, 0f).width, h = tx.getPixelBounds(frc, 0f, 0f).height;
            g2d.drawString(ULText, (float)(posX - sizeX/2.0)+5, (float)(posY+h+5 - (sizeY)/2.0));
        }
        if(!URText.equals(""))
        {
            g2d.setColor(textColor);
            g2d.setFont(font);
            FontRenderContext frc = new FontRenderContext(null, false, false);
            TextLayout tx = new TextLayout(URText, font, frc);
            int w = tx.getPixelBounds(frc, 0f, 0f).width, h = tx.getPixelBounds(frc, 0f, 0f).height;
            g2d.drawString(URText, (float)(posX + sizeX/2.0)-5-w, (float)(posY+h+5 - (sizeY)/2.0));
        }
        if(!LLText.equals(""))
        {
            g2d.setColor(textColor);
            g2d.setFont(font);
            FontRenderContext frc = new FontRenderContext(null, false, false);
            TextLayout tx = new TextLayout(LLText, font, frc);
            int w = tx.getPixelBounds(frc, 0f, 0f).width, h = tx.getPixelBounds(frc, 0f, 0f).height;
            g2d.drawString(LLText, (float)(posX - sizeX/2.0)+5, (float)(posY-5 + (sizeY)/2.0));
        }
        if(!LRText.equals(""))
        {
            g2d.setColor(textColor);
            g2d.setFont(font);
            FontRenderContext frc = new FontRenderContext(null, false, false);
            TextLayout tx = new TextLayout(LRText, font, frc);
            int w = tx.getPixelBounds(frc, 0f, 0f).width, h = tx.getPixelBounds(frc, 0f, 0f).height;
            g2d.drawString(LRText, (float)(posX + sizeX/2.0)-5-w, (float)(posY-5 + (sizeY)/2.0));
        }
    }
}
