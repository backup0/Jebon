package snarky.jebon;

class ObjectHandler {

    protected enum ReturnFlag {
        OBJECT,
        ARRAY,
        CONTINUE,
        DONE
    }

    private enum Operations {
        FIND_NAME_START,
        READ_NAME,
        FIND_COLON,
        DETERMINE_VAL_TYPE,
        READ_VALUE,
        FIND_OBJ_END
    }

    private Finder valueReader = null;
    private StringFinder nameReader = null;
    // if this is an array, this is the 'name'
    private int arrayIndex = -1;

    // when we encounter object/array, a tree node is create
    // this is the index of that node.
    private int lastChild = -1;

    private final boolean isObject;
    private final int treeIndex;
    private final JebonTree jsonTree;

    private Operations op;

    protected ObjectHandler(boolean obj, JebonTree jTree, int treeIndex) {
        isObject = obj;
        // array does not use name. No need to find name.
        op = isObject ? Operations.FIND_NAME_START : Operations.DETERMINE_VAL_TYPE;
        jsonTree = jTree;
        this.treeIndex = treeIndex;
    }

    protected ReturnFlag update(char c) throws JebonException {

        final ReturnFlag rtn;
        switch (op) {
            case FIND_NAME_START:
                rtn = findTheStartOfName(c);
                break;
            case READ_NAME:
                readName(c);
                rtn = ReturnFlag.CONTINUE;
                break;
            case FIND_COLON:
                findColon(c);
                rtn = ReturnFlag.CONTINUE;
                break;
            case DETERMINE_VAL_TYPE:
                rtn = determineValueType(c);
                break;
            case READ_VALUE:
                rtn = readValue(c);
                break;
            case FIND_OBJ_END:
                rtn = findObjectEnding(c);
                break;
            default:
                throw new RuntimeException("Program error. Not implemented.");
        }
        return rtn;
    }

    private ReturnFlag readValue(char c) throws JebonException {

        if (valueReader == null) {
            throw new RuntimeException();
        }

        valueReader.update(c);
        final JSONItem item = valueReader.getValue();
        final ReturnFlag rtn;
        // white space is handled by value finder (to be ignored),
        // return value will be non-null when a terminator is found
        // terminator being ',', } or ].
        if (item != null) {
            // we've found the value
            interpretValue();
            // c is the terminator ...
            if (c == ',') {
                // we expect another value next
                op = isObject ? Operations.FIND_NAME_START :  Operations.DETERMINE_VAL_TYPE;
                rtn = ReturnFlag.CONTINUE;
            }
            else {
                // }, ] ok, else throw exception
                checkTerminator(c);
                rtn = ReturnFlag.DONE;
            }
        }
        else {
            rtn = ReturnFlag.CONTINUE;
        }
        return rtn;
    }

    private ReturnFlag findObjectEnding(char c) throws JebonException {
        // the value is an object(or an array)
        // this is handled by an instance of this class, when read is done, we try to find the terminator
        if (Helper.isWhiteSpace(c)) {
            return ReturnFlag.CONTINUE;
        }

        final ReturnFlag rtn;
        if (c == ',') {
            // we expect another value next
            op = isObject ? Operations.FIND_NAME_START :  Operations.DETERMINE_VAL_TYPE;
            rtn = ReturnFlag.CONTINUE;
        }
        else {
            // }, ]
            checkTerminator(c);
            rtn = ReturnFlag.DONE;
        }
        return rtn;
    }

    private void interpretValue() {

        final JSONItem val = valueReader.getValue();
        // items in array do not have names
        // name reader will be null if this is an array
        final String name = getName();

        switch (val.getType()) {

            case STRING:
                final JSONItem stringVal = new JSONItem(name, JSONTypes.STRING, val.getValue());
                jsonTree.addItem(treeIndex, stringVal);
                break;
            case OBJECT:
            case ARRAY:
                // not interpreted.
                break;
            case BOOLEAN:
                final JSONItem booleanVal = new JSONItem(name, JSONTypes.BOOLEAN, val.getValue());
                jsonTree.addItem(treeIndex, booleanVal);
                break;
            case NUMBER:
                final JSONItem numVal = new JSONItem(name, JSONTypes.NUMBER, val.getValue());
                jsonTree.addItem(treeIndex, numVal);
                break;
            case NULL:
                final JSONItem nullVal = new JSONItem(name, JSONTypes.NULL, null);
                jsonTree.addItem(treeIndex, nullVal);
                break;
            default:
                throw new RuntimeException("Program error. Not implemented.");
        }
    }

    private ReturnFlag findTheStartOfName(char c) throws JebonException {
        // while reading name space is allowed.
        if (Helper.isWhiteSpace(c)) {
            return ReturnFlag.CONTINUE;
        }

        if ((nameReader == null) && (c == '}')) {
            // this is an empty object
            // name reader is assigned after we've found "
            return ReturnFlag.DONE;
        }

        // }, ], ',' shouldn't be sent here; we use }, ], ',' to determine the end of value, so
        // those are read @ reading value, not here. Here the only possible values are ", whitespaces, and
        // } (if the object is empty).
        // the 2nd and subsequent 'rounds' are triggered by ','. If ',' terminates a value field, we expect another
        // value field, } or ] ends this object, so this will no be called again.

        if (c == '"') {
            // ok - we've found the starting point ..
            nameReader = new StringFinder();
            op = Operations.READ_NAME;
        }
        else {
            throw new JebonException("Unexpected character.");
        }
        return ReturnFlag.CONTINUE;
    }

    private void readName(char c) throws JebonException {
        // pass the value to string reader
        // ..
        // found string?
        nameReader.update(c);
        if (nameReader.getString() != null) {
            op = Operations.FIND_COLON;
        }
    }

    private void findColon(char c) throws JebonException {
        if (Helper.isWhiteSpace(c)) {
            return;
        }

        if (c == ':') {
            // ok
            op = Operations.DETERMINE_VAL_TYPE;
        }
        else {
            // exception
            throw new JebonException("Unexpected character.");
        }
    }

    private ReturnFlag determineValueType(char c) throws JebonException {

        if (Helper.isWhiteSpace(c)) {
            return ReturnFlag.CONTINUE;
        }

        // empty array ...
        if (arrayIndex == -1 && c == ']') {
            return ReturnFlag.DONE;
        }

        // the value may be overridden in the switch block. Just a reminder.
        op = Operations.READ_VALUE;
        // array 'name'
        if (!isObject) {
            arrayIndex++;
        }

        // - is for the start of negative number
        if (Helper.isNumber(c) || c == '-') {
            // process number
            valueReader = new NumberFinder();
            valueReader.update(c);
            return ReturnFlag.CONTINUE;
        }

        final ReturnFlag rtn;
        switch (c) {
            case '{':
                rtn = ReturnFlag.OBJECT;
                lastChild = jsonTree.addItem(treeIndex, new JSONItem(getName(), JSONTypes.OBJECT, null));
                op = Operations.FIND_OBJ_END;
                break;
            case '[':
                rtn = ReturnFlag.ARRAY;
                lastChild = jsonTree.addItem(treeIndex, new JSONItem(getName(), JSONTypes.ARRAY, null));
                op = Operations.FIND_OBJ_END;
                break;
            case '\"':
                valueReader = new StringFinder();
                rtn = ReturnFlag.CONTINUE;
                break;
            case 't':
            case 'f':
            case 'n':
                // true, false, null
                valueReader = new TFNRFinder();
                valueReader.update(c);
                rtn = ReturnFlag.CONTINUE;
                break;
            default:
                throw new JebonException("Unexpected character.");
        }
        return rtn;
    }

    private void checkTerminator(char c) throws JebonException {

        final boolean ok;
        if (isObject) {
            // if this is an object
            // true if c == } - everything else is false.
            ok = c == '}';
        }
        else {
            // if this is an array (not object)
            // true if c == ]
            // everything else is bad.
            ok = c == ']';
        }

        if (!ok) {
            throw new JebonException("Unexpected character");
        }
    }

    private String getName() {
        return (nameReader != null) ? nameReader.getString() : "" + arrayIndex;
    }

    protected int getLastChild() {
        return lastChild;
    }
}
