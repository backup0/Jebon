package snarky.jebon;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Jebon {

    private void doThing() throws JebonException {

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
