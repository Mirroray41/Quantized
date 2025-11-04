package net.zapp.quantized.core.utils.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.*;

public class RecipeUtils {
    public static Optional<ItemStack> tryGetRecipeOutput(Recipe<?> r, Level level) {
        try {
            ItemStack result = r.getResultItem(level.registryAccess());
            if (!result.isEmpty()) {
                return Optional.of(result);
            }
            return Optional.empty();
        } catch (Throwable t) {
            return Optional.empty();
        }
    }

    public static List<ItemStack> getRecipeInputs(Recipe<?> r, Level level) {
        List<ItemStack> stacks = new ArrayList<>();

        if (r instanceof CraftingRecipe craftingRecipe) {
            for (Ingredient ingredient : craftingRecipe.getIngredients()) {
                if (ingredient.isEmpty()) continue;
                ItemStack[] items = ingredient.getItems();
                if (items.length > 0 && !items[0].isEmpty()) {
                    ItemStack stack = items[0].copy();
                    if (stack.getCraftingRemainingItem().isEmpty()) {
                        stacks.add(stack);
                    }
                }
            }
            return stacks;
        }

        if (r instanceof AbstractCookingRecipe cookingRecipe) {
            Ingredient ingredient = cookingRecipe.getIngredients().getFirst();
            ItemStack[] items = ingredient.getItems();
            if (items.length > 0 && !items[0].isEmpty()) {
                stacks.add(items[0].copy());
            }
            return stacks;
        }

        if (r instanceof StonecutterRecipe stonecutterRecipe) {
            Ingredient ingredient = stonecutterRecipe.getIngredients().getFirst();
            ItemStack[] items = ingredient.getItems();
            if (items.length > 0 && !items[0].isEmpty()) {
                stacks.add(items[0].copy());
            }
            return stacks;
        }

        if (r instanceof SmithingRecipe smithingRecipe) {
            for (Ingredient ingredient : smithingRecipe.getIngredients()) {
                if (ingredient.isEmpty()) continue;
                ItemStack[] items = ingredient.getItems();
                if (items.length > 0 && !items[0].isEmpty()) {
                    ItemStack stack = items[0].copy();
                    if (stack.getCraftingRemainingItem().isEmpty()) {
                        stacks.add(stack);
                    }
                }
            }
            return stacks;
        }

        return List.of();
    }

    public static List<Item> expandTagToItems(TagKey<Item> tag, HolderLookup.Provider provider) {
        Set<Item> out = new LinkedHashSet<>();

        Optional<? extends HolderLookup.RegistryLookup<Item>> itemLookupOpt = provider.lookup(Registries.ITEM);
        if (itemLookupOpt.isEmpty()) return List.of();

        HolderLookup.RegistryLookup<Item> itemLookup = itemLookupOpt.get();
        itemLookup.get(tag).ifPresent(holders ->
                holders.forEach(holder -> out.add(holder.value()))
        );

        return out.stream().toList();
    }
}