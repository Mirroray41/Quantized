package net.zapp.quantized.init;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.api.utils.DataFluxPair;

public class ModComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Quantized.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DataFluxPair>> DATA_FLUX_PAIR = COMPONENTS.register("int_pair", () ->
            DataComponentType.<DataFluxPair>builder().persistent(DataFluxPair.CODEC).networkSynchronized(DataFluxPair.STREAM_CODEC).build());

    public static void register(IEventBus modEventBus) {
        COMPONENTS.register(modEventBus);
    }

}


