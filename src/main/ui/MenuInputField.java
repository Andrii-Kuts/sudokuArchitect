package main.ui;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Function;

public class MenuInputField extends RenderObject implements Button
{
    private MenuLabel label;
    private MenuButton button;
    private double posX, posY, sizeX, sizeY, thickness, roundness;
    private Color textFieldColor;
    private StringBuilder text;
    private Font font, placeholderFont;
    private String placeholderText;
    private int carrotPosition;
    private final double carrotSpeed = 0.3;
    private double carrotTime, labelPosX, labelPosY, carrotHeight;
    private boolean carrotVisible, showCarrot, justClicked = false, multiline;
    private Runnable selectAction, deselectAction;
    private Function<Character, Boolean> checker;

    private RoundRectangle2D.Double rect;

    public MenuInputField(double x, double y, double width, double height)
    {
        label = new MenuLabel(x-width/2.0, y);
        label.SetText("");
        label.SetAlignment(MenuLabel.Alignment.Left);
        labelPosX = x-width/2.0;
        labelPosY = y;
        showCarrot = false;
        multiline = false;

        font = placeholderFont = new Font("Arial", Font.PLAIN, 20);
        placeholderText = "Enter Text";

        button = new MenuButton(x, y, width, height, 5);
        button.SetAction(() -> Select(), 0);

        posX = x;
        posY = y;
        sizeX = width;
        sizeY = height;
        thickness = 0;
        roundness = 5.0;
        textFieldColor = new Color(255, 255, 255);
        text = new StringBuilder("");
        carrotPosition = 0;
        checker = (c) -> (c >= 32 && c < 127);

        rect = new RoundRectangle2D.Double(posX-(sizeX-thickness*2.0)/2.0, posY-(sizeY-thickness*2.0)/2.0,
                sizeX-thickness*2.0, sizeY-thickness*2.0, roundness-thickness, roundness-thickness);
    }

    public MenuInputField()
    {
        this(0, 0, 0, 0);
    }
    public MenuInputField(UITransform transform)
    {
        this(transform.x, transform.y, transform.width, transform.height);
    }

    public MenuButton getButton()
    {
        return button;
    }
    public MenuLabel getLabel()
    {
        return label;
    }
    public void SetTransform(double x, double y, double width, double height)
    {
        posX = x;
        posY = y;
        sizeX = width;
        sizeY = height;
        button.SetTransform(x, y, width, height);
        label.SetPosition(x-width/2.0+thickness, y);
        rect = new RoundRectangle2D.Double(posX-(sizeX-thickness*2.0)/2.0, posY-(sizeY-thickness*2.0)/2.0,
                sizeX-thickness*2.0, sizeY-thickness*2.0, roundness-thickness, roundness-thickness);
    }
    public void SetTransform(UITransform transform)
    {
        SetTransform(transform.x, transform.y, transform.width, transform.height);
    }
    public void SetThickness(double thickness)
    {
        this.thickness = thickness;
        labelPosX = posX-sizeX/2.0+thickness;
        label.SetPosition(labelPosX, posY);
        rect = new RoundRectangle2D.Double(posX-(sizeX-thickness*2.0)/2.0, posY-(sizeY-thickness*2.0)/2.0,
                sizeX-thickness*2.0, sizeY-thickness*2.0, roundness-thickness, roundness-thickness);
    }
    public void SetRoundness(double roundness)
    {
        this.roundness = roundness;
        button.SetRoundness(roundness);
        rect = new RoundRectangle2D.Double(posX-(sizeX-thickness*2.0)/2.0, posY-(sizeY-thickness*2.0)/2.0,
                sizeX-thickness*2.0, sizeY-thickness*2.0, roundness-thickness, roundness-thickness);
    }
    public void SetTextFieldColor(Color color)
    {
        textFieldColor = color;
    }
    public void SetText(String text)
    {
        if(text == null)
            text = "";
        this.text = new StringBuilder(text);
        updateLabel();
        MoveCarrot(0);
    }
    public void SetPlaceholder(String text)
    {
        placeholderText = text;
        updateLabel();
    }
    public void SetFonts(Font main, Font placeholder)
    {
        font = main;
        placeholderFont = placeholder;
        updateLabel();
    }
    public void SetFont(Font main)
    {
        SetFonts(main, main);
    }
    public void SetSelectAction(Runnable action)
    {
        selectAction = action;
    }
    public void SetDeselectAction(Runnable action)
    {
        deselectAction = action;
    }
    public void SetChecker(Function<Character, Boolean> checker)
    {
        this.checker = checker;
    }
    public void SetMultiline(boolean value)
    {
        if(value)
        {
            label.SetAlignment(MenuLabel.Alignment.Upper_Left);
            labelPosY = posY-sizeY/2.0+thickness;
            label.SetPosition(posX-sizeX/2.0+thickness, labelPosY);
        }
        else
        {
            label.SetAlignment(MenuLabel.Alignment.Left);
            labelPosY = posY;
            label.SetPosition(posX-sizeX/2.0+thickness, posY);
        }
        multiline = value;
        updateLabelPosition();
    }
    public String GetText()
    {
        return text.toString();
    }

    public void OnMouseHover(float x, float y) {
        button.OnMouseHover(x, y);
    }
    public void OnMouseLeave() {
        button.OnMouseLeave();
    }
    public void OnClick(float x, float y, int button) {
        this.button.OnClick(x, y, button);
    }
    public void OnRelease(float x, float y, int button) {
        this.button.OnRelease(x, y, button);
        if(justClicked)
        {
            if(text.length() > 0)
               carrotPosition = label.GetCaret(x, y);
            updateLabelPosition();
            justClicked = false;
        }
    }
    public boolean Intersects(float x, float y) {
        return button.Intersects(x, y);
    }
    public void Select()
    {
        selectAction.run();
        showCarrot = true;
        justClicked = true;
    }
    public void Deselect()
    {
        deselectAction.run();
        showCarrot = false;
    }
    private void updateLabel()
    {
        if(text.toString().equals("")) {
            label.SetFont(placeholderFont);
            label.SetText(placeholderText);
        }
        else {
            label.SetFont(font);
            label.SetText(text.toString());
        }
        carrotHeight = label.GetFontHeight();
    }
    public void Type(char c)
    {
        if(!checker.apply(c))
        {
            WrongCharacter(c);
            return;
        }
        text.insert(carrotPosition, c);
        carrotPosition++;
        updateLabel();
        updateLabelPosition();
    }
    public void KeyPress(KeyEvent event)
    {
        int code = event.getKeyCode();
        switch (code)
        {
            case KeyEvent.VK_BACK_SPACE:
                if(carrotPosition > 0) {
                    text.deleteCharAt(carrotPosition - 1);
                    carrotPosition--;
                }
                break;
            case KeyEvent.VK_DELETE:
                if(carrotPosition < text.length())
                {
                    text.deleteCharAt(carrotPosition);
                }
                break;
            case KeyEvent.VK_ENTER:
                if((event.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0)
                {
                    if(multiline) {
                        text.insert(carrotPosition, '\n');
                        carrotPosition++;
                        updateLabel();
                        updateLabelPosition();
                    }
                    break;
                }
            case KeyEvent.VK_ESCAPE:
                Deselect();
                break;
            case KeyEvent.VK_LEFT:
                MoveCarrot(-1);
                break;
            case KeyEvent.VK_RIGHT:
                MoveCarrot(1);
                break;
        }
        updateLabel();
        updateLabelPosition();
    }
    private void WrongCharacter(char c)
    {

    }

    private void updateLabelPosition() {
        Point2D.Double carrotPos = label.GetCarrotPosition(carrotPosition);
        carrotPos.x += labelPosX;
        carrotPos.y += labelPosY;
        if (carrotPos.x < posX - sizeX / 2.0 + thickness + 5.0) {
            double change = posX - sizeX / 2.0 + thickness + 5.0 - carrotPos.x;
            carrotPos.x += change;
            labelPosX += change;
        }
        if (carrotPos.x > posX + sizeX / 2.0 - thickness - 5.0) {
            double change = carrotPos.x - (posX + sizeX / 2.0 - thickness - 5.0);
            carrotPos.x -= change;
            labelPosX -= change;
        }
        if(multiline) {
            if (carrotPos.y < posY - sizeY / 2.0 + thickness + 5.0) {
                double change = posY - sizeY / 2.0 + thickness + 5.0 - carrotPos.y;
                labelPosY += change;
                carrotPos.y += change;
            }
            if (carrotPos.y + carrotHeight > posY + sizeY / 2.0 - thickness - 5.0) {
                double change = carrotPos.y - (posY + sizeY / 2.0 - thickness - 5.0) + carrotHeight;
                labelPosY -= change;
                carrotPos.y -= change;
            }
        }
        label.SetPosition(labelPosX, labelPosY);
    }
    public void SetCarrot(int position)
    {
        carrotPosition = position;
        if(carrotPosition < 0)
            carrotPosition = 0;
        else if(carrotPosition > text.length())
            carrotPosition = text.length();
        updateLabelPosition();
    }
    public void MoveCarrot(int dx)
    {
        carrotPosition += dx;
        if(carrotPosition < 0)
            carrotPosition = 0;
        else if(carrotPosition > text.length())
            carrotPosition = text.length();
        updateLabelPosition();
    }

    public void Tick(double delta) {
        label.Tick(delta);
        button.Tick(delta);
        carrotTime += delta;
        while(carrotTime >= carrotSpeed)
        {
            carrotVisible ^= true;
            carrotTime -= carrotSpeed;
        }
    }
    public void Render(Graphics g) {
        button.Render(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(textFieldColor);
        g2d.fill(rect);

        Shape oldClip = g2d.getClip();
        g2d.clip(rect);
        if(showCarrot && carrotVisible)
        {
            Point2D.Double carrotPos =label.GetCarrotPosition(carrotPosition);
            carrotPos.x += labelPosX;
            carrotPos.y += labelPosY;
            Rectangle2D.Double carrot = new Rectangle2D.Double(carrotPos.x, carrotPos.y, 2.0, carrotHeight);
            g2d.setColor(new Color(12, 12, 12));
            g2d.fill(carrot);
        }
        label.Render(g);
        g2d.setClip(oldClip);
    }
}
