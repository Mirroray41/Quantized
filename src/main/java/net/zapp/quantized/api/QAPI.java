package net.zapp.quantized.api;

import net.minecraft.resources.ResourceLocation;
import net.zapp.quantized.Quantized;

public final class QAPI {
    private QAPI() {}

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Quantized.MOD_ID, path);
    }
}