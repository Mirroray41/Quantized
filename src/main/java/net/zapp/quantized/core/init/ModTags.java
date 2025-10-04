package net.zapp.quantized.core.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.zapp.quantized.Quantized;

public class ModTags {
    public static class Blocks {
        //public static final TagKey<Block> NEEDS_BISMUTH_TOOL = createTag("needs_bismuth_tool");
        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(Quantized.MOD_ID, name));
        }
    }

    public static class Items {
        public static final TagKey<Item> STAIRS_STONE = createTag("stairs/stone");
        public static final TagKey<Item> SLABS_STONE = createTag("slabs/stone");

        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(Quantized.MOD_ID, name));
        }
    }
}
