package snarky.jebon;

import java.util.ArrayList;
import java.util.Arrays;

public class JSONCreator {

    private final JebonTree jTree;

    /**
     *
     * @param object
     */
    public JSONCreator(boolean object) {
        jTree = new JebonTree(object);
    }

    /**
     *
     * @param val
     * @param keys
     * @throws IndexOutOfBoundsException if key is empty.
     * @throws JebonException If key or value is invalid.
     */
    public void put(String val, String... keys) throws JebonException {
        putItem(new JSONItem("", JSONTypes.STRING, val), keys);
    }

    /**
     *
     * @param val
     * @param keys
     * @throws IndexOutOfBoundsException if key is empty.
     * @throws JebonException If key or value is invalid.
     */
    public void put(double val, String... keys) throws JebonException {
        putItem(new JSONItem("", JSONTypes.NUMBER, val), keys);
    }

    /**
     *
     * @param val
     * @param keys
     * @throws IndexOutOfBoundsException if key is empty.
     * @throws JebonException If key or value is invalid.
     */
    public void put(boolean val, String... keys) throws JebonException {
        putItem(new JSONItem("", JSONTypes.BOOLEAN, val), keys);
    }

    /**
     *
     * @param val
     * @param keys
     * @throws IndexOutOfBoundsException if key is empty.
     * @throws JebonException If key or value is invalid.
     */
    public void put(SpecialType val, String... keys) throws JebonException {
        switch (val) {

            case JSONObject:
                putItem(new JSONItem("", JSONTypes.OBJECT, val), keys);
                break;
            case JSONArray:
                putItem(new JSONItem("", JSONTypes.ARRAY, val), keys);
                break;
            case JSONNull:
                putItem(new JSONItem("", JSONTypes.NULL, val), keys);
                break;
            default:
                break;
        }
    }

    private void putItem(JSONItem item, String... keys) throws JebonException {

        final ArrayList<String> keyFragments = new ArrayList<>(Arrays.asList(keys));
        final String lastKey = keyFragments.remove(keyFragments.size() - 1);

        int index = 0;
        while (!keyFragments.isEmpty()) {
            // remove from the front.
            final String key = keyFragments.remove(0);
            final Object obj = jTree.getObject(index).get(key);

            if (obj == null) {
                // does not exist, create it
                // add back the key because we need to start here.
                keyFragments.add(0, key);
                index = createPath(index, keyFragments, lastKey);
                // the list should be empty by now ..
                // still need to break, else the below will be x'cuted
                break;
            }

            if (obj instanceof Integer) {
                // object exists - move to the next.
                index = (int) obj;
            }
            else {
                // not an object.
                // can't insert into non object.
                throw new JebonException("Key does not point to object/array");
            }
        }
        jTree.addItem(index, new JSONItem(lastKey, item.getType(), item.getValue()));
    }

    private int createPath(int index, ArrayList<String> keyFragments, String lastKey) {

        int cIndex = index;
        for (int i = 0; i < keyFragments.size(); i++) {
            final String key = keyFragments.get(i);
            final String nextKey;
            final int nextIndex = i + 1;
            if (nextIndex < keyFragments.size()) {
                nextKey = keyFragments.get(nextIndex);
            }
            else {
                nextKey = lastKey;
            }

            // if name is integer we treat it as a member of an array.
            if (intAsNameCheck(nextKey).length() == 0) {
                cIndex = jTree.addItem(cIndex, new JSONItem(key, JSONTypes.OBJECT, null));
            }
            else {
                cIndex = jTree.addItem(cIndex, new JSONItem(key, JSONTypes.ARRAY, null));
            }
        }
        return cIndex;
    }

    /**
     * Get JSON representation of this object.
     * @return JSON String.
     */
    public String toString() {

        final ArrayList<JebonContainer> queue = new ArrayList<>();
        final JebonContainer firstObj = jTree.getObject(0);
        queue.add(firstObj);

        final StringBuilder sbs = new StringBuilder();
        sbs.append(firstObj.isObject() ? "{" : "[");

        while (!queue.isEmpty()) {

            final int index = queue.size() - 1;
            final JebonContainer obj = queue.get(index);

            final String[] keySet = obj.keySet().toArray(new String[0]);
            if (keySet.length == 0) {
                queue.remove(index);
                sbs.append(obj.isObject() ? "}" : "]");

                final int prevIndex = index - 1;
                if (prevIndex >= 0) {
                    final JebonContainer prevObj = queue.get(prevIndex);
                    if (!prevObj.isEmpty()) {
                        sbs.append(",");
                    }
                }
            }

            for (String key : keySet) {

                final Object jsonItem = obj.remove(key);

                if (obj.isObject()) {
                    // array does not use name
                    sbs.append("\"").append(key).append("\"");
                    sbs.append(":");
                }

                if (jsonItem instanceof Integer) {
                    final JebonContainer nestedObj = jTree.getObject((int) jsonItem);
                    sbs.append(nestedObj.isObject() ? "{" : "[");
                    queue.add(nestedObj);
                    break;
                }
                else {
                    sbs.append(getVal((JSONItem) jsonItem));
                    if (!obj.isEmpty()) {
                        sbs.append(",");
                    }
                }
            }
        }
        return sbs.toString();
    }

    private String getVal(JSONItem item) {

        final String s;

        switch (item.getType()) {

            case STRING:
                s = "\"" + item.getValue() + "\"";
                break;
            case OBJECT:
            case ARRAY:
                s = "";
                break;
            case NUMBER:
            case BOOLEAN:
                s = "" + item.getValue();
                break;
            case NULL:
                s = "null";
                break;
            default:
                throw new RuntimeException("Program error. Not implemented.");
        }
        return s;
    }

    /**
     * Check if name is integer.
     * @param name
     * @return blank string if not, else return the string.
     */
    private String intAsNameCheck(String name) {

        final char c = name.charAt(0);
        // what if string = 0?
        // error - can we use empty string as name? Don't think so.
        // so this will throw exception if name length is 0.
        if (!Helper.isNumber(c)) {
            return "";
        }

        boolean isNumber = false;
        for (char nc : name.toCharArray()) {
            isNumber = Helper.isNumber(nc);
            if (!isNumber) {
                break;
            }
        }
        // todo: correct or not correct?
        return (isNumber ? name : "");
    }
}
