package net.zapp.quantized.core.utils.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RecipeInfo {
    public static final Set<RecipeType<?>> COOKING_TYPES = Set.of(
            RecipeType.SMELTING,
            RecipeType.BLASTING,
            RecipeType.SMOKING,
            RecipeType.CAMPFIRE_COOKING
    );

    private final List<ItemStack> inputItems;
    private final ItemStack outputItem;
    private final RecipeType<?> recipeType;


    private RecipeInfo(List<ItemStack> inputItems, ItemStack outputItem, RecipeType<?> recipeType) {
        this.inputItems = inputItems;
        this.outputItem = outputItem;
        this.recipeType = recipeType;
    }

    public static RecipeInfo tryGetFromRecipe(Recipe<?> r, HolderLookup.Provider regs) {
        List<ItemStack> inputItems = RecipeUtils.getRecipeInputs(r, regs);
        if (inputItems.isEmpty()) return null;
        Optional<ItemStack> output = RecipeUtils.tryGetRecipeOutput(r, regs);
        return output.map(item -> new RecipeInfo(inputItems, item, r.getType())).orElse(null);
    }

    public List<ItemStack> slotOptions() {
        return inputItems;
    }

    public ItemStack output() {
        return outputItem;
    }

    public RecipeType<?> getRecipeType() {
        return recipeType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RecipeInfo for output: ").append(outputItem.getItemName().getString()).append('\n');
        for (int i = 0; i < inputItems.size(); i++) {
            sb.append("Slot: ").append(i).append(" : ").append(inputItems.get(i).getItemName().getString()).append('\n');
        }
        return sb.toString();
    }
}
