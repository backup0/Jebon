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

    protected void update(char c) throws JebonException {

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

    private void processTerminator(char c) throws JebonException {

        if (Helper.isTerminator(c)) {
            rtnValue = new JSONItem("", JSONTypes.STRING, sbs.toString());
            // done
            return;
        }

        if (!Helper.isWhiteSpace(c)) {
            throw new JebonException("Unexpected character.");
        }
    }

    private void processEscapedChar(char c) throws JebonException {

        // ",\, /, b, f, n, r, t, uhex
        // no big cap.
        final char[] escChars = new char[]{'u', '"', '\\', '/', 'b', 'f', 'n', 'r', 't'};
        boolean ok = false;
        for (char o : escChars) {
            if (c == o) {
                ok = true;
                break;
            }
        }

        if (!ok) {
            throw new JebonException("Unexpected character: " + c + ".");
        }

        if (c == escChars[0]) {
            // find hex next
            charTypeToSearch = CharacterTypes.UNICODE;
        }
        else {
            appendEscapedCharacter(c);
            charTypeToSearch = CharacterTypes.ANY;
        }
    }

    private void processAnyCharacter(char c) {

        switch (c) {
            case '"':
                // opening " isn't sent to this class.
                // must be the end. Unless it's escaped, we treat this as the end of string,
                // still not the end of field.
                charTypeToSearch = CharacterTypes.TERMINATOR;
                stringRead = true;
                break;
            case '\\':
                charTypeToSearch = CharacterTypes.ESCAPE;
                break;
            default:
                // no need to set char type to search because it is already ANY.
                sbs.append(c);
                break;
        }
    }

    private void appendEscapedCharacter(char c) throws JebonException {
        // {'u', '"','\\', '/', 'b', 'f', 'n', 'r', 't'};
        switch (c) {
            case '"':
                sbs.append("\"");
                break;
            case '\\':
                sbs.append("\\");
                break;
            case '/':
                sbs.append("/");
                break;
            case 'b':
                sbs.append("\b");
                break;
            case 'f':
                sbs.append("\f");
                break;
            case 'n':
                sbs.append("\n");
                break;
            case 'r':
                sbs.append("\r");
                break;
            case 't':
                sbs.append("\t");
                break;
            default:
                throw new JebonException("Illegal escaped character.");
        }
    }

    private void processUnicodeLow(char c) throws JebonException {

        if (unicodeBuffer2.length() > 0) {
            // we've already flagged this.
            // we set this when we found \
            // next item expected is u;
            // which is unicode escape ???
            unicodeBuffer2.setLength(0);
            // the buffer has to be reset anyway whether we're looking for low surrogate or else.
            if (c == 'u') {
                // search unicode character
                charTypeToSearch = CharacterTypes.UNICODE;
            }
            else {
                // other escaped character
                // write back whatever in the first buffer.
                // rest both - w're done with this.
                writeOrphanSurrogate();
                unicodeBuffer1.setLength(0);
                unicodeBuffer2.setLength(0);
                charTypeToSearch = CharacterTypes.ESCAPE;
                processEscapedChar(c);
            }
        }
        else {
            // still empty, so we're looking @ the first character, which should be \
            // anything else is not escape character.
            if (c == '\\') {
                // 'flag it'
                unicodeBuffer2.append("flag");
            }
            else {
                // any other character
                // there's no surrogate
                // the 2nd buffer is still empty.
                writeOrphanSurrogate();
                unicodeBuffer1.setLength(0);
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

    private void processUniCode(char c) throws JebonException {

        if (unicodeBuffer1.length() < 4) {
            unicodeBuffer1.append(c);
            if (unicodeBuffer1.length() == 4) {
                final int codePoint;
                try {
                    codePoint = Integer.parseInt(unicodeBuffer1.toString(), 16);
                }
                catch (Exception e) {
                    throw new JebonException(e.getMessage());
                }

                if (codePoint < Helper.MIN_CODE_POINT) {
                    throw new JebonException("Illegal character.");
                }
                // uses 1 char only ..
                if (Character.isBmpCodePoint(codePoint)) {
                    sbs.append(Character.toString(codePoint));
                    unicodeBuffer1.setLength(0);
                    unicodeBuffer2.setLength(0);
                    charTypeToSearch = CharacterTypes.ANY;
                }
                else {
                    charTypeToSearch = CharacterTypes.UNICODE_LOW;
                }
            }
        }
        else {
            // first buffer read
            unicodeBuffer2.append(c);
            // we've read all 4; now
            if (unicodeBuffer2.length() == 4) {
                final int codePoint;
                try {
                    final char c1 = Character.highSurrogate(Integer.parseInt(unicodeBuffer1.toString(), 16));
                    final char c2 = Character.lowSurrogate(Integer.parseInt(unicodeBuffer2.toString(), 16));
                    codePoint = Character.toCodePoint(c1, c2);
                }
                catch (Exception e) {
                    throw new JebonException(e.getMessage());
                }

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
