package main.sudoku;

import main.DecodeError;

import java.nio.ByteBuffer;

public class Cell {
    public enum CellType {
        Regular, Frozen, Outside
    }

    private int digit, pencilMarks, setMark, colorMarks, symbol;
    private CellType type;

    Cell() {
        digit = pencilMarks = setMark = colorMarks = symbol = 0;
        type = CellType.Regular;
    }

    Cell(CellType type) {
        digit = pencilMarks = setMark = colorMarks = 0;
        this.type = type;
    }

    Cell(int d) {
        digit = d;
        pencilMarks = setMark = colorMarks = 0;
        type = CellType.Frozen;
    }

    public void SetDigit(int digit) {
        this.digit = digit;
    }

    public void SetPencil(int mask) {
        this.pencilMarks = mask;
    }

    public void SetSmall(int mask) {
        this.setMark = mask;
    }

    public void SetColors(int mask) {
        this.colorMarks = mask;
    }

    public int GetValue() {
        return digit;
    }

    public int GetPencil() {
        return pencilMarks;
    }

    public int GetSmall() {
        return setMark;
    }

    public int GetColor() {
        return colorMarks;
    }

    public CellType GetType() {
        return type;
    }

    public void SetType(CellType type)
    {
        this.type = type;
    }

    public byte[] Encode() {
        byte[] bytes = new byte[2];
        switch (type)
        {
            case Outside:
                bytes[0] = 0;
                break;
            case Frozen:
                bytes[0] = 1;
                break;
            case Regular:
                bytes[0] = 2;
                break;
        }
        bytes[1] = (byte)digit;
        return bytes;
    }

    public int Decode(ByteBuffer buffer) throws DecodeError {
        byte[] bytes = new byte[2];
        buffer.get(bytes);
        setMark = colorMarks = pencilMarks = 0;
        switch (bytes[0])
        {
            case 0:
                type = CellType.Outside;
                break;
            case 1:
                type = CellType.Frozen;
                break;
            case 2:
                type = CellType.Regular;
                break;
            default:
                throw new DecodeError();
        }
        if(bytes[1] < 0 || bytes[1] > 9)
            throw new DecodeError();
        digit = bytes[1];
        return 2;
    }
}
