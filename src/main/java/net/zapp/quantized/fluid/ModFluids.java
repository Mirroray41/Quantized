package net.zapp.quantized.fluid;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.registries.*;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.init.ModBlocks;


public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, Quantized.MOD_ID);

    public static final DeferredHolder<Fluid, FlowingFluid> QUANTUM_FLUX = FLUIDS.register("quantum_flux",
            () -> new BaseFlowingFluid.Source(ModFluids.QUANTUM_FLUX_PROPS));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_QUANTUM_FLUX = FLUIDS.register("flowing_quantum_flux",
            () -> new BaseFlowingFluid.Flowing(ModFluids.QUANTUM_FLUX_PROPS));
    private static final BaseFlowingFluid.Properties QUANTUM_FLUX_PROPS = new BaseFlowingFluid.Properties(
            ModFluidTypes.QUANTUM_FLUX_FLUID_TYPE, QUANTUM_FLUX, FLOWING_QUANTUM_FLUX
    ).explosionResistance(100.f).block(ModBlocks.QUANTUM_FLUX_BLOCK);

    public static void register(IEventBus modEventBus) {
        FLUIDS.register(modEventBus);
    }
}