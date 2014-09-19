package dk.itu.jesl.st;

import java.io.*;

public class RotFact {
    public static void main(String[] args) throws IOException {
        String s =
            readFile(args[0]);
            // "She sells seashells by the seashore. " +
            // "The shells she sells are surely seashells. " +
            // "So if she sells shells on the seashore, " +
            // "I'm sure she sells seashore shells.";
        DataOutputStream out = new DataOutputStream(new FileOutputStream(args[1]));
        EOTD st = new EOTD(new StringText(s, 0), 3, 2);
        EOTD.Pointer xptr = st.pointer(), yptr = st.pointer();
        int i = 0, factors = 0;
        StringText text = new StringText(s, 0);
        while (i < s.length()) {
            ++factors;
            xptr.reset();
            xptr.matchForward(text, i, s.length()-i);
            int xl = xptr.sdepth(), yl = 1;
            if (xl == 0) {      // no match at all
                out.writeInt(0); out.writeInt(s.charAt(i));
                // System.out.println("0: '" + s.charAt(i) + "'");
                i++;
            } else {            // try to extend match
                yptr.reset(); 
                yptr.matchForward(text, i+xl, s.length()-(i+xl));
                yl = yptr.sdepth();
                while (true) {
                    if (yl == 0) { // rotation doesn't help
                        out.writeInt((int) (i-xptr.position())); out.writeInt(xl); out.writeInt(0);
                        // System.out.println(xl + ", " + (i-xptr.position()) + ", 0");
                        break;
                    }
                    if (yptr.matchForward(text, i, xl)) { // rotation found
                        out.writeInt((int) (i-yptr.position())); out.writeInt(xl+yl); out.writeInt(yl);
                        // System.out.println((xl+yl) + ", " + (i-yptr.position()) + ", " + yl);
                        break;
                    }
                    if (--yl > 0) {
                        yptr.reset();
                        yptr.matchForward(text, i+xl, yl);
                    }
                }
                i += xl+yl;
            }
            st.update(xl+yl);
        }
        out.close();
        System.out.println("Rot factors: " + factors);
    }

    private static String readFile(String fnam) throws IOException {
        StringBuilder b = new StringBuilder();
        Reader r = new InputStreamReader(new FileInputStream(fnam), "UTF-8");
        while (true) {
            int c = r.read();
            if (c < 0) break;
            b.append((char) c);
        }
        return b.toString();
    }
}
