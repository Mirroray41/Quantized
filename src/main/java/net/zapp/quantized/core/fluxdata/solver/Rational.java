package net.zapp.quantized.core.fluxdata.solver;

/**
 * Exact non-negative-capable rational number backed by {@code long}s.
 *
 * <p>The value solver works in exact rationals so that dividing a recipe cost by its output count
 * and later multiplying it back into another recipe introduces <b>no rounding drift</b> (the bug
 * that {@link net.zapp.quantized.core.utils.DataFluxPair#div} caused with asymmetric ceil/floor).
 * Values are only rounded to {@code int}s once, at the very end, in {@link #ceilToInt()}.
 *
 * <p>Every fraction is kept normalised (gcd-reduced, positive denominator). Arithmetic uses
 * {@link Math#multiplyExact} / {@link Math#addExact}; on overflow an {@link ArithmeticException} is
 * thrown so the caller can treat the offending conversion as un-priceable for this pass instead of
 * silently producing garbage (mirrors ProjectE's BigFraction {@code ArithmeticException -> ZERO}).
 */
public final class Rational implements Comparable<Rational> {
    public static final Rational ZERO = new Rational(0, 1);

    private final long num;
    private final long den; // always > 0

    private Rational(long num, long den) {
        this.num = num;
        this.den = den;
    }

    public static Rational of(long whole) {
        return whole == 0 ? ZERO : new Rational(whole, 1);
    }

    public static Rational of(long num, long den) {
        if (den == 0) throw new ArithmeticException("Rational with zero denominator");
        if (num == 0) return ZERO;
        if (den < 0) {
            num = Math.negateExact(num);
            den = Math.negateExact(den);
        }
        long g = gcd(Math.abs(num), den);
        return new Rational(num / g, den / g);
    }

    public Rational add(Rational o) {
        if (this.num == 0) return o;
        if (o.num == 0) return this;
        // a/b + c/d = (a*d + c*b) / (b*d)
        long n = Math.addExact(Math.multiplyExact(num, o.den), Math.multiplyExact(o.num, den));
        long d = Math.multiplyExact(den, o.den);
        return of(n, d);
    }

    /** this * (n/d), used for amount/outnumber scaling. */
    public Rational mulDiv(long n, long d) {
        if (this.num == 0 || n == 0) return ZERO;
        return of(Math.multiplyExact(num, n), Math.multiplyExact(den, d));
    }

    public boolean isZero() {
        return num == 0;
    }

    public boolean isPositive() {
        return num > 0;
    }

    public boolean isNegative() {
        return num < 0;
    }

    /** Smallest int >= this value, clamped to >= 1 for any strictly-positive fraction. */
    public int ceilToInt() {
        if (num <= 0) return 0;
        long c = Math.ceilDiv(num, den);
        long clamped = Math.max(1L, Math.min(c, Integer.MAX_VALUE));
        return (int) clamped;
    }

    @Override
    public int compareTo(Rational o) {
        // a/b ? c/d  ->  a*d ? c*b  (denominators positive)
        return Long.compare(Math.multiplyExact(num, o.den), Math.multiplyExact(o.num, den));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Rational r && r.num == num && r.den == den;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(num) * 31 + Long.hashCode(den);
    }

    @Override
    public String toString() {
        return den == 1 ? Long.toString(num) : num + "/" + den;
    }

    private static long gcd(long a, long b) {
        while (b != 0) {
            long t = b;
            b = a % b;
            a = t;
        }
        return a == 0 ? 1 : a;
    }
}
