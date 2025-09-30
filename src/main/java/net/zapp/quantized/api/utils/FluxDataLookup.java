package net.zapp.quantized.api.utils;

import net.minecraft.world.item.ItemStack;
import net.zapp.quantized.FluxDataConfig;

public class FluxDataLookup {
    public static FluxDataPair valuesFor(ItemStack stack) {
        return FluxDataConfig.getWithFallback(stack.getItem());
    }
}
