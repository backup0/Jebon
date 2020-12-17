package snarky.jebon;

class Helper {

    protected static final int MIN_CODE_POINT = 0x0020;
    protected static final int MAX_CODE_POINT = 0x10ffff;

    protected static boolean isWhiteSpace(char c) {

        final char[] ws = new char[]{0x0020, 0x000A, 0x000D, 0x0009};
        for (char c0 : ws) {
            if (c0 == c) {
                return true;
            }
        }
        return false;
    }

    protected static boolean isNumber(char c) {

        final char[] nums = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

        for (char n : nums) {
            if (c == n) {
                return true;
            }
        }
        return false;
    }

    protected static boolean isTerminator(char c) {

        final char[] terminators = new char[] {'}', ']', ','};
        for (char ct : terminators) {
            if (c == ct) {
                return true;
            }
        }
        return false;
    }
}
