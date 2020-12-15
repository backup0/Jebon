package snarky.jebon;

class StringFinder extends Finder {

    // " has already been found
    // stuff
    // if \- escape
    // " not allowed; unless it i's the end; if we find this it's the end

    private enum CharacterTypes {
        ESCAPE, UNICODE, UNICODE_LOW, ANY, TERMINATOR
    }

    private JSONItem rtnValue = null;
    private boolean stringRead = false;

    private CharacterTypes charTypeToSearch = CharacterTypes.ANY;

    private final StringBuilder sbs = new StringBuilder();
    private final StringBuilder unicodeBuffer1 = new StringBuilder();
    private final StringBuilder unicodeBuffer2 = new StringBuilder();

    protected void update(String c) throws JebonException {

        switch (charTypeToSearch) {
            case ESCAPE:
                processEscapedChar(c);
                break;
            case UNICODE:
                processUniCode(c);
                break;
            case UNICODE_LOW:
                processUnicodeLow(c);
                break;
            case ANY:
                processAnyCharacter(c);
                break;
            case TERMINATOR:
                processTerminator(c);
                break;
            default:
                throw new RuntimeException("Program error. Not implemented.");
        }
    }

    private void processTerminator(String c) throws JebonException {

        if (Helper.isTerminator(c)) {
            rtnValue = new JSONItem("", JSONTypes.STRING, sbs.toString());
            // done
            return;
        }

        if (!Helper.isWhiteSpace(c)) {
            throw new JebonException("Unexpected character.");
        }
    }

    private void processEscapedChar(String c) throws JebonException {

        // ",\, /, b, f, n, r, t, uhex
        final String[] escChars = new String[]{"u", "\"","\\", "/", "b", "f", "n", "r", "t"};
        boolean ok = false;
        for (String es : escChars) {
            // ignore case, yes.
            if (c.equalsIgnoreCase(es)) {
                ok = true;
                break;
            }
        }

        if (!ok) {
            throw new JebonException("Unexpected escaped character: " + c + ".");
        }

        if (c.equalsIgnoreCase(escChars[0])) {
            // find hex next
            charTypeToSearch = CharacterTypes.UNICODE;
        }
        else {
            appendEscapedCharacter(c);
            charTypeToSearch = CharacterTypes.ANY;
        }
    }

    private void processAnyCharacter(String c) throws JebonException {

        switch (c) {
            case "\"":
                // opening " isn't sent to this class.
                charTypeToSearch = CharacterTypes.TERMINATOR;
                validateString();
                stringRead = true;
                break;
            case "\\":
                charTypeToSearch = CharacterTypes.ESCAPE;
                break;
            default:
                sbs.append(c);
                break;
                
        }
    }

    private void appendEscapedCharacter(String c) throws JebonException {
        // {'u', '"','\\', '/', 'b', 'f', 'n', 'r', 't'};
        switch (c) {
            case "\"":
                sbs.append("\"");
                break;
            case "\\":
                sbs.append("\\");
                break;
            case "/":
                sbs.append("/");
                break;
            case "b":
                sbs.append("\b");
                break;
            case "f":
                sbs.append("\f");
                break;
            case "n":
                sbs.append("\n");
                break;
            case "r":
                sbs.append("\r");
                break;
            case "t":
                sbs.append("\t");
                break;
            default:
                throw new JebonException("Illegal escaped character.");
        }
    }

    private void processUnicodeLow(String c) throws JebonException {

        final String unicodeBuffer2Flag = "?";
        if (unicodeBuffer2.length() > 0) {
            // we've already flagged this.
            if (unicodeBuffer2.toString().equals(unicodeBuffer2Flag)) {
                // we set this when we found \
                // next item expected is u;
                // which is unicode escape ???
                unicodeBuffer2.setLength(0);
                // the buffer has to be reset anyway whether we're looking for low surrogate or else.
                if (c.equalsIgnoreCase("u")) {
                    // search unicode character as usual
                    charTypeToSearch = CharacterTypes.UNICODE;
                }
                else {
                    // other escaped character
                    // write back whatever in the first buffer.
                    writeOrphanSurrogate();
                    charTypeToSearch = CharacterTypes.ESCAPE;
                    processEscapedChar(c);
                }
            }
            else {
                throw new RuntimeException("Program error.");
            }
        }
        else {
            // still empty, so we're looking @ the first character, which should be \
            // anything else is not escape character.
            if (c.equals("\\")) {
                // 'flag it'
                unicodeBuffer2.append(unicodeBuffer2Flag);
            }
            else {
                // any other character
                // there's no surrogate
                // the 2nd buffer is still empty.
                writeOrphanSurrogate();
                charTypeToSearch = CharacterTypes.ANY;
                processAnyCharacter(c);
            }
        }
    }

    private void writeOrphanSurrogate() {
        final int i = Integer.parseInt(unicodeBuffer1.toString(), 16);
        sbs.append(Character.toString(i));
        unicodeBuffer2.setLength(0);
    }

    private void processUniCode(String c) throws JebonException {

        // method throws exception
        checkIfCharLegal(c);

        if (unicodeBuffer1.length() < 4) {
            unicodeBuffer1.append(c);
            if (unicodeBuffer1.length() == 4) {
                final int codePoint = Integer.parseInt(unicodeBuffer1.toString(), 16);

                if (codePoint < Helper.MIN_CODE_POINT) {
                    throw new JebonException("Illegal character.");
                }
                // uses 1 char only ..
                if (Character.isBmpCodePoint(codePoint)) {
                    sbs.append(Character.toString(codePoint));
                    unicodeBuffer1.setLength(0);
                    unicodeBuffer2.setLength(0);
                }
                // else we keep the first part.
                // either; we're @ the end of this.
                charTypeToSearch = CharacterTypes.ANY;
            }
        }
        else {
            unicodeBuffer2.append(c);
            // we've read all 4; now
            if (unicodeBuffer2.length() == 4) {
                final char c1 = Character.highSurrogate(Integer.parseInt(unicodeBuffer1.toString(), 16));
                final char c2 = Character.lowSurrogate(Integer.parseInt(unicodeBuffer2.toString(), 16));
                final int codePoint = Character.toCodePoint(c1, c2);

                if (codePoint < Helper.MIN_CODE_POINT || codePoint > Helper.MAX_CODE_POINT) {
                    throw new JebonException("Illegal character.");
                }
                sbs.append(Character.toString(codePoint));
                unicodeBuffer1.setLength(0);
                unicodeBuffer2.setLength(0);
                charTypeToSearch = CharacterTypes.ANY;
            }
        }
    }

    private void checkIfCharLegal(String c) throws JebonException {

        final String[] allowedChars;
        allowedChars = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
        boolean ok = false;
        for (String c1 : allowedChars) {
            // hex, any case should be fine
            if (c.equalsIgnoreCase(c1)) {
                ok = true;
                break;
            }
        }

        if (!ok) {
            // wrong
            throw new JebonException("Illegal hex character: " + c + ".");
        }
    }

    private void validateString() throws JebonException {

        final int[] codepoints = sbs.toString().codePoints().toArray();
        for (int i : codepoints) {
            if (i < Helper.MIN_CODE_POINT || i > Helper.MAX_CODE_POINT) {
                throw new JebonException("Invalid character in string. CP: " + i + "/");
            }
        }
    }

    protected String getString() {

        if (stringRead) {
            return sbs.toString();
        }
        else {
            return null;
        }
    }

    @Override
    protected JSONItem getValue() {

        return rtnValue;
    }
}
