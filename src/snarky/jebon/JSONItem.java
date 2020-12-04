package snarky.jebon;

public class JSONItem {

    private final String name;
    private final JSONTypes type;
    private final Object value;

    public JSONItem(String name, JSONTypes type, Object value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public JSONTypes getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public String toString() {
        return name + ": " + value + "[" + type + "]";
    }
}
