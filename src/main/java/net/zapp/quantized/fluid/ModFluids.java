package net.zapp.quantized.fluid;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.*;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.block.ModBlocks;
import net.zapp.quantized.item.ModItems;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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