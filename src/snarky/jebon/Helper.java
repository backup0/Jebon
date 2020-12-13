package snarky.jebon;

class Helper {

    protected static final int MIN_CODE_POINT = 0x0020;
    protected static final int MAX_CODE_POINT = 0x10ffff;

    protected static boolean isWhiteSpace(String c) {

        final int codePoint = c.codePointAt(0);
        final int[] whiteSpaceChars = new int[]{0x0020, 0x000A, 0x000D, 0x0009};
        for (int ws : whiteSpaceChars) {
            if (codePoint == ws) {
                return true;
            }
        }
        return false;
    }

    protected static boolean isNumber(String c) {

        final String[] nums = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

        for (String n : nums) {
            if (n.equalsIgnoreCase(c)) {
                return true;
            }
        }
        return false;
    }

    protected static boolean isTerminator(String c) {

        final String[] terms = new String[] {"}", "]", ","};
        for (String s : terms) {
            if (s.equals(c)) {
                return true;
            }
        }
        return false;
    }
}
