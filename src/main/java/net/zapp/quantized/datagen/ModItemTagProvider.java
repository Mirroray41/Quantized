package net.zapp.quantized.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.core.init.ModTags;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Quantized.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModTags.Items.SLABS_STONE)
                .add(Items.STONE_SLAB)
                .add(Items.SMOOTH_STONE_SLAB)
                .add(Items.STONE_BRICK_SLAB)
                .add(Items.PURPUR_SLAB)
                .add(Items.COBBLESTONE_SLAB)
                .add(Items.NETHER_BRICK_SLAB)
                .add(Items.PRISMARINE_SLAB)
                .add(Items.PRISMARINE_BRICK_SLAB)
                .add(Items.DARK_PRISMARINE_SLAB)
                .add(Items.POLISHED_GRANITE_SLAB)
                .add(Items.MOSSY_STONE_BRICK_SLAB)
                .add(Items.POLISHED_DIORITE_SLAB)
                .add(Items.MOSSY_COBBLESTONE_SLAB)
                .add(Items.END_STONE_BRICK_SLAB)
                .add(Items.SMOOTH_QUARTZ_SLAB)
                .add(Items.GRANITE_SLAB)
                .add(Items.ANDESITE_SLAB)
                .add(Items.RED_NETHER_BRICK_SLAB)
                .add(Items.POLISHED_ANDESITE_SLAB)
                .add(Items.DIORITE_SLAB)
                .add(Items.BLACKSTONE_SLAB)
                .add(Items.POLISHED_BLACKSTONE_BRICK_SLAB)
                .add(Items.POLISHED_BLACKSTONE_SLAB)
                .add(Items.COBBLED_DEEPSLATE_SLAB)
                .add(Items.POLISHED_DEEPSLATE_SLAB)
                .add(Items.DEEPSLATE_TILE_SLAB)
                .add(Items.DEEPSLATE_BRICK_SLAB)
                .add(Items.TUFF_SLAB)
                .add(Items.POLISHED_TUFF_SLAB)
                .add(Items.TUFF_BRICK_SLAB);

        tag(ModTags.Items.STAIRS_STONE)
                .add(Items.STONE_STAIRS)
                .add(Items.STONE_BRICK_STAIRS)
                .add(Items.PURPUR_STAIRS)
                .add(Items.COBBLESTONE_STAIRS)
                .add(Items.NETHER_BRICK_STAIRS)
                .add(Items.PRISMARINE_STAIRS)
                .add(Items.PRISMARINE_BRICK_STAIRS)
                .add(Items.DARK_PRISMARINE_STAIRS)
                .add(Items.POLISHED_GRANITE_STAIRS)
                .add(Items.MOSSY_STONE_BRICK_STAIRS)
                .add(Items.POLISHED_DIORITE_STAIRS)
                .add(Items.MOSSY_COBBLESTONE_STAIRS)
                .add(Items.END_STONE_BRICK_STAIRS)
                .add(Items.SMOOTH_QUARTZ_STAIRS)
                .add(Items.GRANITE_STAIRS)
                .add(Items.ANDESITE_STAIRS)
                .add(Items.RED_NETHER_BRICK_STAIRS)
                .add(Items.POLISHED_ANDESITE_STAIRS)
                .add(Items.DIORITE_STAIRS)
                .add(Items.BLACKSTONE_STAIRS)
                .add(Items.POLISHED_BLACKSTONE_BRICK_STAIRS)
                .add(Items.POLISHED_BLACKSTONE_STAIRS)
                .add(Items.COBBLED_DEEPSLATE_STAIRS)
                .add(Items.POLISHED_DEEPSLATE_STAIRS)
                .add(Items.DEEPSLATE_TILE_STAIRS)
                .add(Items.DEEPSLATE_BRICK_STAIRS)
                .add(Items.TUFF_STAIRS)
                .add(Items.POLISHED_TUFF_STAIRS)
                .add(Items.TUFF_BRICK_STAIRS);
    }
}