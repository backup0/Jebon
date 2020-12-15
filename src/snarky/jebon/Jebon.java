package snarky.jebon;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Jebon {

    private void doThing() throws JebonException {

        Path p = Paths.get("D:\\etemp\\json-exp.json" );
        w(new JSONReader(p));
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
