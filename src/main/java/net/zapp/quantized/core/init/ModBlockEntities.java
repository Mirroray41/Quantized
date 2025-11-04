package net.zapp.quantized.core.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.content.blocks.flux_generator.FluxGeneratorTile;
import net.zapp.quantized.content.blocks.sterling_engine.SterlingEngineTile;
import net.zapp.quantized.content.blocks.quantum_analyzer.QuantumAnalyzerTile;
import net.zapp.quantized.content.blocks.quantum_destabilizer.QuantumDestabilizerTile;
import net.zapp.quantized.content.blocks.quantum_fabricator.QuantumFabricatorTile;
import net.zapp.quantized.content.blocks.quantum_stabilizer.QuantumStabilizerTile;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Quantized.MOD_ID);

    public static final Supplier<BlockEntityType<QuantumDestabilizerTile>> QUANTUM_DESTABILIZER_TILE =
            BLOCK_ENTITY_TYPES.register("quantum_destabilizer_tile",
                    () -> BlockEntityType.Builder
                            .of(QuantumDestabilizerTile::new, ModBlocks.QUANTUM_DESTABILIZER.get())
                            .build(null));
    public static final Supplier<BlockEntityType<QuantumAnalyzerTile>> QUANTUM_ANALYZER_TILE =
            BLOCK_ENTITY_TYPES.register("quantum_analyzer_tile",
                    () -> BlockEntityType.Builder
                            .of(QuantumAnalyzerTile::new, ModBlocks.QUANTUM_ANALYZER.get())
                            .build(null));
    public static final Supplier<BlockEntityType<QuantumFabricatorTile>> QUANTUM_FABRICATOR_TILE =
            BLOCK_ENTITY_TYPES.register("quantum_fabricator_tile",
                    () -> BlockEntityType.Builder
                            .of(QuantumFabricatorTile::new, ModBlocks.QUANTUM_FABRICATOR.get())
                            .build(null));
    public static final Supplier<BlockEntityType<QuantumStabilizerTile>> QUANTUM_STABILIZER_TILE =
            BLOCK_ENTITY_TYPES.register("quantum_stabilizer_tile",
                    () -> BlockEntityType.Builder
                            .of(QuantumStabilizerTile::new, ModBlocks.QUANTUM_STABILIZER.get())
                            .build(null));
    public static final Supplier<BlockEntityType<SterlingEngineTile>> STERLING_ENGINE_TILE =
            BLOCK_ENTITY_TYPES.register("sterling_engine_tile",
                    () -> BlockEntityType.Builder
                            .of(SterlingEngineTile::new, ModBlocks.STERLING_ENGINE.get())
                            .build(null));
    public static final Supplier<BlockEntityType<FluxGeneratorTile>> FLUX_GENERATOR_TILE =
            BLOCK_ENTITY_TYPES.register("flux_generator_tile",
                    () -> BlockEntityType.Builder
                            .of(FluxGeneratorTile::new, ModBlocks.FLUX_GENERATOR.get())
                            .build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }

}
