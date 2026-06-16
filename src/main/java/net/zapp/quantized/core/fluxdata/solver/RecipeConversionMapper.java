package net.zapp.quantized.core.fluxdata.solver;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.zapp.quantized.Quantized;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns Minecraft recipes into solver-friendly {@link Conversion}s.
 *
 * <p>Improvements over the old first-item-only input extraction:
 * <ul>
 *   <li>Keeps <b>all</b> candidate items for a choice/tag ingredient (was: only {@code items[0]}),
 *       so the solver can charge the cheapest option.</li>
 *   <li>Counts crafting-remainder items as <b>credits</b> (negative slots) instead of dropping the
 *       whole ingredient — so e.g. a cake correctly costs the milk minus the returned buckets.</li>
 *   <li>Carries the real output count as {@code outnumber}.</li>
 * </ul>
 */
public final class RecipeConversionMapper {
    private RecipeConversionMapper() {}

    public static List<Conversion> build(RecipeManager recipeManager, Level level) {
        List<Conversion> out = new ArrayList<>();
        for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
            try {
                Conversion c = fromRecipe(holder.value(), holder.id(), level);
                if (c != null) out.add(c);
            } catch (Throwable t) {
                Quantized.LOGGER.debug("[Quantized:Solver] Skipped unmappable recipe {}: {}",
                        holder.id(), t.toString());
            }
        }
        // Deterministic order so solving is reproducible run-to-run.
        out.sort(Comparator
                .comparing((Conversion c) -> BuiltInRegistries.ITEM.getKey(c.output()).toString())
                .thenComparingInt(c -> c.slots().size()));
        return out;
    }

    private static Conversion fromRecipe(Recipe<?> r, ResourceLocation source, Level level) {
        ItemStack result = r.getResultItem(level.registryAccess());
        if (result == null || result.isEmpty()) return null;

        List<Ingredient> ingredients = ingredientsOf(r);
        if (ingredients.isEmpty()) return null;

        // Merge identical ingredient choice-sets into a single slot with a summed amount; keeps the
        // slot list compact (e.g. 8 cobblestone -> one slot amount 8) without changing the cost.
        Map<List<Item>, Integer> consumed = new LinkedHashMap<>();
        Map<List<Item>, Integer> credits = new LinkedHashMap<>();

        for (Ingredient ing : ingredients) {
            if (ing == null || ing.isEmpty()) continue;
            List<Item> candidates = candidatesOf(ing);
            if (candidates.isEmpty()) continue;

            consumed.merge(candidates, 1, Integer::sum);

            Item remainder = remainderOf(ing);
            if (remainder != null) {
                credits.merge(List.of(remainder), 1, Integer::sum);
            }
        }
        if (consumed.isEmpty()) return null;

        List<Conversion.Slot> slots = new ArrayList<>();
        consumed.forEach((cands, amount) -> slots.add(new Conversion.Slot(cands, amount)));
        credits.forEach((cands, amount) -> slots.add(new Conversion.Slot(cands, -amount)));

        return new Conversion(result.getItem(), Math.max(1, result.getCount()), slots, source);
    }

    /** All non-air items that satisfy an ingredient, distinct and sorted by id for determinism. */
    private static List<Item> candidatesOf(Ingredient ing) {
        List<Item> items = new ArrayList<>();
        for (ItemStack stack : ing.getItems()) {
            if (stack.isEmpty()) continue;
            Item item = stack.getItem();
            if (item == Items.AIR || items.contains(item)) continue;
            items.add(item);
        }
        items.sort(Comparator.comparing(i -> BuiltInRegistries.ITEM.getKey(i).toString()));
        return items;
    }

    /** Crafting-remainder of an ingredient's representative item (e.g. bucket from water bucket). */
    private static Item remainderOf(Ingredient ing) {
        ItemStack[] stacks = ing.getItems();
        if (stacks.length == 0) return null;
        ItemStack rem = stacks[0].getCraftingRemainingItem();
        if (rem.isEmpty() || rem.getItem() == Items.AIR) return null;
        if (rem.getItem() == stacks[0].getItem()) return null; // self-remainder, no net change
        return rem.getItem();
    }

    /** Ingredient list across the recipe types we price. Smithing keeps template+base+addition. */
    private static List<Ingredient> ingredientsOf(Recipe<?> r) {
        if (r instanceof CraftingRecipe cr) return cr.getIngredients();
        if (r instanceof AbstractCookingRecipe cook) return cook.getIngredients();
        if (r instanceof StonecutterRecipe sc) return sc.getIngredients();
        if (r instanceof SmithingRecipe sm) return sm.getIngredients();
        return List.of();
    }
}
