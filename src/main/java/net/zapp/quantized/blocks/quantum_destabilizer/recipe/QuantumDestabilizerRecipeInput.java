package net.zapp.quantized.blocks.quantum_destabilizer.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

public record QuantumDestabilizerRecipeInput(ItemStack input) implements RecipeInput {
    @Override
    public ItemStack getItem(int i) {
        return input;
    }

    @Override
    public int size() {
        return 1;
    }
}

