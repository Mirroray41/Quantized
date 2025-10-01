package net.zapp.quantized.core.datafixing;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zapp.quantized.core.configs.FluxDataConfig;
import net.zapp.quantized.core.utils.DataFluxPair;

import java.util.Set;

public class FluxDataFixerUpper {
    /**
     * Gets the DataFluxPair associated with this Item
     * @param item The item to get the data flux pair for.
     * @return The DataFluxPair, this pair can be 0 meaning there was no definition for this item,
     * either because it wasnt specified
     * or because the item's corresponding tag was used to give it a value.
     */
    public static DataFluxPair getDataFlux(Item item) {
        var id = BuiltInRegistries.ITEM.getKey(item);
        var d = FluxDataConfig.itemMapView().get(id);
        if (d != null) return d;
        var holder = item.builtInRegistryHolder();
        for (var e : FluxDataConfig.tagMapView().entrySet()) {
            if (holder.is(e.getKey()))
                return e.getValue();
        }
        return new DataFluxPair(0, 0);
    }

    /**
     * Gets the DataFluxPair associated with this itemStack and writes the value to the stack's dataComponent
     * This method is private since it can cause desync between server-client if used incorrectly.
     * @param stack The ItemStack to get and write the data for.
     * @return The DataFluxPair for this ItemStack, it could also be 0 (No Definition)
     * By this point Item Tags have been computed.
     */
    public static DataFluxPair getDataFluxFromStack(ItemStack stack) {
        if (stack.isEmpty()) return DataFluxPair.zero();
        return getDataFlux(stack.getItem());
    }

    public static DataFluxPair getValuesFor(ResourceLocation id) { return FluxDataConfig.itemMapView().get(id); }

    public static DataFluxPair getForTags(Set<TagKey<Item>> itemTags) {
        for (var k : FluxDataConfig.tagMapView().keySet()) if (itemTags.contains(k)) return FluxDataConfig.tagMapView().get(k);
        return null;
    }

}
