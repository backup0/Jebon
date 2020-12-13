package snarky.jebon;

public abstract class Finder {

    protected abstract void update(String s) throws JebonException;
    protected abstract JSONItem getValue();
}
