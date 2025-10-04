package net.zapp.quantized.content.blocks.quantum_destabilizer.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

public record QuantumDestabilizerRecipeInput(ItemStack input) implements RecipeInput {
    @Override
    public ItemStack getItem(int i) {
        return i == 0 ? input : ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 1;
    }
}

