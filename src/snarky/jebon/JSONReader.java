package snarky.jebon;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class JSONReader {

    private final int queueIndex = 0;
    private final ArrayList<ObjectHandler> objQueue = new ArrayList<>();
    private final char[] chars;
    private int index = 0;

    private final JebonTree jTree;

    public static void zyx () throws Exception{

        byte[] b = Files.readAllBytes(Paths.get("D:\\etemp\\json-exp.json" ));
        String s0 = new String(b);
        final String s = Files.readString(Paths.get("D:\\etemp\\json-exp.json" ));

        JSONReader ds = new JSONReader();

        /*
        "obj1", "array", "#1" = x <-- put x inside "array" @ position 1;
        "obj1", "array", "#1" = y <-- new value overrides? What if ne value is obj and vice versa?

        "obj1", "array", "#1", "val1" = x <-- insert an object inside "array" @ position 1; then insert val1.
        YES it is that complicated. Here's the plan
        >> allow user to add object 'on the fly', just by stating liek my simpel json
        >> stuff inside array? if it has ONE #, followed by a number(s) and nothing else, it's an array So if you want
        >> "#" as name, then use that.
        >> "#1" as name, then use ## twice. ##1 -> #1
        Why not .. why no use alternative way? Why? You can ise a list, or you can use specual object.

         */


        //NumberFinder nf = new NumberFinder();
        //nf.test0(s0);
    }

    public JSONReader() {

        try {
            final String s = Files.readString(Paths.get("D:\\etemp\\json-exp.json" ));
            chars = s.toCharArray();
            final boolean isObj = findOpener();
            jTree = new JebonTree(isObj);
            if (isObj) {
                objQueue.add(new ObjectHandler(true, jTree, JebonTree.ROOT_INDEX));
            }
            else {
                objQueue.add(new ObjectHandler(false, jTree, JebonTree.ROOT_INDEX));
            }
            process();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void process() throws JebonException {

        while (!objQueue.isEmpty()) {

            if (index >= chars.length) {
                // queue isn't empty, but we're run out of text.
                throw new RuntimeException("W00tz");
            }
            // once done the obj is removed
            // no problem.
            processObject(objQueue.get(queueIndex));
        }

        // whitespaces after final } or ] are ok.
        while (index < chars.length) {

            char c = chars[index++];
            if (!Helper.isWhiteSpace(c)) {
                throw new RuntimeException("Danglings stuff ...");
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

    private boolean findOpener() {

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
        throw new RuntimeException("Probably invalid JSON.");
    }
}
