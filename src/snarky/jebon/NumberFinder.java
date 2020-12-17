package snarky.jebon;

class NumberFinder extends Finder {

    private final StringBuilder sbs = new StringBuilder();
    private JSONItem rtnVal = null;
    private boolean done = false;

    @Override
    protected void update(char c) throws JebonException {

        if (Helper.isTerminator(c)) {
            // if terminator end immediately, we've read the entire field
            rtnVal = new JSONItem("", JSONTypes.NUMBER, Double.parseDouble(sbs.toString()));
            return;
        }

        if (!done) {
            if (Helper.isWhiteSpace(c)) {
                // end of number sequence.
                done = true;
            }
            else {
                sbs.append(c);
            }
        }
        else {
            if (!Helper.isWhiteSpace(c)) {
                throw new JebonException("Unexpected character.");
            }
        }
    }

    @Override
    protected JSONItem getValue() {
        return rtnVal;
    }
}
