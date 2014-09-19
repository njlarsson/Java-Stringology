package dk.itu.jesl.st;

import java.io.*;

public class DeRot {
    public static void main(String[] args) throws IOException {
        DataInputStream in = new DataInputStream(new FileInputStream(args[0]));
        StringBuilder b = new StringBuilder();
        try {
            while (true) {
                int d = in.readInt();
                if (d == 0) b.append((char) in.readInt());
                else {
                    int p = b.length()-d;
                    int l = in.readInt();
                    int yl = in.readInt();
                    b.append(b, p+yl, p+l);
                    b.append(b, p, p+yl);
                }
            }
        } catch (EOFException e) { /* fine */ }
        Writer w = new OutputStreamWriter(new FileOutputStream(args[1]), "UTF-8");
        w.append(b);
        w.close();
    }
}
