package main.ui;

public interface Button
{
    public void OnMouseHover(float x, float y);
    public void OnMouseLeave();
    public void OnClick(float x, float y, int button);
    public void OnRelease(float x, float y, int button);
    public boolean Intersects(float x, float y);
}