package net.zapp.quantized.core.init;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.core.utils.fluid.CustomFluidType;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class ModFluidTypes {
    private ModFluidTypes() {}

    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, Quantized.MOD_ID);

    public static final Supplier<CustomFluidType> QUANTUM_FLUX_FLUID_TYPE = FLUID_TYPES.register("quantum_flux",
            () -> new CustomFluidType(FluidType.Properties.create().density(1200).viscosity(1200).canExtinguish(true),
                    ResourceLocation.withDefaultNamespace("block/water_still"), ResourceLocation.withDefaultNamespace("block/water_flow"),
                    null, 0xFFFF00FF, new Vector3f(1.f, 0.f, 1.f)));

    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
    }
}
