package snarky.jebon;

import java.util.Scanner;

public class Jebon {

    private void doThing() throws JebonException {

        // if there's space in between, .. yeah. should be wrong though.
        // so the code is correct or some reason, but need to make sure if there's something else .. what?
        // ok we need to set the hex1 and hex2 to 0,  .. the pair, this istricky isn;t it, the pair can be
        /*
        if we ave two, the enxt .. we expect te enxt one if it's high surrogate, ele ope, no the next onr has o be'
            of course the ext one can be anything,  we'll ebd up wrong character if ..'
                Ok, say u1 is igh surrogate, we maintain hex1, then we look up hex2, or anuhign else ..
        >> the enxt one can be anything .. it can be othercharacters, or it can be another esaped .. ssee? we dot want to use
                this later after skipping  afe .. see?
         */

        w(new JSONReader());

        /*
        String s = "fakz you \\u007f \\uD834 \\uDD1E \\uD834 \\uDD1E \"";
        StringFinder sf = new StringFinder();

        for (char c : s.toCharArray()) {
            sf.update(c);
        }

        w("--", sf.e());
        */
    }

    public static void main(String[] args)
    {
        String opx;
        Scanner scanner = new Scanner(System.in);

        do {
            try {
                final Jebon m = new Jebon();
                m.doThing();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            System.out.println("? ");
            opx = scanner.nextLine();

        } while (!"x".equalsIgnoreCase(opx));
    }

    public static void w(Object... t) {
        StringBuilder sb = new StringBuilder();
        for (Object s : t) {
            sb.append(s);
        }
        System.out.println(sb.toString());
    }
}
