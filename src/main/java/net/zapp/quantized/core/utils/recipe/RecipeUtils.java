package net.zapp.quantized.core.utils.recipe;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.display.*;

import java.util.*;

public class RecipeUtils {
    public static Optional<ItemStack> tryGetRecipeOutput(Recipe<?> r, HolderLookup.Provider regs) {
        try {
            List<RecipeDisplay> display = r.display();
            if (display.isEmpty()) return Optional.empty();
            RecipeDisplay first = display.getFirst();
            if (first == null) return Optional.empty();

            SlotDisplay result = first.result();
            ItemStack parsed = parseSlotDisplay(result, regs);
            if (!parsed.isEmpty()) {
                return Optional.of(parsed);
            } else {
                return Optional.empty();
            }
        } catch (Throwable t) {
            return Optional.empty();
        }
    }


    public static List<ItemStack> getRecipeInputs(Recipe<?> r, HolderLookup.Provider regs) {
        List<RecipeDisplay> displays = r.display();
        if (displays.isEmpty()) return List.of();
        RecipeDisplay first = displays.getFirst();
        if (first == null) return List.of();

        List<ItemStack> stacks = new ArrayList<>();
        if (first instanceof ShapedCraftingRecipeDisplay disp) {
            for (SlotDisplay ingredient : disp.ingredients()) {
                if (!isConsumed(ingredient, regs)) continue;
                ItemStack parsed = parseSlotDisplay(ingredient, regs);
                if (!parsed.isEmpty())
                    stacks.add(parsed);
            }
            return stacks;
        }
        if (first instanceof ShapelessCraftingRecipeDisplay disp) {
            for (SlotDisplay ingredient : disp.ingredients()) {
                if (!isConsumed(ingredient, regs)) continue;
                ItemStack parsed = parseSlotDisplay(ingredient, regs);
                if (!parsed.isEmpty())
                    stacks.add(parsed);
            }
            return stacks;
        }
        if (first instanceof FurnaceRecipeDisplay disp) {
            ItemStack parsed = parseSlotDisplay(disp.ingredient(), regs);
            if (!parsed.isEmpty())
                stacks.add(parsed);
            return stacks;
        }
        if (first instanceof StonecutterRecipeDisplay disp) {
            ItemStack parsed = parseSlotDisplay(disp.input(), regs);
            if (!parsed.isEmpty())
                stacks.add(parsed);
            return stacks;
        }
        if (first instanceof SmithingRecipeDisplay disp) {
            ItemStack baseParsed = parseSlotDisplay(disp.base(), regs);
            ItemStack additionParsed = parseSlotDisplay(disp.addition(), regs);
            ItemStack templateParsed = parseSlotDisplay(disp.template(), regs);
            if (!baseParsed.isEmpty())
                stacks.add(baseParsed);
            if (!additionParsed.isEmpty())
                stacks.add(additionParsed);
            if (!templateParsed.isEmpty())
                stacks.add(templateParsed);
            return stacks;
        }
        return List.of();
    }


    private static boolean isConsumed(SlotDisplay slot, HolderLookup.Provider regs) {
        if (slot instanceof SlotDisplay.WithRemainder) return false;

        if (slot instanceof SlotDisplay.ItemSlotDisplay(Holder<Item> holder)) {
            return new ItemStack(holder.value()).getCraftingRemainder().isEmpty();
        }
        if (slot instanceof SlotDisplay.ItemStackSlotDisplay(ItemStack stack)) {
            return stack.getCraftingRemainder().isEmpty();
        }

        ItemStack probe = parseSlotDisplay(slot, regs);
        if (!probe.isEmpty()) {
            return probe.getCraftingRemainder().isEmpty();
        }
        return true;
    }

    private static ItemStack parseSlotDisplay(SlotDisplay display, HolderLookup.Provider regs) {
        if (display instanceof SlotDisplay.Empty) return ItemStack.EMPTY;
        if (display instanceof SlotDisplay.ItemSlotDisplay(Holder<Item> item)) {
            return new ItemStack(item.value());
        }
        if (display instanceof SlotDisplay.ItemStackSlotDisplay(ItemStack item)) {
            return item;
        }
        if (display instanceof SlotDisplay.TagSlotDisplay(TagKey<Item> tag)) {
            return new ItemStack(expandTagToItems(tag, regs).getFirst());
        }
        if (display instanceof SlotDisplay.Composite(List<SlotDisplay> components)) {
            var shit = components.stream().map(slot -> parseSlotDisplay(slot, regs)).filter(it -> !it.isEmpty()).toList();
            if (shit.isEmpty()) return ItemStack.EMPTY;
            return shit.getFirst();
        }
        return ItemStack.EMPTY;
    }

    public static List<Item> expandTagToItems(TagKey<Item> tag, HolderLookup.Provider provider) {
        Set<Item> out = new LinkedHashSet<>();
        BuiltInRegistries.ITEM.get(tag);
        Optional<? extends HolderLookup.RegistryLookup<Item>> itemLookupOpt = provider.lookup(Registries.ITEM);
        if (itemLookupOpt.isEmpty()) return List.of();

        HolderLookup.RegistryLookup<Item> itemLookup = itemLookupOpt.get();
        itemLookup.get(tag)
                .map(HolderSet.Named::stream)
                .ifPresent(stream -> stream
                        .map(Holder::value)
                        .forEach(out::add));

        return out.stream().toList();
    }
}
