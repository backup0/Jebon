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

    private CharacterTypes charTypeToSearch = CharacterTypes.ANY;
    private final StringBuilder sbs = new StringBuilder();

    private final StringBuilder uniCodeBuffer1 = new StringBuilder();
    private final StringBuilder uniCodeBuffer2 = new StringBuilder();

    protected void update(char c) throws JebonException {

        if (uniCodeBuffer1.length() == 4 && uniCodeBuffer2.length() == 0) {
            // first unicode char has been found, and it's a high surrogate
            // the 2nd one is empty.
        }

        switch (charTypeToSearch) {

            case ESCAPE:
                processEscapeChar(c);
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
                throw new RuntimeException("Program error.");
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

    private void processEscapeChar(char c) throws JebonException {
        //",\, /, b, f, n, r, t, uhex
        final char[] chars = new char[]{'u', '"','\\', '/', 'b', 'f', 'n', 'r', 't'};
        boolean ok = false;
        for (char o : chars) {
            if (c == o) {
                ok = true;
                break;
            }
        }

        if (!ok) {
            throw new JebonException("Unexpected character: " + c + ".");
        }

        if (c == chars[0]) {
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
                charTypeToSearch = CharacterTypes.TERMINATOR;
                break;
            case '\\':
                charTypeToSearch = CharacterTypes.ESCAPE;
                break;
            default:
                sbs.append(c);
                break;
        }
    }

    private void appendEscapedCharacter(char c) {
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
                throw new RuntimeException("Program error.");
        }
    }

    private void processUnicodeLow(char c) throws JebonException {

        if (uniCodeBuffer2.length() > 0) {
            // we've already flagged this.
            final char fc = uniCodeBuffer2.charAt(0);
            if (fc == '-') {
                // we set this when we found \
                // next item expected is u;
                // which is unicode escape ???
                uniCodeBuffer2.setLength(0);
                // the buffer has to be reset anyway whether we're looking for low surrogate or else.
                if (c == 'u') {
                    // search unicode character as usual
                    charTypeToSearch = CharacterTypes.UNICODE;
                }
                else {
                    // other escaped character
                    // write back whatever in the first buffer.
                    writeOrphanSurrogate();
                    charTypeToSearch = CharacterTypes.ESCAPE;
                    processEscapeChar(c);
                }
            }
            else {
                throw new RuntimeException("Program error.");
            }
        }
        else {
            // still empty, so we're looking @ the first character, which should be \
            // anything else is not escape character.
            if (c == '\\') {
                // 'flag it'
                uniCodeBuffer2.append('-');
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
        final int i = Integer.parseInt(uniCodeBuffer1.toString(), 16);
        sbs.append(Character.toString(i));
        uniCodeBuffer2.setLength(0);
    }

    private void processUniCode(char c) throws JebonException {

        final char a = Character.toLowerCase(c);
        char[] allowedChars;
        allowedChars = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        boolean ok = false;
        for (char o : allowedChars) {
            if (a == o) {
                ok = true;
                break;
            }
        }

        if (!ok) {
            // wrong
            throw new JebonException("Illegal character.");
        }

        if (uniCodeBuffer1.length() < 4) {
            uniCodeBuffer1.append(c);
            if (uniCodeBuffer1.length() == 4) {
                final int i = Integer.parseInt(uniCodeBuffer1.toString(), 16);
                // uses 1 char only ..
                if (Character.isBmpCodePoint(i)) {
                    sbs.append(Character.toString(i));
                    uniCodeBuffer1.setLength(0);
                    uniCodeBuffer2.setLength(0);
                } ?? code point has to be between
                // else we keep the first part.
                // either; we're @ the end of this.
                charTypeToSearch = CharacterTypes.ANY;
            }
        }
        else {
            uniCodeBuffer2.append(c);
            // we've read all 4; now
            if (uniCodeBuffer2.length() == 4) {
                final char c1 = Character.highSurrogate(Integer.parseInt(uniCodeBuffer1.toString(), 16));
                final char c2 = Character.lowSurrogate(Integer.parseInt(uniCodeBuffer2.toString(), 16));
                final int codePoint = Character.toCodePoint(c1, c2);
                ?? code point has to be between
                sbs.append(Character.toString(codePoint));
                uniCodeBuffer1.setLength(0);
                uniCodeBuffer2.setLength(0);
                charTypeToSearch = CharacterTypes.ANY;
            }
        }
    }

    protected String e() {
        if (charTypeToSearch == CharacterTypes.TERMINATOR) {
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
