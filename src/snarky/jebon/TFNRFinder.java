package snarky.jebon;

class TFNRFinder extends Finder {

    // todo: probably not 100% correct here ...

    // true, false, or null
    private final StringBuilder sbs = new StringBuilder();
    private JSONItem rtnVal = null;
    boolean done = false;

    @Override
    protected void update(String c) throws JebonException {

        if (Helper.isTerminator(c)) {
            rtnVal = getItem();
            if (rtnVal == null) {
                throw new JebonException("Unexpected character.");
            }
            return;
        }

        if (!done) {
            if (Helper.isWhiteSpace(c)) {
                done = true;
            }
            else {
                sbs.append(c);
            }
        }
        else {
            if (!Helper.isWhiteSpace(c)) {
                throw new JebonException("Unexpected character");
            }
        }
    }

    private JSONItem getItem() {

        final JSONItem i;
        switch (sbs.toString().toLowerCase()) {
            case "true":
                i = new JSONItem("", JSONTypes.BOOLEAN, true);
                break;
            case "false":
                i = new JSONItem("", JSONTypes.BOOLEAN, false);
                break;
            case "null":
                i = new JSONItem("", JSONTypes.NULL, null);
                break;
            default:
                i = null;
                break;
        }
        return i;
    }

    @Override
    protected JSONItem getValue() {
        return rtnVal;
    }
}
