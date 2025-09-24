package net.zapp.quantized.api;

import net.minecraft.resources.ResourceLocation;

public final class QAPI {
    private QAPI() {}

    public static final String MOD_ID = "quantized";

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}