package main.ui;

public interface MenuParameter<T> {
    T GetParameterValue();
    void SetParameterValue(T value);
}
