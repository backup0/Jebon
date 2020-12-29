package snarky.jebon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

class JebonTree {

    protected static final int ROOT_INDEX = 0;
    protected static final int NON_OBJECT = -1;

    private final ArrayList<JebonContainer> nodes = new ArrayList<>();

    protected JebonTree(boolean isObject) {
        nodes.add(ROOT_INDEX, new JebonContainer(isObject));
    }

    protected int addItem(int objIndex, JSONItem item) {

        final boolean isObj = (item.getType() == JSONTypes.OBJECT) || item.getType() == JSONTypes.ARRAY;
        if (isObj) {
            return storeObject(objIndex, item);
        }
        else {
            storePrimitive(objIndex, item);
            return NON_OBJECT;
        }
    }

    protected JebonContainer getObject(int index) {
        if (index < nodes.size()) {
            return nodes.get(index);
        }
        else {
            return null;
        }
    }

    private int storeObject(int parentIndex, JSONItem item) {
        // attach to parent
        // this is where the obj will be
        final int index = nodes.size();
        // add there - if not object, it's an array then.
        nodes.add(index, new JebonContainer(item.getType() == JSONTypes.OBJECT));
        // create a reference in there
        nodes.get(parentIndex).put(item.getName(), index);
        return index;
    }

    private void storePrimitive(int objIndex, JSONItem item) {
        final JebonContainer obj = nodes.get(objIndex);
        obj.put(item.getName(), item);
    }

    protected JSONItem getItem(String... key) {

        if (key.length == 0) {
            return getRootItem();
        }

        final ArrayList<String> keys = new ArrayList<>(Arrays.asList(key));
        final String lastKey = keys.remove(keys.size() - 1);

        // the first item.
        JebonContainer obj = nodes.get(ROOT_INDEX);
        for (String s : keys) {
            final Object oo = obj.get(s);
            if (oo instanceof Integer) {
                // if oo is out of range, it's runtime exception then how does that happen anyway?
                // no need to check.
                obj = nodes.get((int) oo);
            }
            else {
                obj = null;
                break;
            }
        }

        if (obj == null) {
            // no value - not in the json
            return null;
        }

        final Object lastObj = obj.get(lastKey);
        if (lastObj == null) {
            // no such val
            return null;
        }

        final JSONItem rtnVal;
        if (lastObj instanceof Integer) {
            // meaning this is an object - retrieve the object, and pass back list of keys
            final JebonContainer val = nodes.get((int) lastObj);
            final String[] valKeys = val.keySet().toArray(new String[0]);
            // if it's an array ... the keys are numbers?
            final JSONTypes type =  val.isObject() ? JSONTypes.OBJECT : JSONTypes.ARRAY;
            rtnVal = new JSONItem(lastKey, type, valKeys);
        }
        else {
            rtnVal = (JSONItem) lastObj;
        }

        return rtnVal;
    }

    private JSONItem getRootItem() {
        final JebonContainer obj = nodes.get(ROOT_INDEX);
        final String[] valKeys = obj.keySet().toArray(new String[0]);
        // if it's an array ... the keys are numbers?
        final JSONTypes type =  obj.isObject() ? JSONTypes.OBJECT : JSONTypes.ARRAY;
        return new JSONItem("", type, valKeys);
    }

    public String toString () {

        final StringBuilder sbs = new StringBuilder();
        for (JebonContainer o : nodes) {
            for (Map.Entry<String, Object> x : o.entrySet()) {
                sbs.append(x.getKey()).append("/").append(x.getValue()).append("; ");
            }
            sbs.append("\n");
        }
        return sbs.toString();
    }
}
