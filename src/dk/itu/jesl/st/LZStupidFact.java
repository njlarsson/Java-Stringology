package dk.itu.jesl.st;

import java.io.*;

public class LZStupidFact {
    public static void main(String[] args) throws IOException {
        String s =
            readFile(args[0]);
            // "She sells seashells by the seashore. " +
            // "The shells she sells are surely seashells. " +
            // "So if she sells shells on the seashore, " +
            // "I'm sure she sells seashore shells.";
        DataOutputStream out = new DataOutputStream(new FileOutputStream(args[1]));
        EOTD st = new EOTD(new StringText(s, 0), 3, 2);
        EOTD.Pointer ptr = st.pointer();
        int i = 0, factors = 0;
        StringText text = new StringText(s, 0);
        while (i < s.length()) {
            ++factors;
            ptr.reset();
            ptr.matchForward(text, i, s.length()-i);
            int l = ptr.sdepth();
            if (l == 0) {
                out.writeInt(0); out.writeInt(s.charAt(i));
                // System.out.println("0: '" + s.charAt(i) + "'");
                i++;
            } else {
                out.writeInt((int) (i-ptr.position())); out.writeInt(l);
                // System.out.println(l + ", " + (i-ptr.position()));
                i += l;
            }
            st.update();
        }
        out.flush();
        System.out.println("LZ factors: " + factors);
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
