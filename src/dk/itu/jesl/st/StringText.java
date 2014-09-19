package dk.itu.jesl.st;

public final class StringText implements Text {
    private final String s;
    private final int off;

    StringText(String s, int off) { this.s = s; this.off = off; }
    StringText(String s) { this(s, 0); }

    public int charAt(int pos) { return s.charAt(pos + off); }
    public int length() { return s.length() - off; }
}
