package net.zapp.quantized.init;


import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.blocks.machine_block.MachineBlockTile;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Quantized.MOD_ID);

    public static final Supplier<BlockEntityType<MachineBlockTile>> MACHINE_BLOCK_TILE =
            BLOCK_ENTITY_TYPES.register("machine_block_tile", () -> new BlockEntityType<>(
                    MachineBlockTile::new, ModBlocks.MACHINE_BLOCK.get()));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }

}
