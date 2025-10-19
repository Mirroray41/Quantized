package net.zapp.quantized.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.core.init.ModBlocks;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Quantized.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.STEEL_BLOCK.get())
                .add(ModBlocks.QUANTUM_DESTABILIZER.get())
                .add(ModBlocks.QUANTUM_ANALYZER.get())
                .add(ModBlocks.QUANTUM_FABRICATOR.get())
                .add(ModBlocks.FLUX_GENERATOR.get())
                .add(ModBlocks.STERLING_ENGINE.get());
        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.STEEL_BLOCK.get())
                .add(ModBlocks.QUANTUM_DESTABILIZER.get())
                .add(ModBlocks.QUANTUM_ANALYZER.get())
                .add(ModBlocks.QUANTUM_FABRICATOR.get())
                .add(ModBlocks.FLUX_GENERATOR.get())
                .add(ModBlocks.STERLING_ENGINE.get());
    }
}
