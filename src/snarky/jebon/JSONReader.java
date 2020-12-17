package snarky.jebon;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class JSONReader {

    private final int queueIndex = 0;
    private final ArrayList<ObjectHandler> objQueue = new ArrayList<>();
    private final char[] chars;
    private int index = 0;

    private final JebonTree jTree;

    public JSONReader(byte[] b) throws JebonException {
        this(new String(b, StandardCharsets.UTF_8));
    }

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
                // queue isn't empty, but we're run out of text.
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

            if (c == '[') {
                return false;
            }
        }
        throw new JebonException("Probably invalid JSON.");
    }

    protected JSONItem getItem(String... key) {
        return jTree.getItem(key);
    }

    public String toString() {
        return jTree.toString();
    }
}
