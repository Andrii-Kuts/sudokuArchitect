package main.sudoku;

import main.DecodeError;

import java.nio.ByteBuffer;

public class BoardPosition
{
    public int x, y;

    public BoardPosition()
    {
        x = y = 0;
    }

    public BoardPosition(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    byte[] Encode()
    {
        byte[] bytes = new byte[2];
        bytes[0] = (byte)x;
        bytes[1] = (byte)y;
        return bytes;
    }

    public int Decode(ByteBuffer buffer) throws DecodeError {
        byte[] bytes = new byte[2];
        buffer.get(bytes);
        x = bytes[0];
        y = bytes[1];
        if(x < 0) x += 256;
        if(y < 0) y += 256;
        return 2;
    }
}
