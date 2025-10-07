package net.zapp.quantized.core.fluxdata;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.core.utils.DataFluxPair;
import net.zapp.quantized.core.utils.recipe.RecipeInfo;

import java.util.*;
import java.util.stream.Collectors;

public final class FluxDataRecipeComputer {
    private FluxDataRecipeComputer() {}

    public static void init() {
        NeoForge.EVENT_BUS.addListener(FluxDataRecipeComputer::onServerStarted);
    }

    public static void onServerStarted(ServerStartedEvent e) {
        MinecraftServer server = e.getServer();
        RecipeManager recipeManager = server.getRecipeManager();
        RegistryAccess.Frozen regs = server.registryAccess();
        computeAndCacheRecursive(recipeManager, regs);
    }

    private static int tierOf(RecipeInfo ri) {
        var t = ri.getRecipeType();
        if (t == RecipeType.CRAFTING) return 0;
        if (RecipeInfo.COOKING_TYPES.contains(t)) return 1;
        if (t == RecipeType.SMITHING) return 2;
        if (t == RecipeType.STONECUTTING) return 3;
        return 4;
    }

    private static ResourceLocation outputItemId(RecipeInfo ri) {
        return BuiltInRegistries.ITEM.getKey(ri.output().getItem());
    }

    private static boolean usesOutputItemAsInput(RecipeInfo ri) {
        var outId = outputItemId(ri);
        for (ItemStack in : ri.slotOptions()) {
            if (BuiltInRegistries.ITEM.getKey(in.getItem()).equals(outId)) return true;
        }
        return false;
    }

    public static void computeAndCacheRecursive(RecipeManager recipeManager, HolderLookup.Provider regs) {
        // 1) Build RecipeInfo once, index by output item id
        Map<ResourceLocation, List<RecipeInfo>> byOutput = new HashMap<>();
        for (var holder : recipeManager.getRecipes()) {
            var info = RecipeInfo.tryGetFromRecipe(holder.value(), regs);
            if (info == null) continue;
            byOutput.computeIfAbsent(outputItemId(info), k -> new ArrayList<>()).add(info);
        }
        // Sort each output's candidates by precedence tier (stable)
        for (var list : byOutput.values()) {
            list.sort(Comparator.comparingInt(FluxDataRecipeComputer::tierOf));
        }

        // 2) Resolve everything by DFS with memoization
        var memo = new HashMap<ResourceLocation, Boolean>();     // outId -> resolved?
        var inProgress = new HashSet<ResourceLocation>();        // cycle guard

        for (var outId : byOutput.keySet()) {
            resolveOutput(outId, byOutput, memo, inProgress);
        }
    }


    private static boolean resolveOutput(ResourceLocation outId, Map<ResourceLocation, List<RecipeInfo>> byOutput, Map<ResourceLocation, Boolean> memo, Set<ResourceLocation> inProgress) {
        // Cached already?
        if (FluxDataRecipeComputer.isOutputCached(outId)) {
            memo.put(outId, true);
            return true;
        }

        // Only short-circuit on a recorded SUCCESS
        Boolean m = memo.get(outId);
        if (Boolean.TRUE.equals(m)) return true;

        // Cycle guard
        if (!inProgress.add(outId)) {
            return false;
        }

        try {
            var candidates = byOutput.get(outId);
            if (candidates == null || candidates.isEmpty()) {
                return false;
            }

            // We have candidates sorted by precedence: craft -> smelt -> smith -> stonecut -> other
            int currentTier = -1;
            List<RecipeInfo> tierPile = new ArrayList<>();

            for (RecipeInfo ri : candidates) {
                int tier = tierOf(ri);
                if (tier != currentTier) {
                    // before switching to a lower tier: try all accumulated higher-tier options first
                    if (!tierPile.isEmpty()) {
                        if (tryTierPile(outId, tierPile, byOutput, memo, inProgress)) {
                            memo.put(outId, true);
                            return true;
                        }
                        tierPile.clear();
                    }
                    currentTier = tier;
                }
                tierPile.add(ri);
            }
            // flush the last tier
            if (!tierPile.isEmpty()) {
                if (tryTierPile(outId, tierPile, byOutput, memo, inProgress)) {
                    memo.put(outId, true);
                    return true;
                }
            }

            return false;

        } finally {
            inProgress.remove(outId);
        }
    }

    // Try to realize 'outId' using any recipe in this single tier.
    // For each recipe: recursively resolve its inputs, then cache-if-absent.
    private static boolean tryTierPile(ResourceLocation outId, List<RecipeInfo> tierPile, Map<ResourceLocation, List<RecipeInfo>> byOutput, Map<ResourceLocation, Boolean> memo, Set<ResourceLocation> inProgress) {
        // Minor anti-footgun: de-prioritize *directly* cyclic recipes inside the tier
        // so e.g., nugget->ingot isn't chosen before a non-cyclic craft path in the same tier.
        var ordered = new ArrayList<>(tierPile);
        ordered.sort(Comparator.comparing(FluxDataRecipeComputer::usesOutputItemAsInput)); // false first

        for (RecipeInfo ri : ordered) {
            // Skip if someone else already solved it
            if (FluxDataRecipeComputer.isOutputCached(outId)) return true;

            // 1) Make sure every input item is resolved (if it has recipes)
            boolean inputsOk = true;
            for (ItemStack input : ri.slotOptions()) {
                var inId = BuiltInRegistries.ITEM.getKey(input.getItem());
                if (!FluxDataRecipeComputer.isOutputCached(inId)) {
                    // Recursively resolve that input; if no recipes exist, isOutputCached(inId)
                    // should already reflect "primitive" values from your base config.
                    boolean ok = resolveOutput(inId, byOutput, memo, inProgress);
                    if (!ok && !FluxDataRecipeComputer.isOutputCached(inId)) {
                        inputsOk = false;
                        break;
                    }
                }
            }
            if (!inputsOk) continue;

            // 2) All inputs are available now — attempt to cache this output (no overwrite)
            if (!FluxDataRecipeComputer.isOutputCached(ri) && FluxDataRecipeComputer.allSlotsCached(ri)) {
                FluxDataRecipeComputer.cacheRecipe(ri);
            }
            if (FluxDataRecipeComputer.isOutputCached(outId)) {
                memo.put(outId, true);   // only remember wins
                return true;
            }
        }
        return false;
    }


    private static boolean isOutputCached(ResourceLocation rl) {
        Optional<Holder.Reference<Item>> item  = BuiltInRegistries.ITEM.get(rl);
        return item.filter(itemReference -> DataFluxPair.isValid(FluxDataFixerUpper.getDataFlux(itemReference.value()))).isPresent();
    }

    private static boolean isOutputCached(RecipeInfo recipeInfo) {
        return DataFluxPair.isValid(FluxDataFixerUpper.getDataFluxFromStack(recipeInfo.output()));
    }


    private static boolean allSlotsCached(RecipeInfo info) {
        for (var item : info.slotOptions()) {
            DataFluxPair flux = FluxDataFixerUpper.getDataFlux(item.getItem());
            if (!DataFluxPair.isValid(flux)) return false;
        }
        return true;
    }

    private static DataFluxPair costForRecipe(RecipeInfo info) {
        DataFluxPair sum = DataFluxPair.zero();
        Set<Item> uniqueIngredients = info.slotOptions().stream().map(ItemStack::getItem).collect(Collectors.toSet());
        for (ItemStack slotOption : info.slotOptions()) {
            DataFluxPair val = FluxDataFixerUpper.getDataFlux(slotOption.getItem()).copy();
            if (!uniqueIngredients.contains(slotOption.getItem())) {
                val.setData(0);
            } else {
                uniqueIngredients.remove(slotOption.getItem());
            }
            if (DataFluxPair.isValid(val)) sum.add(val);
        }
        int outCount = info.output().getCount();
        if (outCount > 1) {
            sum.div(outCount);
        }
        return sum;
    }

    private static void cacheRecipe(RecipeInfo recipeInfo) {
        FluxDataFixerUpper.cacheNewValue(recipeInfo.output().getItem(), costForRecipe(recipeInfo));
        Quantized.LOGGER.info("Successfully cached derived recipe for: {}, {}", recipeInfo.output().getItemName().getString(), FluxDataFixerUpper.getDataFluxFromStack(recipeInfo.output()));
    }
}
