package snarky.jebon;

import java.util.HashMap;

class JebonContainer extends HashMap<String, Object> {

    private final boolean isObject;

    protected JebonContainer(boolean isObject) {
        this.isObject = isObject;
    }

    protected boolean isObject() {
        return isObject;
    }
}
