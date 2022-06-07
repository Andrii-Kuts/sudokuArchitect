package main.ui;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Random;

public class MenuButton extends RenderObject implements main.ui.Button
{
    public enum ButtonColor
    {
        Default, Accent, Selected, Transparent, Dimmed;
    }

    protected double posX, posY, sizeX, sizeY, roundness, colorSpeed;
    protected Color idleColor, hoverColor, pressColor, textColor;
    private DoubleColor currentColor, destinationColor;
    private String text;
    private Font font;
    private Runnable[] action;
    public MenuButton(double x, double y, double width, double height, double round)
    {
        posX = x; posY = y;
        sizeX = width;
        sizeY = height;
        roundness = round;

        colorSpeed = 5.0;
        currentColor = new DoubleColor(0, 0, 0);
        destinationColor = new DoubleColor(0, 0, 0);
        SetColors(ButtonColor.Default);
        ResetColors();

        text = "";
        font = new Font("Arial", Font.PLAIN, 11);

        action = new Runnable[3];

        isPressed = new boolean[3];
        isHovered = false;
    }
    public MenuButton()
    {
        this(0, 0, 0, 0, 0);
    }
    public MenuButton(UITransform transform, double roundness)
    {
        this(transform.x, transform.y, transform.width, transform.height, roundness);
    }

    public void SetTransform(double x, double y, double width, double height) {
        posX = x;
        posY = y;
        sizeX = width;
        sizeY = height;
    }
    public void SetTransform(UITransform transform)
    {
        SetTransform(transform.x, transform.y, transform.width, transform.height);
    }
    public void SetRoundness(double value)
    {
        roundness = value;
    }
    public void SetColors(ButtonColor colors)
    {
        if(colors == ButtonColor.Default)
        {
            idleColor = new Color(192, 202, 220);
            hoverColor = new Color(155, 163, 191);
            pressColor = new Color(74, 107, 165);
            textColor = new Color(20, 22, 42);
        }
        else if(colors == ButtonColor.Selected)
        {
            idleColor = new Color(27, 42, 92);
            hoverColor = new Color(26, 39, 94);
            pressColor = new Color(14, 34, 68);
            textColor = new Color(200, 201, 219);
        }
    }
    public void SetColors(Color[] colors)
    {
        idleColor = colors[0];
        hoverColor = colors[1];
        pressColor = colors[2];
        textColor = colors[3];
    }
    public void ResetColors()
    {
        destinationColor.set(idleColor);
        currentColor.set(idleColor);
    }
    public void SetColorSpeed(double value)
    {
        colorSpeed = value;
    }
    public void SetText(String text)
    {
        this.text = text;
    }
    public void SetTextStyle(Font font)
    {
        this.font = font;
    }
    public void SetAction(Runnable action, int button)
    {
        this.action[button] = action;
    }

    private void MoveColor(DoubleColor a, Color b, double dst)
    {
        double d = 0;
        double x = (a.r - b.getRed());
        d += x*x;
        x = (a.g - b.getGreen());
        d += x*x;
        x = (a.b - b.getBlue());
        d += x*x;
        x = (a.a - b.getAlpha());
        d += x*x;
        d = Math.sqrt(d);
        d /= 255.0;

        if((dst+0.001) > d)
        {
            a.set(b);
            return;
        }
        double k = dst/d;

        a.set((a.r * (1-k) + b.getRed() * k), (a.g * (1-k) + b.getGreen() * k), (a.b * (1-k) + b.getBlue() * k), (a.a * (1-k) + b.getAlpha() * k));
    }

    public void Tick(double delta)
    {
        Color col;
        if(isPressed[0])
            col = pressColor;
        else if(isHovered)
            col = hoverColor;
        else
            col = idleColor;
        MoveColor(currentColor, col, colorSpeed*delta);
    }
    public void Render(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;

        double x = posX - sizeX/2.0, y = posY - sizeY/2.0;

        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        RoundRectangle2D.Double rect = new RoundRectangle2D.Double(x, y, sizeX, sizeY, roundness, roundness);
        g2d.setColor(currentColor.get());
        g2d.fill(rect);

        if(text != "")
        {
            FontRenderContext frc = new FontRenderContext(null, false, false);
            TextLayout tx = new TextLayout(text, font, frc);
            int w = tx.getPixelBounds(frc, 0f, 0f).width, h = tx.getPixelBounds(frc, 0f, 0f).height;
            g2d.setColor(textColor);
            g2d.setFont(font);
            g2d.drawString(text, (float)(posX - w / 2.0), (float)(posY + h / 2.0));
        }
    }

    private boolean[] isPressed;
    private boolean isHovered;
    public void OnMouseHover(float x, float y) {
        isHovered = true;
    }
    public void OnMouseLeave() {
        isHovered = false;
        isPressed[0] = isPressed[1] = isPressed[2] = false;
    }
    public void OnClick(float x, float y, int button) {
        isPressed[button] = true;
    }
    public void OnRelease(float x, float y, int button) {
        if(isPressed[button])
            action[button].run();
        isPressed[button] = false;
    }
    public boolean Intersects(float x, float y) {
        if(x < posX-sizeX/2.0 || x > posX+sizeX/2.0 || y < posY-sizeY/2.0 || y > posY+sizeY/2.0)
            return false;
        return true;
    }
}
