package snarky.jebon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class Jebon {

    private void doThing() throws JebonException {

        // Chceks.

        try {
            String s = Files.readString(Paths.get("D:\\etemp\\json-string-test.txt" )) + "\"";
            StringFinder sf = new StringFinder();

            for (char c : s.toCharArray()) {
                sf.update(c);
            }
            w(sf.getString());

        } catch (IOException e) {
            e.printStackTrace();
        }


        /*
        JSONCreator jkar = new JSONCreator(true);
        jkar.put(123.45, "point", "is", "taken", "@1");
        jkar.put(SpecialType.JSONNull, "point", "is", "taken", "kll-you");
        jkar.put(true, "point", "is", "taken", "@t");
        jkar.put(false, "point", "is", "taken", "@you");
        jkar.put(11, "point", "is", "taken", "aray", "0");
        jkar.put(12, "point", "is", "taken", "aray", "1");
        jkar.put(13, "point", "is", "taken", "aray", "2");
        jkar.put(14, "point", "is", "taken", "aray", "3");
        w(jkar);
        */

        /*
        Path p = Paths.get("D:\\etemp\\json-exp.json" );
        //Path p = Paths.get("D:\\etemp\\j-large.json" );

        JSONReader jr = new JSONReader(p);
        JSONItem item = jr.getItem("obj");
        w(item.getValue());
        String x = (String) item.getValue();
        char[] e = x.toCharArray();
        w(Character.isHighSurrogate(e[0]));
        w(Character.isLowSurrogate(e[1]));
        StringBuffer xyz = new StringBuffer();
        xyz.append(e[0]);
        xyz.append(e[1]);
        w(xyz.toString());
        /*
        if (item != null) {
            w(item.getType());
            //w(Arrays.toString((String[]) item.getValue()));
        }
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
