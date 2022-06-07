package main.ui.graphics;

import main.ui.RenderObject;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class UIUniqueRect extends RenderObject implements UIConstraint{
    private double pos1x, pos1y, pos2x, pos2y, roundness;
    private Color colorIdle, colorOk, colorWrong, color;

    private enum renderType
    {
        Idle, Ok, Wrong
    };
    private UIUniqueRect.renderType type;

    public UIUniqueRect(double pos1x, double pos1y, double pos2x, double pos2y)
    {
        this.pos1x = pos1x;
        this.pos1y = pos1y;
        this.pos2x = pos2x;
        this.pos2y = pos2y;

        type = renderType.Idle;
        roundness = 10;

        colorIdle = new Color(255, 255, 255);
        colorOk = new Color(70, 222, 21);
        colorWrong = new Color(229, 15, 15);
    }
    public UIUniqueRect()
    {
        this(0, 0, 0, 0);
    }

    public void SetPosition(double pos1x, double pos1y, double pos2x, double pos2y) {
        this.pos1x = pos1x;
        this.pos1y = pos1y;
        this.pos2x = pos2x;
        this.pos2y = pos2y;
    }
    public void SetPointPosition(int index, double x, double y) {
        if(index == 0) {
            this.pos1x = x;
            this.pos1y = y;
        }
        else {
            this.pos2x = x;
            this.pos2y = y;
        }
    }
    public void SetColor(Color color) {
        this.color = color;
    }
    public void SetColors(Color colorIdle, Color colorOk, Color colorWrong) {
        this.colorIdle = colorIdle;
        this.colorOk = colorOk;
        this.colorWrong = colorWrong;
        switch (type) {
            case Idle:
                color = colorIdle;
                break;
            case Wrong:
                color = colorWrong;
                break;
            case Ok:
                color = colorOk;
                break;
        }
    }
    private Color setColorTransparency(Color c, int alpha)
    {
        c = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
        return c;
    }
    public void SetRoundness(double roundness)
    {
        this.roundness = roundness;
    }


    public void SetIdle() {
        color = colorIdle;
        type = renderType.Idle;
    }
    public void SetWrong() {
        color = colorWrong;
        type = renderType.Wrong;
    }
    public void SetOk() {
        color = colorOk;
        type = renderType.Ok;
    }

    @Override
    public void Tick(double delta) {

    }

    @Override
    public void Render(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double szx = pos2x-pos1x, szy = pos2y-pos1y;
        RoundRectangle2D.Double rect = new RoundRectangle2D.Double(pos1x, pos1y, szx, szy, roundness, roundness);
        g2d.setColor(color);
        g2d.fill(rect);
    }
}
