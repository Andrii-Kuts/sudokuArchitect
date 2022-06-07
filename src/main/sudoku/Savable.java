package main.sudoku;

import main.DecodeError;

import java.nio.ByteBuffer;

public interface Savable
{
    byte[] Encode();
    int Decode(ByteBuffer buffer) throws DecodeError;
}
