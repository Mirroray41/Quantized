package net.zapp.quantized.init;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.api.utils.FluxDataPair;

public class ModComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Quantized.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FluxDataPair>> INT_PAIR = COMPONENTS.register("int_pair", () ->
            DataComponentType.<FluxDataPair>builder().persistent(FluxDataPair.CODEC).networkSynchronized(FluxDataPair.STREAM_CODEC).build());
}


