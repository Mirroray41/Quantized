package net.zapp.quantized.core.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.content.blocks.machine_block.MachineBlock;
import net.zapp.quantized.content.blocks.quantum_destabilizer.QuantumDestabilizer;

import java.util.function.Function;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Quantized.MOD_ID);

    public static final DeferredBlock<Block> STEEL_BLOCK = registerBlock("steel_block",
            (properties) -> new Block(properties
                    .strength(3f).requiresCorrectToolForDrops().sound(SoundType.METAL)));

    public static final DeferredBlock<Block> MACHINE_BLOCK = registerBlock("machine_block",
            (properties) -> new MachineBlock(properties
                    .strength(3f).requiresCorrectToolForDrops().sound(SoundType.METAL)));

    public static final DeferredBlock<Block> QUANTUM_DESTABILIZER = registerBlock("quantum_destabilizer",
            (properties) -> new QuantumDestabilizer(properties
                    .strength(3f).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion()
                    .lightLevel(state -> state.getValue(QuantumDestabilizer.ON) ? 15 : 0)));

    public static final DeferredBlock<LiquidBlock> QUANTUM_FLUX_BLOCK = registerBlock("quantum_flux",
            (properties) -> new LiquidBlock(ModFluids.FLOWING_QUANTUM_FLUX.get(), properties), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER));



    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> function) {
        DeferredBlock<T> toReturn = BLOCKS.registerBlock(name, function);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    public static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<Block.Properties, T> factory, Block.Properties properties) {
        ResourceLocation blockId = Quantized.id(name);
        return BLOCKS.register(name, () -> factory.apply(properties.setId(ResourceKey.create(Registries.BLOCK, blockId))));
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.registerItem(name, (properties) -> new BlockItem(block.get(), properties.useBlockDescriptionPrefix()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
