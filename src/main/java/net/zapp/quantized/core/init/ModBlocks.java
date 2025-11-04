package net.zapp.quantized.core.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.content.blocks.sterling_engine.SterlingEngine;
import net.zapp.quantized.content.blocks.flux_generator.FluxGenerator;
import net.zapp.quantized.content.blocks.quantum_analyzer.QuantumAnalyzer;
import net.zapp.quantized.content.blocks.quantum_destabilizer.QuantumDestabilizer;
import net.zapp.quantized.content.blocks.quantum_fabricator.QuantumFabricator;
import net.zapp.quantized.content.blocks.quantum_stabilizer.QuantumStabilizer;

import java.util.function.Function;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Quantized.MOD_ID);

    public static final DeferredBlock<Block> STEEL_BLOCK = registerBlock("steel_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(3f).requiresCorrectToolForDrops().sound(SoundType.METAL)));
    public static final DeferredBlock<Block> QUANTUM_DESTABILIZER = registerBlock("quantum_destabilizer",
            () -> new QuantumDestabilizer(BlockBehaviour.Properties.of()
                    .strength(3f).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion()
                    .lightLevel(state -> state.getValue(QuantumDestabilizer.ON) ? 15 : 0)));
    public static final DeferredBlock<Block> QUANTUM_ANALYZER = registerBlock("quantum_analyzer",
            () -> new QuantumAnalyzer(BlockBehaviour.Properties.of()
                    .strength(3f).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion()
                    .lightLevel(state -> state.getValue(QuantumAnalyzer.ON) ? 15 : 0)));
    public static final DeferredBlock<Block> QUANTUM_FABRICATOR = registerBlock("quantum_fabricator",
            () -> new QuantumFabricator(BlockBehaviour.Properties.of()
                    .strength(3f).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion()
                    .lightLevel(state -> state.getValue(QuantumFabricator.ON) ? 15 : 0)));
    public static final DeferredBlock<Block> QUANTUM_STABILIZER = registerBlock("quantum_stabilizer",
            () -> new QuantumStabilizer(BlockBehaviour.Properties.of()
                    .strength(3f).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion()
                    .lightLevel(state -> state.getValue(QuantumStabilizer.ON) ? 15 : 0)));
    public static final DeferredBlock<Block> FLUX_GENERATOR = registerBlock("flux_generator",
            () -> new FluxGenerator(BlockBehaviour.Properties.of()
                    .strength(3f).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion()
                    .lightLevel(state -> state.getValue(FluxGenerator.ON) ? 15 : 0)));
    public static final DeferredBlock<Block> STERLING_ENGINE = registerBlock("sterling_engine",
            () -> new SterlingEngine(BlockBehaviour.Properties.of()
                    .strength(3f).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion()
                    .lightLevel(state -> state.getValue(SterlingEngine.LIT) ? 15 : 0)));

    public static final DeferredBlock<LiquidBlock> QUANTUM_FLUX_BLOCK = registerBlock("quantum_flux",
            () -> new LiquidBlock(ModFluids.FLOWING_QUANTUM_FLUX.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER)));



    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
