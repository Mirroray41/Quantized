package net.zapp.quantized.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import net.zapp.quantized.Quantized;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Quantized.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        /*tag(ModTags.Items.TRANSFORMABLE_ITEMS)
                .add(ModItems.BISMUTH.get())
                .add(ModItems.RAW_BISMUTH.get())
                .add(Items.COAL)
                .add(Items.STICK)
                .add(Items.COMPASS)*/
    }
}