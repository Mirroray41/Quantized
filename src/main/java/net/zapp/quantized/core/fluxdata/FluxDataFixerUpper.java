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

public class FluxDataFixerUpper {
    private static final Map<Item, DataFluxPair> CACHE = new IdentityHashMap<>();


    public static void clearCache() {
        CACHE.clear();
    }
    /**
     * Gets the DataFluxPair associated with this Item
     * @param item The item to get the data flux pair for.
     * @return The DataFluxPair, this pair can be 0 meaning there was no definition for this item,
     * either because it wasn't specified
     * or because the item's corresponding tag was used to give it a value.
     */
    public static DataFluxPair getDataFlux(Item item) {
        if (item == null) return null;
        DataFluxPair cached = CACHE.get(item);
        if (DataFluxPair.isValid(cached)) return cached;

        var id = BuiltInRegistries.ITEM.getKey(item);
        var fromItem = FluxDataConfig.itemMapView().get(id);
        if (fromItem != null) {
            CACHE.put(item, fromItem);
            return fromItem;
        }

        Holder.Reference<Item> holder = item.builtInRegistryHolder();
        Map<TagKey<Item>, DataFluxPair> tagMap = FluxDataConfig.tagMapView();
        for (var tagKey : holder.tags().toList()) {
            DataFluxPair fromTag = tagMap.get(tagKey);
            if (DataFluxPair.isValid(fromTag)) {
                CACHE.put(item, fromTag);
                return fromTag;
            }
        }

        return null;
    }

    /**
     * Gets the DataFluxPair associated with this itemStack.
     * @param stack The ItemStack to get the data for.
     * @return The DataFluxPair for this ItemStack, it could also be 0 (No Definition)
     * By this point Item Tags have been computed.
     */
    public static DataFluxPair getDataFluxFromStack(ItemStack stack) {
        if (stack.isEmpty()) return null;
        return getDataFlux(stack.getItem());
    }

    public static DataFluxPair getValuesFor(ResourceLocation id) { return FluxDataConfig.itemMapView().get(id); }

    public static DataFluxPair getForTags(Set<TagKey<Item>> itemTags) {
        for (var k : FluxDataConfig.tagMapView().keySet()) if (itemTags.contains(k)) return FluxDataConfig.tagMapView().get(k);
        return null;
    }

    public static void cacheIfComputed(Item item) {
        if (item == null) return;
        var cached = CACHE.get(item);
        if (DataFluxPair.isValid(cached)) return;

        var id = BuiltInRegistries.ITEM.getKey(item);
        var fromItem = FluxDataConfig.itemMapView().get(id);
        if (DataFluxPair.isValid(fromItem)) {
            CACHE.put(item, fromItem);
            return;
        }

        var holder = item.builtInRegistryHolder();
        var tagMap = FluxDataConfig.tagMapView();
        for (var tagKey : holder.tags().toList()) {
            var fromTag = tagMap.get(tagKey);
            if (DataFluxPair.isValid(fromTag)) {
                CACHE.put(item, fromTag);
                return;
            }
        }
    }

    public static void cacheNewValue(Item item, DataFluxPair pair) {
        if (item == null || !DataFluxPair.isValid(pair)) return;
        CACHE.putIfAbsent(item, pair);
    }


    public static void cacheAllItems() {
        Quantized.LOGGER.info("Recaching all Flux items");
        clearCache();
        for (Item item : BuiltInRegistries.ITEM) {
            cacheIfComputed(item);
        }
        Quantized.LOGGER.info("Successfully cached {} items.", CACHE.size());
    }
}
