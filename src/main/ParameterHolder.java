package main;

public interface ParameterHolder<T>{

    void Set(T value);
    T Get();
}
