package net.zapp.quantized.core.fluxdata;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.zapp.quantized.core.configs.FluxDataConfig;
import net.zapp.quantized.core.utils.DataFluxPair;

import java.util.*;

public class FluxCraftingComputer {

    public static void init() {
        NeoForge.EVENT_BUS.addListener(FluxCraftingComputer::onServerStarted);
    }

    public static void onServerStarted(ServerStartedEvent e) {
        var server = e.getServer();
        var mgr = server.getRecipeManager();
        var regs = server.registryAccess();
        computeAndCache(mgr, regs);
    }

    public static void computeAndCache(RecipeManager recipeManager, HolderLookup.Provider regs) {
        System.out.println("DOING SHIT");
        Object2ObjectOpenHashMap<Item, DataFluxPair> known = seedFromConfigs();
        Object2ObjectOpenHashMap<Item, ObjectArrayList<CraftingRecipe>> index = buildReverseIndex(recipeManager, regs);
        System.out.println("KNOWN PAIRS");
        known.forEach((i, p) -> System.out.println(i.getName() + ": " + p));
        System.out.println("THINGS USED BY THOSE PAIRS");
        index.forEach((i, p) -> System.out.println(i.getName() + ": " + p));
        computeCrafting(known, index, regs);

        FluxDataFixerUpper.clearCache();
        known.forEach((item, df) -> {
            if (DataFluxPair.isValid(df)) FluxDataFixerUpper.cache(item, df);
        });
    }

    private static void computeCrafting(Object2ObjectOpenHashMap<Item, DataFluxPair> known, Object2ObjectOpenHashMap<Item, ObjectArrayList<CraftingRecipe>> usedBy, HolderLookup.Provider regs) {
        ArrayDeque<Item> queue = new ArrayDeque<>(known.keySet());
        while (!queue.isEmpty()) {
            Item it = queue.removeFirst();
            ObjectArrayList<CraftingRecipe> recipes = usedBy.get(it);
            if (recipes == null || recipes.isEmpty()) return;

            for (var c : recipes) {
                System.out.println("computing recipe " + c);
                List<Ingredient> inputs = c.placementInfo().ingredients();
                boolean allResolvable = true;
                DataFluxPair sum = DataFluxPair.ZERO;

                for (var ing : inputs) {
                    if (ing.isEmpty()) continue;

                    DataFluxPair choice = null;

                    Set<Item> items = getIngredientItems(ing);
                    for (var item : items) {
                        DataFluxPair df = known.get(item);
                        if (DataFluxPair.isValid(df)) {
                            choice = df;
                            break;
                        }
                    }
                    if (!DataFluxPair.isValid(choice))  { allResolvable = false; break; }

                    sum.add(choice);

                }

                if (!allResolvable) continue;

                var outItem = tryGetOutputItem(c);
                if (outItem.isEmpty()) continue;

                if (!known.containsKey(outItem.get())) {
                    known.put(outItem.get(), sum);
                    queue.addLast(outItem.get());
                }
            }

        }
    }

    private static Object2ObjectOpenHashMap<Item, DataFluxPair> seedFromConfigs() {
        Object2ObjectOpenHashMap<Item, DataFluxPair> known = new Object2ObjectOpenHashMap<>();
        for (var e : FluxDataConfig.itemMapView().entrySet()) {
            ResourceLocation rl = e.getKey();
            Optional<Item> opt = BuiltInRegistries.ITEM.getOptional(rl);
            opt.ifPresent(item -> known.put(item, e.getValue()));
        }

        for (var e : FluxDataConfig.tagMapView().entrySet()) {
            TagKey<Item> tagKey = e.getKey();
            DataFluxPair df = e.getValue();
            Iterable<Holder<Item>> tagIt = BuiltInRegistries.ITEM.getTagOrEmpty(tagKey);
            tagIt.forEach(holder -> known.putIfAbsent(holder.value(), df));
        }

        return known;
    }

    private static Object2ObjectOpenHashMap<Item, ObjectArrayList<CraftingRecipe>> buildReverseIndex(RecipeManager manager, HolderLookup.Provider regs) {
        Object2ObjectOpenHashMap<Item, ObjectArrayList<CraftingRecipe>> index = new Object2ObjectOpenHashMap<>();

        for (var recipe : manager.getRecipes()) {
            if (recipe.value().isSpecial()) continue;
            if (!(recipe.value() instanceof CraftingRecipe c)) continue;
            System.out.println(((CraftingRecipe) recipe.value()).getType());
            List<Ingredient> inputs = c.placementInfo().ingredients();
            for (var ing : inputs) {
                if (ing.isEmpty()) continue;
                for (Item item : getIngredientItems(ing)) {
                    index.computeIfAbsent(item, k ->  new ObjectArrayList<>()).add(c);
                }
            }
        }
        return index;
    }

    public static Optional<Item> tryGetOutputItem(Recipe<?> r) {
        try {
            var display = r.display();
            if (display.isEmpty()) return Optional.empty();

            var first = display.getFirst();
            if (first == null) return Optional.empty();

            var result = first.result();                 // SlotDisplay
            if (result instanceof SlotDisplay.ItemSlotDisplay(var holder)) {
                var it = holder.value();
                return Optional.of(it);
            } else if (result instanceof SlotDisplay.ItemStackSlotDisplay(var stack)) {
                return stack.isEmpty() ? Optional.empty()
                        : Optional.of(stack.getItem());
            }

            return Optional.empty();

        } catch (Throwable t) {
            return Optional.empty();
        }
    }

    public static Set<Item> getIngredientItems(Ingredient ingredient) {
        if (ingredient.isEmpty()) return Set.of();
        Set<Item> itemSet = new HashSet<>();
        if (ingredient.isCustom()) {
            ingredient.getCustomIngredient().items().forEach(i -> itemSet.add(i.value()));

        } else {
            ingredient.getValues().forEach(i -> itemSet.add(i.value()));
        }
        return itemSet;
    }

}
