package net.zapp.quantized.core.event;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import net.zapp.quantized.core.init.ModBlockEntities;
import net.zapp.quantized.core.utils.energy.EnergyInputWrapper;
import net.zapp.quantized.core.utils.energy.EnergyOutputWrapper;
import net.zapp.quantized.core.utils.fluid.FluidInputWrapper;
import net.zapp.quantized.core.utils.fluid.FluidOutputWrapper;

public class TileCapabilities {
    protected static void destabilizerCaps(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.QUANTUM_DESTABILIZER_TILE.get(),
                (be, side) -> {
                    ItemStackHandler handler = be.getItemHandler();
                    if (side == Direction.UP) {
                        return new RangedWrapper(handler, 0, 1);
                    } else if (side == Direction.DOWN) {
                        return new RangedWrapper(handler, 1, 2);
                    } else {
                        return new RangedWrapper(handler, 0, 2);
                    }
                });

        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.QUANTUM_DESTABILIZER_TILE.get(),
                (be, side) -> new EnergyInputWrapper(be.getEnergyHandler()));

        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.QUANTUM_DESTABILIZER_TILE.get(),
                (be, side) -> new FluidOutputWrapper(be.getFluidHandler()));
    }

    protected static void analyzerCaps(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.QUANTUM_ANALYZER_TILE.get(),
                (be, side) -> {
                    ItemStackHandler handler = be.getItemHandler();
                    if (side == Direction.UP) {
                        return new RangedWrapper(handler, 0, 1);
                    } else if (side == Direction.DOWN) {
                        return new RangedWrapper(handler, 1, 2);
                    } else {
                        return new RangedWrapper(handler, 0, 2);
                    }
                });

        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.QUANTUM_ANALYZER_TILE.get(),
                (be, side) -> new EnergyInputWrapper(be.getEnergyHandler()));
    }

    protected static void stabilizerCaps(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.QUANTUM_STABILIZER_TILE.get(),
                (be, side) -> {
                    ItemStackHandler handler = be.getItemHandler();
                    return new RangedWrapper(handler, 0, 2);
                });

        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.QUANTUM_STABILIZER_TILE.get(),
                (be, side) -> new EnergyInputWrapper(be.getEnergyHandler()));

        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.QUANTUM_STABILIZER_TILE.get(),
                (be, side) -> new FluidInputWrapper(be.getFluidHandler()));
    }

    protected static void fluxGeneratorCaps(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.FLUX_GENERATOR_TILE.get(),
                (be, side) -> new EnergyOutputWrapper(be.getEnergyHandler()));

        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.FLUX_GENERATOR_TILE.get(),
                (be, side) -> new FluidInputWrapper(be.getFluidHandler()));
    }

    protected static void fabricatorCaps(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.QUANTUM_FABRICATOR_TILE.get(),
                (be, side) -> {
                    ItemStackHandler handler = be.getItemHandler();
                    if (side == Direction.UP) {
                        return new RangedWrapper(handler, 1, 7);
                    } else if (side == Direction.DOWN) {
                        return new RangedWrapper(handler, 1, 7);
                    } else {
                        return new RangedWrapper(handler, 0, 1);
                    }
                });

        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.QUANTUM_FABRICATOR_TILE.get(),
                (be, side) -> new EnergyInputWrapper(be.getEnergyHandler()));

        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.QUANTUM_FABRICATOR_TILE.get(),
                (be, side) -> new FluidInputWrapper(be.getFluidHandler()));
    }

    protected static void sterlingEngineCaps(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.STERLING_ENGINE_TILE.get(),
                (be, side) -> be.getItemHandler());

        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.STERLING_ENGINE_TILE.get(),
                (be, side) -> new EnergyOutputWrapper(be.getEnergyHandler()));

    }
}
