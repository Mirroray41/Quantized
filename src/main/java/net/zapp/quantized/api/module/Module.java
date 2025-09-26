package net.zapp.quantized.api.module;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface Module {
    default void save(ValueOutput out, HolderLookup.Provider registries) {}
    default void load(ValueInput in, HolderLookup.Provider registries) {}
}
