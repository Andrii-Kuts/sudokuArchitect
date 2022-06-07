package main.sudoku.graphics;

import main.DecodeError;
import main.ParameterHolder;
import main.sudoku.BoardPosition;
import main.sudoku.SudokuConstraint;
import main.sudoku.SudokuView;
import main.sudoku.UniqueSet;
import main.ui.*;
import main.ui.graphics.UIConstraint;
import main.ui.graphics.UIUniqueRect;

import java.awt.*;
import java.awt.geom.Point2D;
import java.nio.ByteBuffer;

public class UniqueRect implements Constraint {
    public int pos1x, pos1y, pos2x, pos2y, priority;
    public Color color, okColor, wrongColor;
    private MenuGroup group;
    private boolean selected = false;

    public UniqueRect(int pos1x, int pos1y, int pos2x, int pos2y) {
        this.pos1x = pos1x;
        this.pos1y = pos1y;
        this.pos2x = pos2x;
        this.pos2y = pos2y;

        priority = -2;
        color = new Color(0, 0, 0, 0);
        color = new Color(0, 0, 0, 0);
        okColor = new Color(0, 200, 0, 60);
        wrongColor = new Color(200, 0, 0, 60);
    }

    public UniqueRect() {
        this(0, 0, 1, 1);
    }

    @Override
    public UIConstraint Convert(SudokuView view) {
        UIUniqueRect rect = new UIUniqueRect();
        Convert(view, rect);
        return rect;
    }

    @Override
    public void Convert(SudokuView view, RenderObject renderObject) {
        Point2D.Double pos1 = view.GetNodePos(pos1x, pos1y), pos2 = view.GetNodePos(pos2x + 1, pos2y + 1);
        Style style = view.GetStyle();
        pos1 = style.GetPoint(pos1, Style.Anchor.Upper_Left);
        pos2 = style.GetPoint(pos2, Style.Anchor.Upper_Left);
        UIUniqueRect ui = (UIUniqueRect) renderObject;
        ui.SetPosition(pos1.x, pos1.y, pos2.x, pos2.y);
        ui.SetRoundness(style.GetScaled(10));
        ui.SetColors(color, okColor, wrongColor);
        ui.SetRenderPriority(priority);
    }

    @Override
    public String GetName() {
        return "Unique Rectangle";
    }

    public void Select() {
        group.SetVisible(true);
        selected = true;
    }

    public void Deselect() {
        group.SetVisible(false);
        selected = false;
    }

    public boolean IsSelected(){
        return selected;
    }

    public void GenerateUI(MenuView view) {
        SudokuView sudokuView = (SudokuView)view;
        Style style = sudokuView.GetStyle();

        style.StartParameters();
        Point2D.Double gridPos = sudokuView.GetNodePos(0, 0), gridPos2 = sudokuView.GetNodePos(1, 1);
        gridPos = style.GetPoint(gridPos, Style.Anchor.Upper_Left);
        gridPos2 = style.GetPoint(gridPos2, Style.Anchor.Upper_Left);
        Point2D.Double gridSize = new Point2D.Double(gridPos2.x-gridPos.x, gridPos2.y-gridPos.y);
        gridPos = style.GetPoint(sudokuView.GetCellPos(0, 0), Style.Anchor.Upper_Left);
        Point gridDimension = sudokuView.GetSize();
        gridDimension = new Point(gridDimension.x-1, gridDimension.y-1);
        MenuParameter<Point> pos1Param =
                style.GetPointParameter(new ParameterHolder<Point>() {
                    public void Set(Point value) {
                        pos1x = value.x;
                        pos1y = value.y;
                    }
                    public Point Get() {
                        return new Point(pos1x, pos1y);
                    }
                }, gridPos, gridSize, gridDimension);
        MenuParameter<Point> pos2Param =
                style.GetPointParameter(new ParameterHolder<Point>() {
                    public void Set(Point value) {
                        pos2x = value.x;
                        pos2y = value.y;
                    }
                    public Point Get() {
                        return new Point(pos2x, pos2y);
                    }
                }, gridPos, gridSize, gridDimension);
        MenuParameter<Integer> priorityParam =
                style.GetIntegerParameter(view, new ParameterHolder<Integer>() {
                    public void Set(Integer value) {
                        priority = value;
                    }
                    public Integer Get() {
                        return priority;
                    }
                }, "Render Priority", -100, 100, 0);
        style.GetColorParameter(new ParameterHolder<Color>() {
            public void Set(Color value) {
                color = value;
            }
            public Color Get() {
                return color;
            }
        }, "Idle Color");
        style.GetColorParameter(new ParameterHolder<Color>() {
            public void Set(Color value) {
                okColor = value;
            }
            public Color Get() {
                return okColor;
            }
        }, "Ok Color");
        style.GetColorParameter(new ParameterHolder<Color>() {
            public void Set(Color value) {
                wrongColor = value;
            }
            public Color Get() {
                return wrongColor;
            }
        }, "Wrong Color");
        group = style.GetParameterWindow();
        group.SetVisible(false);
    }

    //TODO finish implementing methods
    @Override
    public SudokuConstraint GetConstraint() {
        UniqueSet us = new UniqueSet();
        GetConstraint(us);
        return us;
    }

    @Override
    public void GetConstraint(SudokuConstraint constraint) {
        UniqueSet us = (UniqueSet)constraint;
        us.Clear();
        us.AddRectangle(new BoardPosition(pos1x, pos1y), new BoardPosition(pos2x, pos2y));
    }

    @Override
    public byte[] Encode() {
        ByteBuffer buffer = ByteBuffer.allocate(60);
        buffer.put((byte) pos1x);
        buffer.put((byte) pos1y);
        buffer.put((byte) pos2x);
        buffer.put((byte) pos2y);
        buffer.putInt(priority);
        buffer.putInt(color.getRGB());
        buffer.putInt(okColor.getRGB());
        buffer.putInt(wrongColor.getRGB());
        byte[] res = new byte[buffer.position()];
        System.arraycopy(buffer.array(), 0, res, 0, res.length);
        return res;
    }

    @Override
    public int Decode(ByteBuffer buffer) throws DecodeError {
        pos1x = buffer.get();
        pos1y = buffer.get();
        pos2x = buffer.get();
        pos2y = buffer.get();
        if(pos1x < 0) pos1x += 256;
        if(pos1y < 0) pos1y += 256;
        if(pos2x < 0) pos2x += 256;
        if(pos2y < 0) pos2y += 256;
        priority = buffer.getInt();
        color = new Color(buffer.getInt(), true);
        okColor = new Color(buffer.getInt(), true);
        wrongColor = new Color(buffer.getInt(), true);
        return 12;
    }
}
