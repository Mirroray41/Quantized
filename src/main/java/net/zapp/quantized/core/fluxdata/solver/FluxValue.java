package net.zapp.quantized.core.fluxdata.solver;

import net.zapp.quantized.core.utils.DataFluxPair;

/**
 * A two-dimensional value used while solving: exact {@link Rational} {@code data} and {@code flux}.
 *
 * <p>Resolution state is modelled by presence/contents rather than an enum:
 * <ul>
 *   <li><b>UNRESOLVED</b> – no {@code FluxValue} exists for the item yet (absent from the value map).</li>
 *   <li><b>FREE</b> – a {@code FluxValue} whose {@code flux} is zero ({@link #isFree()}); the item is
 *       resolved but contributes nothing to recipe cost. Distinct from UNRESOLVED so a recipe whose
 *       only ingredient is free can still be priced.</li>
 *   <li><b>VALUED</b> – {@code flux > 0}.</li>
 * </ul>
 *
 * <p>The economic invariant: {@code flux} is the fungible resource (what a Destabilizer emits and a
 * Fabricator consumes), so the solver minimises on {@code flux} and carries {@code data} from the
 * same winning recipe. The two numbers therefore always originate from one coherent conversion.
 */
public record FluxValue(Rational data, Rational flux) {
    public static final FluxValue FREE = new FluxValue(Rational.ZERO, Rational.ZERO);

    public static FluxValue of(DataFluxPair pair) {
        return new FluxValue(Rational.of(pair.data()), Rational.of(pair.flux()));
    }

    /** Resolved but worth nothing: contributes zero cost and cannot itself be processed. */
    public boolean isFree() {
        return !flux.isPositive();
    }

    /** Scale by {@code amount/outnumber}. Amount may be negative (byproduct / container credit). */
    public FluxValue scale(long amount, long outnumber) {
        return new FluxValue(data.mulDiv(amount, outnumber), flux.mulDiv(amount, outnumber));
    }

    public FluxValue add(FluxValue o) {
        return new FluxValue(data.add(o.data), flux.add(o.flux));
    }

    /** Round to the final integer pair used in-game; conservative (rounds up) to avoid undervaluing. */
    public DataFluxPair toPair() {
        return new DataFluxPair(data.ceilToInt(), flux.ceilToInt());
    }
}
