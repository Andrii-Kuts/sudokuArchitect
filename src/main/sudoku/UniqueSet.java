package main.sudoku;

import main.DecodeError;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class UniqueSet implements SudokuConstraint
{
    private Board board;
    private ArrayList<BoardPosition> cells;

    public UniqueSet(main.sudoku.Board board)
    {
        this.board = board;
        cells = new ArrayList<>();
    }

    public UniqueSet(Board board, ArrayList<BoardPosition> cells)
    {
        this.board = board;
        this.cells = cells;
    }
    public UniqueSet()
    {
        cells = new ArrayList<>();
        board = null;
    }

    public void SetBoard(Board board)
    {
        this.board = board;
    }

    private int gcd(int a, int b)
    {
        if(a < 0)
            a = -a;
        if(b < 0)
            b = -b;
        if(a > b)
        {
            b ^= a;
            a ^= b;
            b ^= a;
        }
        while(a > 0)
        {
            int c = b;
            b = a;
            a = c%b;
        }
        return b;
    }

    public void AddRectangle(BoardPosition pos1, BoardPosition pos2)
    {
        int x1 = pos1.x, x2 = pos2.x, y1 = pos1.y, y2 = pos2.y;
        if(x1 > x2)
        {
            x1 ^= x2;
            x2 ^= x1;
            x1 ^= x2;
        }
        if(y1 > y2)
        {
            y1 ^= y2;
            y2 ^= y1;
            y1 ^= y2;
        }
        for(int x = x1; x <= x2; x++)
        {
            for(int y = y1; y <= y2; y++)
            {
                cells.add(new BoardPosition(x, y));
            }
        }
    }

    public void AddLine
            (BoardPosition pos1, BoardPosition pos2)
    {
        int dx = pos2.x-pos1.x, dy = pos2.y-pos1.y, g = gcd(dx, dy);
        if(g != 0) {
            dx /= g;
            dy /= g;
        }
        int x = pos1.x, y = pos1.y;
        while(x != pos2.x || y != pos2.y)
        {
            cells.add(new BoardPosition(x, y));
            x += dx;
            y += dy;
        }
        cells.add(new BoardPosition(x, y));
    }

    public boolean CheckBoard()
    {
        int mask = 0;
        for(BoardPosition pos : cells)
        {
            int v = board.GetCell(pos.x, pos.y).GetValue();
            if(v == 0)
                continue;
            if((mask & (1 << v)) > 0)
                return false;
            mask |= (1 << v);
        }
        return true;
    }

    @Override
    public byte[] Encode() {
        ByteBuffer buffer = ByteBuffer.allocate(cells.size()*2 + 4);
        buffer.putInt(cells.size());
        for(BoardPosition pos : cells)
        {
            buffer.put(pos.Encode());
        }
        return buffer.array();
    }

    @Override
    public int Decode(ByteBuffer buffer) throws DecodeError
    {
        int sum = 0;
        int len = buffer.getInt();
        sum += 4;
        if(len < 0)
            throw new DecodeError();
        cells = new ArrayList<>();
        for(int i = 0; i < len; i++)
        {
            BoardPosition pos = new BoardPosition();
            sum += pos.Decode(buffer);
            cells.add(pos);
        }
        return sum;
    }

    public void Clear()
    {
        cells.clear();
    }
}
