package snarky.jebon;

/**
 * Class representing a json field, field being the key-value pair.
 */
public class JSONItem {

    private final String name;
    private final JSONTypes type;
    private final Object value;

    protected JSONItem(String name, JSONTypes type, Object value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    /**
     * Get the name of the field.
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the data type of the field.
     * @return The data type.
     */
    public JSONTypes getType() {
        return type;
    }

    /**
     * The value; the data type and the value depend on the data type of
     * the field.
     * <ul>
     * <li><code>JSONTypes.STRING</code>: String.</li>
     * <li><code>JSONTypes.OBJECT</code>: String array containing the names of
     * all fields inside the object.</li>
     * <li><code>JSONTypes.ARRAY</code>: String array; like <code>JSONTypes.OBJECT</code> 
     * but the names are integer representing the indice.</li>
     * <li><code>JSONTypes.BOOLEAN</code>: Boolean.</li>
     * <li><code>JSONTypes.NUMBER</code>: Double.</li>
     * <li><code>JSONTypes.NULL</code>: null.</li>
     * </ul>
     * @return The value of the field.
     */
    public Object getValue() {
        return value;
    }

    public String toString() {
        return name + ": " + value + "[" + type + "]";
    }
}
