package snarky.jebon;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class JSONReader {

    private final int queueIndex = 0;
    private final ArrayList<ObjectHandler> objQueue = new ArrayList<>();
    private final char[] chars;
    private int index = 0;

    private final JebonTree jTree;

    /**
     * Create json reader.
     * @param p Path to json file.
     * @throws JebonException if json is invalid.
     */
    public JSONReader(Path p) throws JebonException {

        try {
            final String s = Files.readString(p);
            chars = s.toCharArray();

            final boolean isObj = findOpener();
            jTree = new JebonTree(isObj);
            readJSON(isObj);
        }
        catch (Exception e) {
            throw new JebonException(e.getMessage());
        }
    }

    /**
     * Create json reader.
     * @param s The json.
     * @throws JebonException if json is invalid.
     */
    public JSONReader(String s) throws JebonException {

        try {
            chars = s.toCharArray();
            final boolean isObj = findOpener();
            jTree = new JebonTree(isObj);
            readJSON(isObj);
        } catch (Exception e) {
            throw new JebonException(e.getMessage());
        }
    }

    private void readJSON(boolean isObj) throws JebonException {

        if (isObj) {
            objQueue.add(new ObjectHandler(true, jTree, JebonTree.ROOT_INDEX));
        }
        else {
            objQueue.add(new ObjectHandler(false, jTree, JebonTree.ROOT_INDEX));
        }
        process();
    }

    private void process() throws JebonException {

        while (!objQueue.isEmpty()) {

            if (index >= chars.length) {
                // queue isn't empty, but we've run out of text.
                throw new RuntimeException("Syntax error");
            }
            // once done the obj is removed
            // no problem.
            processObject(objQueue.get(queueIndex));
        }

        // whitespaces after final } or ] are ok.
        while (index < chars.length) {

            char c = chars[index++];
            if (!Helper.isWhiteSpace(c)) {
                throw new JebonException("Wayward character(s).");
            }
        }
    }

    private void processObject(ObjectHandler o) throws JebonException {

        while (index < chars.length) {

            final char c = chars[index++];
            // found a new object
            // we process it.
            final ObjectHandler.ReturnFlag f = o.update(c);
            if (f == ObjectHandler.ReturnFlag.OBJECT) {
                objQueue.add(queueIndex, new ObjectHandler(true, jTree, o.getLastChild()));
                return;
            }

            if (f == ObjectHandler.ReturnFlag.ARRAY) {
                objQueue.add(queueIndex, new ObjectHandler(false, jTree, o.getLastChild()));
                return;
            }

            if (f == ObjectHandler.ReturnFlag.DONE) {
                objQueue.remove(queueIndex);
                return;
            }
        }
    }

    private boolean findOpener() throws JebonException {

        while (index < chars.length) {

            final char c = chars[index++];
            if (Helper.isWhiteSpace(c)) {
                continue;
            }

            if (c == '{') {
                return true;
            }
            else if (c == '[') {
                return false;
            }
            else {
                break;
            }
        }
        throw new JebonException("Probably invalid JSON.");
    }

    /**
     * Get the value assigned to 'key'. The <i>key</i> is the full 'path' to the item,
     * that is the name of the field preceded by the names of all the nested objects.
     * For example <code>getItem("email", "work")</code> means get the value for the field
     * "work" which is inside the object "email", which is inside the root object.</p>
     * <br>
     * Suppose we have the following json
     * <pre>
     * {
     * 	"email" :  {
     * 		"work" : "email@domain", "personal" : "email2@domain", ..
     *    }, ..
     * }
     * </pre>the aforementioned method call will return "email@domain".
     * <br><br>
     * Values inside arrays can be retrieved in the same way,
     * the last item in the path is the index. For example <code>getItem("email", "1")</code>
     * means get the value inside an array "email" at index "1".
     * <br>
     * Suppose we have the following json
     * <pre>
     * {
     * 	"email" :  ["email1@domain", "email2@domain", ....], ..
     * }
     * </pre>the method call will return "email2@domain".
     *
     * @param key The key.
     * @return The value associated with the key wrapped inside <code>JSONItem</code> object.
     */
    public JSONItem getItem(String... key) {
        return jTree.getItem(key);
    }
}
