package main.sudoku.graphics;

import main.DecodeError;
import main.sudoku.Cell;
import main.sudoku.SudokuView;
import main.ui.MenuView;
import main.ui.RenderObject;
import main.ui.Style;
import main.ui.graphics.UICellClue;

import java.awt.*;
import java.awt.geom.Point2D;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class CellClues implements Graphic
{
    public int symbol, pattern, x, y;
    public String ULClue, URClue, LLClue, LRClue;
    public Color textColor, symbolColor, patternColor;

    public CellClues()
    {
        x = y = 0;
        symbol = pattern = -1;
        ULClue = URClue = LLClue = LRClue = "";
        textColor = new Color(0, 0, 0);
        symbolColor = new Color(33, 33, 33, 255);
        patternColor = new Color(0, 0, 0, 64);
    }

    public boolean IsEmpty()
    {
        return (symbol == -1) && (pattern == -1) && (ULClue.equals("")) && (URClue.equals("")) &&(LLClue.equals("")) &&(LRClue.equals(""));
    }

    public byte[] Encode() {
        ByteBuffer buffer = ByteBuffer.allocate(ULClue.length()+URClue.length()+LLClue.length()+LRClue.length() + 60);
        buffer.put((byte)x);
        buffer.put((byte)y);
        buffer.putInt(symbol);
        buffer.putInt(pattern);
        buffer.putInt(textColor.getRGB());
        buffer.putInt(symbolColor.getRGB());
        buffer.putInt(patternColor.getRGB());
        buffer.putInt(ULClue.length());
        buffer.putInt(URClue.length());
        buffer.putInt(LLClue.length());
        buffer.putInt(LRClue.length());
        buffer.put(ULClue.getBytes(StandardCharsets.UTF_8));
        buffer.put(URClue.getBytes(StandardCharsets.UTF_8));
        buffer.put(LLClue.getBytes(StandardCharsets.UTF_8));
        buffer.put(LRClue.getBytes(StandardCharsets.UTF_8));
        byte[] res = new byte[buffer.position()];
        System.arraycopy(buffer.array(), 0, res, 0, res.length);
        return res;
    }

    public int Decode(ByteBuffer buffer) throws DecodeError
    {
        int pos = buffer.position();
        x = buffer.get();
        y = buffer.get();
        symbol = buffer.getInt();
        pattern = buffer.getInt();
        textColor = new Color(buffer.getInt(), true);
        symbolColor = new Color(buffer.getInt(), true);
        patternColor = new Color(buffer.getInt(), true);
        int l1 = buffer.getInt(), l2 = buffer.getInt(), l3 = buffer.getInt(), l4 = buffer.getInt();
        byte[] b1 = new byte[l1], b2 = new byte[l2], b3 = new byte[l3], b4 = new byte[l4];
        buffer.get(b1);
        buffer.get(b2);
        buffer.get(b3);
        buffer.get(b4);
        ULClue = new String(b1);
        URClue = new String(b2);
        LLClue = new String(b3);
        LRClue = new String(b4);
        return buffer.position()-pos;
    }

    private double CellSize(SudokuView view)
    {
        return view.GetNodePos(0, 1).y-view.GetNodePos(0, 0).y;
    }

    @Override
    public RenderObject Convert(SudokuView view) {
        Style style = view.GetStyle();
        Point2D.Double pos = style.GetPoint(view.GetCellPos(x, y), Style.Anchor.Upper_Left);
        double size = style.GetScaled(CellSize(view));
        UICellClue ui = new UICellClue(pos.x, pos.y, size, size);
        ui.SetColors(symbolColor, textColor, patternColor);
        ui.SetFont(style.GetFont(2).deriveFont((float)(size*0.25)));
        //TODO Get pattern from style and set it ui.SetPattern();
        ui.SetShape(style.GetShape(symbol, size));
        ui.SetTexts(ULClue, URClue, LLClue, LRClue);
        ui.SetPattern(style.GetPattern(pattern, size));
        ui.SetRenderPriority(-1);
        return ui;
    }

    @Override
    public void Convert(SudokuView view, RenderObject renderObject) {

    }

    public String GetName() {
        return "Cell Clue";
    }
    public void Select() {

    }
    public void Deselect() {

    }
    public boolean IsSelected()
    {
        return false;
    }
    public void GenerateUI(MenuView view) {

    }

    public void UpdateUI(SudokuView view, UICellClue ui)
    {
        Style style = view.GetStyle();
        Point2D.Double pos = style.GetPoint(view.GetCellPos(x, y), Style.Anchor.Upper_Left);
        double size = style.GetScaled(CellSize(view));
        ui.SetTransform(pos.x, pos.y, size, size);
        ui.SetColors(symbolColor, textColor, patternColor);
        ui.SetFont(style.GetFont(2).deriveFont((float)(size*0.25)));
        ui.SetShape(style.GetShape(symbol, size));
        ui.SetTexts(ULClue, URClue, LLClue, LRClue);
        ui.SetPattern(style.GetPattern(pattern, size));
    }
}
