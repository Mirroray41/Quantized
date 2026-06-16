package net.zapp.quantized.core.fluxdata.solver;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.core.utils.DataFluxPair;

import java.util.*;

/**
 * Fixed-point, minimum-cost value solver (Bellman-Ford-style relaxation), modelled on ProjectE's
 * {@code SimpleGraphMapper.generateValues} but extended to two dimensions and exact arithmetic.
 *
 * <h2>Algorithm</h2>
 * <ol>
 *   <li>Seed {@code values} with the fixed/config anchors.</li>
 *   <li>Build a {@code usedIn} reverse index: item → conversions that reference it.</li>
 *   <li>Worklist relaxation: evaluate a conversion's cost; if it prices its output cheaper (by
 *       {@code flux}) than the current best, record it and re-enqueue every conversion that uses
 *       that output. Repeat until the worklist drains.</li>
 * </ol>
 *
 * <h2>Key rules</h2>
 * <ul>
 *   <li><b>Minimise on flux, carry data:</b> the stored value always comes from one coherent recipe.</li>
 *   <li><b>Per-slot cheapest candidate:</b> a choice/tag slot is charged its cheapest <i>resolved</i>
 *       candidate; re-relaxation guarantees convergence to the true minimum as candidates resolve.</li>
 *   <li><b>Positive slot needs a price; credits are optional:</b> a required input with no resolved
 *       candidate makes the conversion un-priceable for now; an unresolved credit is ignored.</li>
 *   <li><b>Anchors are authoritative:</b> config values are never lowered by recipes — instead a
 *       cheaper recipe is reported as a possible exploit ({@link Result#exploits}).</li>
 *   <li><b>Non-negative & terminating:</b> output cost is clamped to ≥ 0 (a recipe can't yield
 *       negative flux), and a generous step cap guards against pathological negative cycles.</li>
 * </ul>
 */
public final class ValueSolver {
    private ValueSolver() {}

    /** A recipe that could create a fixed-value item for less flux than its anchor value. */
    public record ExploitInfo(DataFluxPair anchor, DataFluxPair cheapest) {}

    public record Result(Map<Item, DataFluxPair> derived,
                         Map<Item, ResourceLocation> sources,
                         Map<Item, ExploitInfo> exploits) {}

    public static Result solve(List<Conversion> conversions, Map<Item, FluxValue> fixed) {
        Map<Item, FluxValue> values = new HashMap<>(fixed);
        Map<Item, ResourceLocation> winningSource = new HashMap<>();
        Set<Item> anchors = fixed.keySet();

        // Reverse index: each item -> conversions referencing it (any slot, positive or credit).
        Map<Item, List<Conversion>> usedIn = new HashMap<>();
        for (Conversion c : conversions) {
            for (Conversion.Slot slot : c.slots()) {
                for (Item cand : slot.candidates()) {
                    usedIn.computeIfAbsent(cand, k -> new ArrayList<>()).add(c);
                }
            }
        }

        ArrayDeque<Conversion> work = new ArrayDeque<>(conversions);
        Set<Conversion> queued = Collections.newSetFromMap(new IdentityHashMap<>());
        queued.addAll(conversions);

        // Track the cheapest recipe cost seen for anchor outputs, for exploit reporting.
        Map<Item, FluxValue> anchorRecipeCost = new HashMap<>();

        long steps = 0;
        long cap = Math.max(1_000_000L, (long) conversions.size() * 64L);

        while (!work.isEmpty()) {
            if (++steps > cap) {
                Quantized.LOGGER.warn("[Quantized:Solver] Relaxation step cap hit ({}); stopping early. "
                        + "A recipe cycle may be producing ever-cheaper values.", cap);
                break;
            }
            Conversion c = work.poll();
            queued.remove(c);

            // Exact-rational arithmetic throws ArithmeticException on long overflow. Per Rational's
            // contract that means THIS conversion is un-priceable for this pass — skip it, never let
            // it abort the whole solve (which would leave every recipe-derived item unpriced).
            try {
                FluxValue cost = evaluate(c, values);
                if (cost == null) continue;

                Item out = c.output();

                if (anchors.contains(out)) {
                    // Don't move anchors; just remember the cheapest recipe for exploit detection.
                    FluxValue prev = anchorRecipeCost.get(out);
                    if (prev == null || cost.flux().compareTo(prev.flux()) < 0) {
                        anchorRecipeCost.put(out, cost);
                    }
                    continue;
                }

                FluxValue current = values.get(out);
                if (current == null || cost.flux().compareTo(current.flux()) < 0
                        || (cost.flux().compareTo(current.flux()) == 0
                            && cost.data().compareTo(current.data()) < 0)) {
                    values.put(out, cost);
                    winningSource.put(out, c.source());
                    for (Conversion dep : usedIn.getOrDefault(out, List.of())) {
                        if (queued.add(dep)) work.add(dep);
                    }
                }
            } catch (ArithmeticException overflow) {
                Quantized.LOGGER.debug("[Quantized:Solver] Skipping un-priceable conversion {} (overflow): {}",
                        c.source(), overflow.toString());
            }
        }

        // Collect derived (non-anchor) results as final integer pairs.
        Map<Item, DataFluxPair> derived = new HashMap<>();
        Map<Item, ResourceLocation> sources = new HashMap<>();
        for (var e : values.entrySet()) {
            if (anchors.contains(e.getKey())) continue;
            FluxValue v = e.getValue();
            if (v.isFree()) continue; // free items carry no usable value in-game
            derived.put(e.getKey(), v.toPair());
            ResourceLocation src = winningSource.get(e.getKey());
            if (src != null) sources.put(e.getKey(), src);
        }

        // Build exploit report: anchor strictly more expensive than its cheapest recipe.
        Map<Item, ExploitInfo> exploits = new HashMap<>();
        for (var e : anchorRecipeCost.entrySet()) {
            FluxValue anchorVal = fixed.get(e.getKey());
            if (anchorVal == null) continue;
            try {
                if (e.getValue().flux().compareTo(anchorVal.flux()) < 0) {
                    exploits.put(e.getKey(), new ExploitInfo(anchorVal.toPair(), e.getValue().toPair()));
                }
            } catch (ArithmeticException ignored) {
                // Overflowing comparison just means we skip exploit reporting for this anchor.
            }
        }

        Quantized.LOGGER.info("[Quantized:Solver] Resolved {} derived item value(s) from {} conversion(s) in {} step(s).",
                derived.size(), conversions.size(), steps);
        return new Result(derived, sources, exploits);
    }

    /**
     * Cost of one conversion given current values, or {@code null} if a required input is unpriced.
     * Result flux is clamped to ≥ 0 (recipes cannot manufacture negative flux).
     */
    private static FluxValue evaluate(Conversion c, Map<Item, FluxValue> values) {
        FluxValue total = FluxValue.FREE;
        for (Conversion.Slot slot : c.slots()) {
            FluxValue best = cheapestResolved(slot.candidates(), values);
            if (best == null) {
                if (slot.isCredit()) continue; // unknown credit -> conservatively ignore
                return null;                   // required input not yet priced
            }
            total = total.add(best.scale(slot.amount(), c.outnumber()));
        }
        if (total.flux().isNegative()) {
            return FluxValue.FREE;
        }
        return total;
    }

    /** Cheapest (by flux, then data, then id) resolved candidate; {@code null} if none resolved. */
    private static FluxValue cheapestResolved(List<Item> candidates, Map<Item, FluxValue> values) {
        FluxValue best = null;
        Item bestItem = null;
        for (Item cand : candidates) {
            FluxValue v = values.get(cand);
            if (v == null) continue;
            if (best == null) {
                best = v;
                bestItem = cand;
                continue;
            }
            int cmp = v.flux().compareTo(best.flux());
            if (cmp == 0) cmp = v.data().compareTo(best.data());
            if (cmp == 0) {
                cmp = BuiltInRegistries.ITEM.getKey(cand).compareTo(BuiltInRegistries.ITEM.getKey(bestItem));
            }
            if (cmp < 0) {
                best = v;
                bestItem = cand;
            }
        }
        return best;
    }
}
