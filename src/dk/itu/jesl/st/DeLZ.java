package dk.itu.jesl.st;

import java.io.*;

public class DeLZ {
    public static void main(String[] args) throws IOException {
        DataInputStream in = new DataInputStream(new FileInputStream(args[0]));
        StringBuilder b = new StringBuilder();
        try {
            while (true) {
                int d = in.readInt();
                if (d == 0) b.append((char) in.readInt());
                else b.append(b, b.length()-d, b.length()-d+in.readInt());
            }
        } catch (EOFException e) { /* fine */ }
        Writer w = new OutputStreamWriter(new FileOutputStream(args[1]), "UTF-8");
        w.append(b);
        w.close();
    }
}
