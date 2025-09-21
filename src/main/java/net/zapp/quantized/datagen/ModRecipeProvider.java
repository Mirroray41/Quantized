package net.zapp.quantized.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.block.ModBlocks;
import net.zapp.quantized.item.ModItems;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(HolderLookup.Provider provider, RecipeOutput recipeOutput) {
        super(provider, recipeOutput);
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> provider) {
            super(packOutput, provider);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput recipeOutput) {
            return new ModRecipeProvider(provider, recipeOutput);
        }

        @Override
        public String getName() {
            return "My Recipes";
        }
    }


    @Override
    protected void buildRecipes() {

        shaped(RecipeCategory.MISC, ModBlocks.QUANTUM_MATTER_BLOCK.get())
                .pattern("BBB")
                .pattern("BBB")
                .pattern("BBB")
                .define('B', ModItems.QUANTUM_MATTER.get())
                .unlockedBy("has_quantum_matter", has(ModItems.QUANTUM_MATTER)).save(output);

        shapeless(RecipeCategory.MISC, ModItems.QUANTUM_MATTER.get(), 9)
                .requires(ModBlocks.QUANTUM_MATTER_BLOCK)
                .unlockedBy("has_quantum_matter_block", has(ModBlocks.QUANTUM_MATTER_BLOCK)).save(output);

        shaped(RecipeCategory.MISC, ModItems.Q_BYTE.get())
                .pattern("BBB")
                .pattern("B B")
                .pattern("BBB")
                .define('B', ModItems.Q_BIT.get())
                .unlockedBy("has_q_bit", has(ModItems.Q_BIT)).save(output);

        shapeless(RecipeCategory.MISC, ModItems.Q_BIT.get(), 8)
                .requires(ModItems.Q_BYTE)
                .unlockedBy("has_q_byte", has(ModItems.Q_BYTE)).save(output);

        shaped(RecipeCategory.MISC, ModBlocks.STEEL_BLOCK.get())
                .pattern("BBB")
                .pattern("BBB")
                .pattern("BBB")
                .define('B', ModItems.STEEL_INGOT.get())
                .unlockedBy("has_steel_ingot", has(ModItems.STEEL_INGOT)).save(output);

        shapeless(RecipeCategory.MISC, ModItems.STEEL_INGOT.get(), 9)
                .requires(ModBlocks.STEEL_BLOCK)
                .unlockedBy("has_steel_block", has(ModBlocks.STEEL_BLOCK)).save(output);

        shaped(RecipeCategory.MISC, ModItems.STEEL_INGOT.get())
                .pattern("BBB")
                .pattern("BBB")
                .pattern("BBB")
                .define('B', ModItems.STEEL_NUGGET.get())
                .unlockedBy("has_steel_nugget", has(ModItems.STEEL_NUGGET)).save(output, Quantized.MOD_ID + ":" + getItemName(ModItems.STEEL_INGOT) + "_from_" + getItemName(ModItems.STEEL_NUGGET));

        shapeless(RecipeCategory.MISC, ModItems.STEEL_NUGGET.get(), 9)
                .requires(ModItems.STEEL_INGOT)
                .unlockedBy("has_steel_ingot", has(ModItems.STEEL_INGOT)).save(output);

        shaped(RecipeCategory.MISC, ModItems.STEEL_GEAR.get())
                .pattern(" B ")
                .pattern("BIB")
                .pattern(" B ")
                .define('B', ModItems.STEEL_INGOT.get())
                .define('I', ModItems.STEEL_NUGGET.get())
                .unlockedBy("has_steel_nugget", has(ModItems.STEEL_NUGGET)).save(output);

        shaped(RecipeCategory.MISC, ModItems.MALLET.get())
                .pattern(" I ")
                .pattern(" SI")
                .pattern("S  ")
                .define('I', ModItems.STEEL_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy("has_steel_ingot", has(ModItems.STEEL_INGOT)).save(output);

        shapeless(RecipeCategory.MISC, ModItems.STEEL_PLATE.get(), 2)
                .requires(ModItems.STEEL_INGOT)
                .requires(ModItems.MALLET)
                .unlockedBy("has_steel_ingot", has(ModItems.STEEL_INGOT)).save(output);

        shaped(RecipeCategory.MISC, ModItems.STEEL_ROD.get())
                .pattern("B")
                .pattern("B")
                .define('B', ModItems.STEEL_PLATE.get())
                .unlockedBy("has_steel_plate", has(ModItems.STEEL_PLATE)).save(output);

        shaped(RecipeCategory.MISC, ModItems.WIRE_CUTTERS.get())
                .pattern(" S ")
                .pattern("TNS")
                .pattern(" T ")
                .define('S', ModItems.STEEL_PLATE.get())
                .define('N', ModItems.STEEL_NUGGET.get())
                .define('T', Items.BLUE_TERRACOTTA)
                .unlockedBy("has_steel_plate", has(ModItems.STEEL_PLATE)).save(output);

        shapeless(RecipeCategory.MISC, ModItems.COPPER_WIRE.get(), 3)
                .requires(Items.COPPER_INGOT)
                .requires(ModItems.WIRE_CUTTERS)
                .unlockedBy("has_wire_cutters", has(ModItems.WIRE_CUTTERS)).save(output);

        shaped(RecipeCategory.MISC, ModItems.INDUCTOR.get())
                .pattern("WRW")
                .pattern("WRW")
                .pattern("WRW")
                .define('R', ModItems.STEEL_ROD.get())
                .define('W', ModItems.COPPER_WIRE.get())
                .unlockedBy("has_copper_wire", has(ModItems.COPPER_WIRE)).save(output);



        oreBlasting(output, List.of(Items.IRON_INGOT), RecipeCategory.MISC, ModItems.STEEL_INGOT.get(), 0.25f, 300, "steel");
        // Throws error
        // trimSmithing(ModItems.KAUPEN_SMITHING_TEMPLATE.get(), ResourceKey.create(Registries.TRIM_PATTERN, ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "kaupen")),
        //         ResourceKey.create(Registries.RECIPE, ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "kaupen")));
    }

    protected void oreSmelting(RecipeOutput recipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult,
                                      float pExperience, int pCookingTIme, String pGroup) {
        oreCooking(recipeOutput, RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new, pIngredients, pCategory, pResult,
                pExperience, pCookingTIme, pGroup, "_from_smelting");
    }

    protected void oreBlasting(RecipeOutput recipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult,
                                      float pExperience, int pCookingTime, String pGroup) {
        oreCooking(recipeOutput, RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new, pIngredients, pCategory, pResult,
                pExperience, pCookingTime, pGroup, "_from_blasting");
    }

    protected <T extends AbstractCookingRecipe> void oreCooking(RecipeOutput recipeOutput, RecipeSerializer<T> pCookingSerializer, AbstractCookingRecipe.Factory<T> factory,
                                                                       List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup, String pRecipeName) {
        for(ItemLike itemlike : pIngredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult, pExperience, pCookingTime, pCookingSerializer, factory).group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(recipeOutput, Quantized.MOD_ID + ":" + getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
        }
    }
}
