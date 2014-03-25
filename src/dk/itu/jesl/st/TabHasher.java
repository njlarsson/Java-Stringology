package dk.itu.jesl.st;

/**
 * An unsophisticated implementation of tabulation hashing. Please don't take
 * this too seriously.
 *
 * @author Jesper Larsson <jesl@avadeaux.net>
 */
final class TabHasher {
    private final static class XORShiftRandom {
        private long x;
        
        XORShiftRandom(long seed) {
            x = seed;
            randomLong();
        }
        
        final long randomLong() {
            x ^= (x << 21);
            x ^= (x >>> 35);
            x ^= (x << 4);
            return x;
        }
    }

    private final int keyLength;
    private final int[][] tab;

    TabHasher(int keyLength, long seed) {
        this.keyLength = keyLength;
        tab = new int[keyLength][];
        XORShiftRandom rand = new XORShiftRandom(seed);
        for (int i = 0; i < keyLength; i++) {
            tab[i] = new int[256];
            for (int j = 0; j < 256; j++) {
                tab[i][j] = (int) rand.randomLong();
            }
        }
    }
    
    TabHasher(int keyLength) { this(keyLength, System.nanoTime()); }

    /**
     * Gets nonnegative hash code for key stored at an offset in a byte array.
     */
    public int hashCode(byte[] a, int off) {
        int h = 0;
        for (int k = 0; k < keyLength; k++) {
            h ^= tab[k][a[k] & 255];
        }
        return h & 0x7fffffff;
    }

    /**
     * Gets a nonnegative hash code using a combination of two ints. The key
     * consists of the iBytes least significant bytes of i and the
     * (keyLength-iBytes) least significant bytes of j.
     */
    public int hashCode(int iBytes, int i, int j) {
        int h = 0, k = 0;
        while (k < iBytes) {
            h ^= tab[k][i & 255];
            i >>>= 8;
            k++;
        }
        while (k < keyLength) {
            h ^= tab[k][j & 255];
            j >>>= 8;
            k++;
        }
        return h & 0x7fffffff;
    }
}
