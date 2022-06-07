package main.ui;

public class CharSetChecker implements InputFieldChecker{
    boolean[] symbols;

    public CharSetChecker()
    {
        symbols = new boolean[256];
    }
    public void SetSymbol(char c, boolean value){
        symbols[c] = value;
    }
    public void SetRange(char beg, char end, boolean value)
    {
        for(char c = beg; c <= end; )
        {
            symbols[c] = value;
            if(c == end)
                break;
            c++;
            if(c == beg)
                break;
        }
    }
    @Override
    public boolean AllowCharacter(char c)
    {
        return symbols[c];
    }
}
