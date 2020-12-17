package snarky.jebon;

public abstract class Finder {

    protected abstract void update(char c) throws JebonException;
    protected abstract JSONItem getValue();
}
