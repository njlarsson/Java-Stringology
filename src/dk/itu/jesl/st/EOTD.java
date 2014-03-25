package dk.itu.jesl.st;

/**
 * Prototype implementation for edge-oriented top-down suffix tree representation.
 *
 * @author Jesper Larsson <jesl@avadeaux.net>
 */
public final class EOTD {
    private static class Edge {
        int pos;                    // pos of repr. suffix, id for internal
        int pid;                    // id of predecessor on path from root
        int length;                 // length of represented suffix
        int c;                      // first character of label
        Edge suf;                   // suffix link
        
        Edge(Edge next) {           // To enter into free list
            suf = next;
        }
    }

    private final int idBytes, charBytes;
    private final Edge[] intern;    // edges in id order, N elts 
    private final Edge[] hash;      // linear probing hash table, 2N elts
    private final TabHasher hasher;

    private final Edge top;         // root incoming

    private final Text text;

    private Edge free = null;       // linked list of free nodes; suf is next
    private int i = 0;              // index of next character to include
    private Edge a;                 // edge containing active point
    private int d = 0;              // length of active suffix
    private Edge s;                 // external just added, or top

    // At the end of an update, we have s==top unless the active point is the
    // root, in which case s is a just-added outgoing (external) edge of the
    // root, and its suflink is therefore to be set in the next update.

    // The only suflink that may ever change is that of an outgoing edge of the
    // root. This is set to top if the edge is split after the first character.

    // The code implements scheme III. The following variables counts hash
    // table accesses.

     public EOTD(Text text, int idBytes, int charBytes) {
        int n = text.length();
        this.text = text;
        this.idBytes = idBytes;
        this.charBytes = charBytes;
        a = s = top = new Edge(null);
        top.pos = -1;
        intern = new Edge[n];
        hash = new Edge[2*n];
        System.out.println("starting edge alloc");
        for (int i = 0; i < 2*n; i++) free = new Edge(free);
        System.out.println("finished edge alloc");
        hasher = new TabHasher(idBytes + charBytes);
    }

    private Edge allocate(int pos, int pid, int length, int c) {
        Edge t = free;
        free = t.suf;
        t.pos = pos;
        t.pid = pid;
        t.length = length;
        t.c = c;
        t.suf = null;
        return t;
    }

    /** Gets the number of characters currently included in the index. */
    public int indexedLength() { return i; }

    /** Expands the index to include n more characters on the right. */
    public void update(int n) {
        for (int j = 0; j < n; j++) update();
    }

    /** Ukkonen-style update, to include one more character. */
    public void update() {
        int c = charAt(i);              // next character to include
        int p = pos(a);
        int l = length(a);
        if (d < l) {                    // active point between characters in label
            int e = charAt(p+d);        // character after active suf
            if (e == c) {               // same as c, reached endpoint
                d++; i++; return;
            } // not there, need to split some edges
            Edge g = split(a, e);       // g will be sibling of new leaf
            while (true) {              // a has just been split into a, g
                Edge t = addExt(a, c);
                sufLink(s, t);
                s = t;
                d--; p++;
                if (d == 0) {
                    sufLink(a, top);    // not strictly necessary, since not used
                    a = top;
                } else {
                    a = suf(a);
                    while (true) {      // rescan
                        l = length(a);
                        if (d <= l) break;
                        a = down(a, charAt(p+l));
                    }
                    if (d < l) {        // not done splitting
                        Edge h = split(a, e);
                        sufLink(g, h);
                        g = h;
                        continue;
                    }
                }
                sufLink(g, down(a, e));
                break;
            }
        } // active point at end of a, move down
        while (true) {
            Edge b = down(a, c);
            if (b != null) {            // found, reached endpoint
                sufLink(s, b);
                s = top;
                a = b;
                d++; i++; return;
            }
            Edge t = addExt(a, c);
            sufLink(s, t);
            s = t;
            if (a == top) {
                i++; return;
            }
            d--; p++;
            if (d == 0) {
                a = top;
            } else {
                a = suf(a);
                while (true) {          // rescan
                    l = length(a);
                    if (d == l) break;
                    a = down(a, charAt(p+l));
                }
            }
        }
    }

    /** Gets the length of the longest match for a pattern, and stores position
     * in original sting as first element of pos array. */
    public int match(Text pat, long[] pos) {
        Edge t = top;
        int m = pat.length();
        int d = 0;
        int j = -1;
        outer: while (d < m && !isExt(t)) {
            t = down(t, pat.charAt(d));
            if (t == null) break;
            d++;
            j = pos(t);
            int l = min(length(t), i-j);
            while (d < l && d < m) {
                if (charAt(j+d) != pat.charAt(d)) break outer;
                d++;
            }
        }
        pos[0] = j;
        return d;
    }

    private static int min(int i, int j) { return i <= j ? i : j; }

    private int charAt(int i) { return text.charAt(i); }

    private static int pos(Edge t) { return t.pos; }

    private Edge down(Edge t, int c) {
        int h = hasher.hashCode(idBytes, t.pos, c) % hash.length;
        while (true) {
            Edge s = hash[h];
            if (s == null) return null;
            if (s.pid == t.pos && s.c == c) return s;
            if (++h == hash.length) h = 0;
        }
    }

    private void setDown(Edge t, int c, Edge s) {
        int h = hasher.hashCode(idBytes, t.pos, c) % hash.length;
        while (hash[h] != null) {
            if (++h == hash.length) h = 0;
        }
        hash[h] = s;
    }

    private Edge pred(Edge t) {
        if (t.pid == -1) return top;
        else             return intern[t.pid];
    }

    private static Edge suf(Edge t) { return t.suf; }
    
    /** Sets g's suffix link to point to h. */
    private static void sufLink(Edge g, Edge h) { g.suf = h; } 

    /** Returns length of suffix represented by transition. */
    private static int length(Edge t) { return t.length; }

    /** Checks if transition is external.*/
    private static boolean isExt(Edge t) { return t.length == Integer.MAX_VALUE; }

    /** Splits f into two transitions f (first part, suffix length d) and g
     * (second part). The first character of g is c. Does not set suffix
     * links. Returns g. */
    private Edge split(Edge f, int c) {
        // System.out.println("Split '" + string(f.pos, d) + "|" + string(f.pos+d, i-(f.pos+d)) + "'");
        Edge g = allocate(f.pos, i-d, f.length, c);
        f.pos = i-d;
        f.length = d;
        intern[i-d] = f;
        if (!isExt(g)) intern[g.pos] = g;
        setDown(f, c, g);
        return g;
    }

    /** Adds external transition, with first character c, below t. */
    private Edge addExt(Edge t, int c) {
        // System.out.println("Add ext '" + string(t.pos, t.length) + "|" + (char) c + "'");
        Edge s = allocate(i-d, t.pos, Integer.MAX_VALUE, c);
        setDown(t, c, s);
        return s;
    }

    // ------------------------------------------------------------------------
    // Debug utilities

    private String string(int pos, int length) {
        char[] a = new char[(int) length];
        for (int j = 0; j < length; j++) {
            a[j] = (char) charAt(pos + j);
        }
        return new String(a);
    }
}
