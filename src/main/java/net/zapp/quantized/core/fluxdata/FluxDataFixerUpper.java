package net.zapp.quantized.core.fluxdata;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.core.configs.FluxDataConfig;
import net.zapp.quantized.core.utils.DataFluxPair;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Two-layer value store with a clear precedence:
 * <ol>
 *   <li><b>BASE</b> – authoritative anchors from config: explicit item values first, then tag
 *       values. Rebuilt by {@link #cacheAllItems()} (e.g. on {@code /reload}).</li>
 *   <li><b>DERIVED</b> – values computed from recipes by the solver (see
 *       {@link net.zapp.quantized.core.fluxdata.solver.ValueSolver}). Replaced wholesale by
 *       {@link #setDerived(Map)} whenever derivation runs.</li>
 * </ol>
 *
 * <p>Splitting the layers fixes the old bug where {@code /reload} cleared the single cache and wiped
 * every recipe-derived value until the next server restart: now a reload rebuilds BASE and triggers
 * a fresh derivation, while lookups still fall back to DERIVED for non-anchored items.
 */
public class FluxDataFixerUpper {
    /** Anchors: config item + tag values (eagerly populated by {@link #cacheAllItems()}). */
    private static final Map<Item, DataFluxPair> BASE = new IdentityHashMap<>();
    /** Items known to have no BASE value, so we don't re-walk their tags every lookup. */
    private static final Set<Item> BASE_MISS = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
    /** Recipe-derived values; swapped in atomically by {@link #setDerived}. */
    private static volatile Map<Item, DataFluxPair> DERIVED = new IdentityHashMap<>();
    /** Recipe id that produced each derived value (provenance for the debug command). */
    private static volatile Map<Item, ResourceLocation> DERIVED_SOURCES = new IdentityHashMap<>();

    public static void clearCache() {
        BASE.clear();
        BASE_MISS.clear();
    }

    /**
     * Resolved value for an item, honouring BASE-over-DERIVED precedence.
     * @return the value, or {@code null} if the item has no value anywhere.
     */
    public static DataFluxPair getDataFlux(Item item) {
        if (item == null) return null;

        DataFluxPair base = BASE.get(item);
        if (DataFluxPair.isValid(base)) return base;

        if (!BASE_MISS.contains(item)) {
            DataFluxPair computed = computeBase(item);
            if (DataFluxPair.isValid(computed)) {
                BASE.put(item, computed);
                return computed;
            }
            BASE_MISS.add(item);
        }

        DataFluxPair derived = DERIVED.get(item);
        if (DataFluxPair.isValid(derived)) return derived;
        return null;
    }

    public static DataFluxPair getDataFluxFromStack(ItemStack stack) {
        if (stack.isEmpty()) return null;
        return getDataFlux(stack.getItem());
    }

    /** Compute an item's BASE value from config: explicit item value, then cheapest matching tag. */
    private static DataFluxPair computeBase(Item item) {
        var id = BuiltInRegistries.ITEM.getKey(item);
        var fromItem = FluxDataConfig.itemMapView().get(id);
        if (DataFluxPair.isValid(fromItem)) return fromItem;

        Holder.Reference<Item> holder = item.builtInRegistryHolder();
        Map<TagKey<Item>, DataFluxPair> tagMap = FluxDataConfig.tagMapView();
        // Deterministic: when an item is in several priced tags, take the cheapest (by flux).
        DataFluxPair best = null;
        for (var tagKey : holder.tags().toList()) {
            DataFluxPair fromTag = tagMap.get(tagKey);
            if (!DataFluxPair.isValid(fromTag)) continue;
            if (best == null || fromTag.flux() < best.flux()
                    || (fromTag.flux() == best.flux() && fromTag.data() < best.data())) {
                best = fromTag;
            }
        }
        return best;
    }

    public static DataFluxPair getValuesFor(ResourceLocation id) {
        return FluxDataConfig.itemMapView().get(id);
    }

    /** Replace the derived layer with a freshly solved map and its provenance. */
    public static void setDerived(Map<Item, DataFluxPair> derived, Map<Item, ResourceLocation> sources) {
        DERIVED = new IdentityHashMap<>(derived);
        DERIVED_SOURCES = new IdentityHashMap<>(sources);
        Quantized.LOGGER.info("[Quantized] Derived value layer updated: {} item(s).", DERIVED.size());
    }

    /** True if this item's value came from the BASE (config) layer rather than a recipe. */
    public static boolean isAnchored(Item item) {
        return hasBaseValue(item);
    }

    /** The recipe id that derived this item's value, or {@code null} if anchored/unpriced. */
    public static ResourceLocation getDerivedSource(Item item) {
        return DERIVED_SOURCES.get(item);
    }

    /** True if this item has an authoritative BASE (config/tag) value. */
    public static boolean hasBaseValue(Item item) {
        if (item == null) return false;
        if (DataFluxPair.isValid(BASE.get(item))) return true;
        if (BASE_MISS.contains(item)) return false;
        return DataFluxPair.isValid(computeBase(item));
    }

    /** Eagerly (re)build the BASE layer from config for every registered item. */
    public static void cacheAllItems() {
        Quantized.LOGGER.info("[Quantized] Rebuilding BASE value layer");
        clearCache();
        for (Item item : BuiltInRegistries.ITEM) {
            DataFluxPair base = computeBase(item);
            if (DataFluxPair.isValid(base)) BASE.put(item, base);
            else BASE_MISS.add(item);
        }
        Quantized.LOGGER.info("[Quantized] BASE layer: {} item(s) anchored.", BASE.size());
    }
}
