package main.sudoku.graphics;

import main.DecodeError;
import main.ParameterHolder;
import main.sudoku.SudokuView;
import main.ui.*;
import main.ui.graphics.UILine;

import java.awt.*;
import java.awt.geom.Point2D;
import java.nio.ByteBuffer;

public class Line implements Graphic
{
    public Integer pos1x, pos1y, pos2x, pos2y, priority;
    public Double thickness;
    public Color color;
    private MenuGroup group;
    private boolean isSelected = false;

    public Line(int pos1x, int pos1y, int pos2x, int pos2y)
    {
        group = new MenuGroup();

        this.pos1x = pos1x;
        this.pos1y = pos1y;
        this.pos2x = pos2x;
        this.pos2y = pos2y;

        thickness = 5.0;
        priority = 2;
        color = new Color(10, 10, 10);
    }

    public Line()
    {
        this(0, 0, 1, 1);
    }

    public UILine Convert(SudokuView view)
    {
        UILine ui = new UILine();
        Convert(view, ui);
        return ui;
    }
    public void Convert(SudokuView view, RenderObject renderObject)
    {
        Point2D.Double pos1 = view.GetNodePos(pos1x, pos1y), pos2 = view.GetNodePos(pos2x, pos2y);
        Style style = view.GetStyle();
        pos1 = style.GetPoint(pos1, Style.Anchor.Upper_Left);
        pos2 = style.GetPoint(pos2, Style.Anchor.Upper_Left);
        if(!(renderObject instanceof RenderObject))
            return;
        UILine ui = (UILine)renderObject;
        ui.SetPosition(pos1.x, pos1.y, pos2.x, pos2.y);
        ui.SetThickness(style.GetScaled(thickness));
        ui.SetColor(color);
        if(priority != ui.GetRenderPriority())
        {
            ui.SetRenderPriority(priority);
        }
    }
    public String GetName() {
        return "Line";
    }
    public void Select() {
        group.SetVisible(true);
        isSelected = true;
    }
    public void Deselect() {
        group.SetVisible(false);
        isSelected = false;
    }
    public boolean IsSelected()
    {
        return isSelected;
    }
    public void GenerateUI(MenuView view)
    {
        SudokuView sudokuView = (SudokuView)view;
        Style style = sudokuView.GetStyle();

        style.StartParameters();
        MenuParameter<Integer> priorityParameter =
                style.GetIntegerParameter(view, new ParameterHolder<Integer>() {
                    public void Set(Integer value) {
                        priority = value;
                    }
                    public Integer Get() {
                        return priority;
                    }
                }, "Render Priority", -100, 100, 0);
        MenuParameter<Double> thicknessParameter =
                style.GetDoubleParameter(view, new ParameterHolder<Double>() {
                    public void Set(Double value) {
                        thickness = value;
                    }
                    public Double Get() {
                        return thickness;
                    }
                }, "Thickness", 0.2, 30, 2);
        Point2D.Double gridPos = sudokuView.GetNodePos(0, 0), gridPos2 = sudokuView.GetNodePos(1, 1);
        gridPos = style.GetPoint(gridPos, Style.Anchor.Upper_Left);
        gridPos2 = style.GetPoint(gridPos2, Style.Anchor.Upper_Left);
        Point2D.Double gridSize = new Point2D.Double(gridPos2.x-gridPos.x, gridPos2.y-gridPos.y);
        MenuParameter<Point> pos1Param =
                style.GetPointParameter(new ParameterHolder<Point>() {
                    public void Set(Point value) {
                        pos1x = value.x;
                        pos1y = value.y;
                    }
                    public Point Get() {
                        return new Point(pos1x, pos1y);
                    }
                }, gridPos, gridSize, sudokuView.GetSize());
        MenuParameter<Point> pos2Param =
                style.GetPointParameter(new ParameterHolder<Point>() {
                    public void Set(Point value) {
                        pos2x = value.x;
                        pos2y = value.y;
                    }
                    public Point Get() {
                        return new Point(pos2x, pos2y);
                    }
                }, gridPos, gridSize, sudokuView.GetSize());
        MenuParameter<Color> colorParam =
                style.GetColorParameter(new ParameterHolder<Color>() {
                    public void Set(Color value) {
                        color = value;
                    }
                    public Color Get() {
                        return color;
                    }
                }, "Color");
        group = style.GetParameterWindow();
        group.SetVisible(false);
    }

    public byte[] Encode() {
        ByteBuffer buffer = ByteBuffer.allocate(60);
        buffer.put(pos1x.byteValue());
        buffer.put(pos1y.byteValue());
        buffer.put(pos2x.byteValue());
        buffer.put(pos2y.byteValue());
        buffer.putInt(priority);
        buffer.putDouble(thickness);
        buffer.putInt(color.getRGB());

        byte[] res = new byte[buffer.position()];
        System.arraycopy(buffer.array(), 0, res, 0, res.length);
        return res;
    }
    public int Decode(ByteBuffer buffer) throws DecodeError {
        pos1x = (int)buffer.get();
        pos1y = (int)buffer.get();
        pos2x = (int)buffer.get();
        pos2y = (int)buffer.get();
        if(pos1x < 0) pos1x += 256;
        if(pos1y < 0) pos1y += 256;
        if(pos2x < 0) pos2x += 256;
        if(pos2y < 0) pos2y += 256;
        priority = buffer.getInt();
        thickness = buffer.getDouble();
        color = new Color(buffer.getInt(), true);
        return 20;
    }
}
