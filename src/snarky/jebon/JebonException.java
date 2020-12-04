package snarky.jebon;

public class JebonException extends Exception {

    public JebonException() {
        this("Jebon exception.");
    }

    public JebonException(String message) {
        super(message);
    }
}
