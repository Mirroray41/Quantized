package net.zapp.quantized.datagen;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.core.init.ModBlocks;
import net.zapp.quantized.core.init.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ModModelProvider extends ModelProvider {
    public ModModelProvider(PackOutput output) {
        super(output, Quantized.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(ModItems.Q_BIT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.Q_BYTE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.STEEL_INGOT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.STEEL_NUGGET.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.STEEL_GEAR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.STEEL_PLATE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.STEEL_ROD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.MALLET.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.WIRE_CUTTERS.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.COPPER_WIRE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.INDUCTOR.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(ModItems.Q_BYTES_8.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.Q_BYTES_64.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.Q_BYTES_512.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(ModItems.Q_BYTES_4K.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.Q_BYTES_32K.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.Q_BYTES_256K.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(ModItems.Q_BYTES_2M.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.Q_BYTES_16M.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.Q_BYTES_128M.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(ModItems.Q_BYTES_1G.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(ModItems.DRIVE_8.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.DRIVE_64.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.DRIVE_512.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(ModItems.DRIVE_4K.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.DRIVE_32K.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.DRIVE_256K.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(ModItems.DRIVE_2M.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.DRIVE_16M.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.DRIVE_128M.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(ModItems.DRIVE_1G.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(ModItems.DRIVE_CASING.get(), ModelTemplates.FLAT_ITEM);

        /* BLOCKS */
        blockModels.createTrivialCube(ModBlocks.STEEL_BLOCK.get());
        blockModels.createTrivialCube(ModBlocks.QUANTUM_FLUX_BLOCK.get());
    }

    private static final List<Block> excludedBlocks = new ArrayList<>();
    private static final List<Item> excludedItems = new ArrayList<>();
    static {
        excludedBlocks.add(ModBlocks.MACHINE_BLOCK.get());
        excludedBlocks.add(ModBlocks.QUANTUM_DESTABILIZER.get());
        excludedBlocks.add(ModBlocks.QUANTUM_ANALYZER.get());
        excludedBlocks.add(ModBlocks.QUANTUM_STABILIZER.get());
        excludedBlocks.add(ModBlocks.FLUX_GENERATOR.get());

//        excludedItems.add(ModBlocks.MACHINE_BLOCK.get().asItem());
//        excludedItems.add(ModBlocks.QUANTUM_DESTABILIZER.get().asItem());
//        excludedItems.add(ModBlocks.QUANTUM_ANALYZER.get().asItem());
//        excludedItems.add(ModBlocks.QUANTUM_STABILIZER.get().asItem());
//        excludedItems.add(ModBlocks.FLUX_GENERATOR.get().asItem());
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().filter(b -> !excludedBlocks.contains(b.get()));
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return ModItems.ITEMS.getEntries().stream().filter(i -> !excludedItems.contains(i.get()));
    }
}
