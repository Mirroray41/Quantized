package net.zapp.quantized.core.utils.module;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public interface Module {
    default void save(CompoundTag out, HolderLookup.Provider registries) {}
    default void load(CompoundTag in, HolderLookup.Provider registries) {}
}
