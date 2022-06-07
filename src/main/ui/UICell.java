package main.ui;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class UICell extends RenderObject implements Button
{
    private double posX, posY, sizeX, sizeY;

    private Font font;
    private String mainText, cornerText, smallText;
    private Color textColor = new Color(23, 22, 22), selectionColor;
    private Color[] colors;
    private boolean isOutside = false;

    private static final float cornerCoefficient = 0.5f;
    private static final float setCoefficient = 0.6f;
    private static final float fontCoefficient = 0.5f;

    private Runnable action;
    private boolean isHovering;
    private boolean[] isPressed;

    private boolean isSelected;
    private double selectionWidth;

    public UICell(double posX, double posY, double width, double height)
    {
        this.posX = posX;
        this.posY = posY;
        sizeX = width;
        sizeY = height;

        mainText = cornerText = smallText = "";
        font = new Font("Franklin Gothic Book", Font.PLAIN, 36);

        colors = new Color[1];
        colors[0] = new Color(255, 255, 255, 255);

        isHovering = false;
        isPressed = new boolean[3];

        selectionColor = new Color(0, 0, 200);
        isSelected = false;

    }
    public UICell(UITransform transform)
    {
        this(transform.x, transform.y, transform.width, transform.height);
    }
    public void SetTexts(String main, String corner, String small)
    {
        mainText = main;
        cornerText = corner;
        smallText = small;
    }
    public void SetFont(Font font)
    {
        this.font = font.deriveFont((float)sizeY*fontCoefficient);
    }
    public void SetColors(Color[] colors)
    {
        this.colors = colors;
    }
    public void SetTextColor(Color color)
    {
        textColor = color;
    }
    public void SetAction(Runnable action)
    {
        this.action = action;
    }
    public void SetSelect(boolean state) {
        isSelected = state;
    }
    public void SetSelectionColor(Color color) {
        selectionColor = color;
    }
    public void SetSelectionWidth(double width) {
        selectionWidth = width;
    }
    public void SetOutside(boolean state){
        isOutside = state;
    }

    public UITransform GetTransform()
    {
        return new UITransform(posX, posY, sizeX, sizeY);
    }

    @Override
    public void OnMouseHover(float x, float y) {
        isHovering = true;
    }
    @Override
    public void OnMouseLeave() {
        isHovering = false;
        isPressed[0] = isPressed[1] = isPressed[2] = false;
    }
    @Override
    public void OnClick(float x, float y, int button) {
        isPressed[button] = true;
    }
    @Override
    public void OnRelease(float x, float y, int button) {
        if(button == 0 && isPressed[0] && action != null)
            action.run();
        isPressed[button] = false;
    }
    @Override
    public boolean Intersects(float x, float y) {
        if(x < posX-sizeX/2.0)
            return false;
        if(x > posX+sizeX/2.0)
            return false;
        if(y < posY-sizeY/2.0)
            return false;
        if(y > posY+sizeY/2.0)
            return false;
        return true;
    }

    @Override
    public void Tick(double delta) {

    }

    @Override
    public void Render(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;

        double x = posX - sizeX/2.0, y = posY - sizeY/2.0;

        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        Rectangle2D.Double rect = new Rectangle2D.Double(x, y, sizeX, sizeY);
        if(isOutside) {
            g2d.setColor(new Color(2, 2, 2, 161));
            g2d.fill(rect);
        }
        else {
            if (colors.length == 1) {
                g2d.setColor(colors[0]);
                g2d.fill(rect);
            } else {
                Area rectArea = new Area(rect);
                double len = 360.0 / colors.length, ang = 100;
                for (int i = 0; i < colors.length; i++) {
                    Arc2D arc = new Arc2D.Double(Arc2D.PIE);
                    arc.setFrame(posX - sizeX, posY - sizeY, sizeX * 2, sizeY * 2);
                    arc.setAngleStart(ang);
                    arc.setAngleExtent(len);
                    ang += len;
                    Area pie = new Area(arc);

                    pie.intersect(rectArea);
                    g2d.setColor(colors[i]);
                    g2d.fill(pie);
                }
            }
        }
        if(isSelected)
        {
            Rectangle2D.Double innerRect = new Rectangle2D.Double(x+selectionWidth, y+selectionWidth,
                    sizeX-selectionWidth*2.0, sizeY-selectionWidth*2.0);
            Area area = new Area(rect);
            area.subtract(new Area(innerRect));
            g2d.setColor(selectionColor);
            g2d.fill(area);
        }
        if(isOutside)
            return;

        if(!mainText.equals(""))
        {
            FontRenderContext frc = new FontRenderContext(null, false, false);
            TextLayout tx = new TextLayout(mainText, font, frc);
            int w = tx.getPixelBounds(frc, 0f, 0f).width, h = tx.getPixelBounds(frc, 0f, 0f).height;
            g2d.setColor(textColor);
            g2d.setFont(font);
            g2d.drawString(mainText, (float)(posX - w / 2.0), (float)(posY + h / 2.0));
        }
        float fontSize = font.getSize();
        if(!cornerText.equals(""))
        {
            Font cornerFont = font.deriveFont(Font.BOLD).deriveFont(fontSize*cornerCoefficient);
            g2d.setColor(textColor);
            g2d.setFont(cornerFont);
            FontRenderContext frc = new FontRenderContext(null, false, false);
            TextLayout tx = new TextLayout(cornerText, cornerFont, frc);
            int w = tx.getPixelBounds(frc, 0f, 0f).width, h = tx.getPixelBounds(frc, 0f, 0f).height;
            String lowText = "", upText = cornerText;
            if(cornerText.length() > 5)
            {
                lowText = cornerText.substring(5);
                upText = cornerText.substring(0, 5);
            }
            g2d.drawString(upText, (float)(posX - sizeX/2.0)+5, (float)(posY+h+5 - (sizeY)/2.0));
            if(lowText != "")
                g2d.drawString(lowText, (float)(posX - sizeX/2.0)+5, (float)(posY-5 + (sizeY)/2.0));
        }
        if(!smallText.equals(""))
        {
            Font smallFont = font.deriveFont(Font.BOLD);
            FontRenderContext frc = new FontRenderContext(null, false, false);
            TextLayout tx = new TextLayout(smallText, smallFont, frc);
            int w = tx.getPixelBounds(frc, 0f, 0f).width, h;
            float resize = (float)(sizeX-10) / w;
            if(resize > setCoefficient)
                resize = setCoefficient;
            smallFont = smallFont.deriveFont(fontSize*resize);
            tx = new TextLayout(smallText, smallFont, frc);
            w = tx.getPixelBounds(frc, 0f, 0f).width;
            h = tx.getPixelBounds(frc, 0f, 0f).height;
            g2d.setColor(textColor);
            g2d.setFont(smallFont);
            g2d.drawString(smallText, (float)(posX - w / 2.0), (float)(posY + h / 2.0));
        }
    }
}
