package net.zapp.quantized.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.core.init.ModBlocks;
import net.zapp.quantized.core.init.ModItems;

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

        eightCompactingRecipe(ModItems.Q_BIT.get(), ModItems.Q_BYTE, "has_q_bit");
        eightCompactingRecipe(ModItems.Q_BYTE.get(), ModItems.Q_BYTES_8, "has_q_byte");
        eightCompactingRecipe(ModItems.Q_BYTES_8.get(), ModItems.Q_BYTES_64, "has_8_q_bytes");
        eightCompactingRecipe(ModItems.Q_BYTES_64.get(), ModItems.Q_BYTES_512, "has_64_q_bytes");
        eightCompactingRecipe(ModItems.Q_BYTES_512.get(), ModItems.Q_BYTES_4K, "has_512_q_bytes");
        eightCompactingRecipe(ModItems.Q_BYTES_4K.get(), ModItems.Q_BYTES_32K, "has_4k_q_bytes");
        eightCompactingRecipe(ModItems.Q_BYTES_32K.get(), ModItems.Q_BYTES_256K, "has_32k_q_bytes");
        eightCompactingRecipe(ModItems.Q_BYTES_256K.get(), ModItems.Q_BYTES_2M, "has_256k_q_bytes");
        eightCompactingRecipe(ModItems.Q_BYTES_2M.get(), ModItems.Q_BYTES_16M, "has_2m_q_bytes");
        eightCompactingRecipe(ModItems.Q_BYTES_16M.get(), ModItems.Q_BYTES_128M, "has_16m_q_bytes");
        eightCompactingRecipe(ModItems.Q_BYTES_128M.get(), ModItems.Q_BYTES_1G, "has_128m_q_bytes");

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

        shaped(RecipeCategory.MISC, ModItems.DRIVE_CASING.get())
                .pattern("SNS")
                .pattern("SGS")
                .pattern("SSS")
                .define('S', ModItems.STEEL_PLATE.get())
                .define('G', Items.GLASS)
                .define('N', Items.GOLD_NUGGET)
                .unlockedBy("has_copper_wire", has(ModItems.COPPER_WIRE)).save(output);

        shaped(RecipeCategory.MISC, ModBlocks.QUANTUM_DESTABILIZER.get())
                .pattern("INI")
                .pattern("PBP")
                .pattern("GRC")
                .define('N', ModItems.STEEL_NUGGET.get())
                .define('I', ModItems.INDUCTOR.get())
                .define('P', ModItems.STEEL_PLATE.get())
                .define('B', Items.NETHER_STAR)
                .define('G', ModItems.STEEL_GEAR.get())
                .define('R', ModItems.STEEL_ROD.get())
                .define('C', ModItems.COPPER_WIRE.get())
                .unlockedBy("has_steel_ingot", has(ModItems.STEEL_INGOT)).save(output);

        shaped(RecipeCategory.MISC, ModBlocks.FLUX_GENERATOR.get())
                .pattern("CRC")
                .pattern("PNP")
                .pattern("IBG")
                .define('C', ModItems.COPPER_WIRE.get())
                .define('R', Items.REDSTONE)
                .define('P', ModItems.STEEL_PLATE.get())
                .define('N', Items.NETHERITE_INGOT)
                .define('I', ModItems.INDUCTOR.get())
                .define('B', ModBlocks.STEEL_BLOCK.get())
                .define('G', ModItems.STEEL_GEAR.get())
                .unlockedBy("has_steel_ingot", has(ModItems.STEEL_NUGGET.get())).save(output);

        shaped(RecipeCategory.MISC, ModBlocks.QUANTUM_STABILIZER.get())
                .pattern("CMC")
                .pattern("PAP")
                .pattern("RGR")
                .define('C', ModItems.COPPER_WIRE.get())
                .define('M', Items.COMPARATOR)
                .define('P', ModItems.STEEL_PLATE.get())
                .define('A', Items.AMETHYST_BLOCK)
                .define('R', ModItems.STEEL_ROD.get())
                .define('G', ModItems.STEEL_GEAR.get())
                .unlockedBy("has_steel_ingot", has(ModItems.STEEL_INGOT.get())).save(output);

        shaped(RecipeCategory.MISC, ModBlocks.QUANTUM_ANALYZER.get())
                .pattern("CIC")
                .pattern("PQP")
                .pattern("RGR")
                .define('C', ModItems.COPPER_WIRE.get())
                .define('I', ModItems.INDUCTOR.get())
                .define('P', ModItems.STEEL_PLATE.get())
                .define('Q', ModItems.Q_BYTES_512.get())
                .define('R', ModItems.STEEL_ROD.get())
                .define('G', ModItems.STEEL_GEAR.get())
                .unlockedBy("has_q_bytes", has(ModItems.Q_BYTE.get())).save(output);

        driveUnpackRecipe(ModItems.DRIVE_8.get(), ModItems.Q_BYTES_8.get());
        driveUnpackRecipe(ModItems.DRIVE_64.get(), ModItems.Q_BYTES_64.get());
        driveUnpackRecipe(ModItems.DRIVE_512.get(), ModItems.Q_BYTES_512.get());
        driveUnpackRecipe(ModItems.DRIVE_4K.get(), ModItems.Q_BYTES_4K.get());
        driveUnpackRecipe(ModItems.DRIVE_32K.get(), ModItems.Q_BYTES_32K.get());
        driveUnpackRecipe(ModItems.DRIVE_256K.get(), ModItems.Q_BYTES_256K.get());
        driveUnpackRecipe(ModItems.DRIVE_2M.get(), ModItems.Q_BYTES_2M.get());
        driveUnpackRecipe(ModItems.DRIVE_16M.get(), ModItems.Q_BYTES_16M.get());
        driveUnpackRecipe(ModItems.DRIVE_128M.get(), ModItems.Q_BYTES_128M.get());
        driveUnpackRecipe(ModItems.DRIVE_1G.get(), ModItems.Q_BYTES_1G.get());
        
        drivePackRecipe(ModItems.DRIVE_8.get(), ModItems.Q_BYTES_8.get());
        drivePackRecipe(ModItems.DRIVE_64.get(), ModItems.Q_BYTES_64.get());
        drivePackRecipe(ModItems.DRIVE_512.get(), ModItems.Q_BYTES_512.get());
        drivePackRecipe(ModItems.DRIVE_4K.get(), ModItems.Q_BYTES_4K.get());
        drivePackRecipe(ModItems.DRIVE_32K.get(), ModItems.Q_BYTES_32K.get());
        drivePackRecipe(ModItems.DRIVE_256K.get(), ModItems.Q_BYTES_256K.get());
        drivePackRecipe(ModItems.DRIVE_2M.get(), ModItems.Q_BYTES_2M.get());
        drivePackRecipe(ModItems.DRIVE_16M.get(), ModItems.Q_BYTES_16M.get());
        drivePackRecipe(ModItems.DRIVE_128M.get(), ModItems.Q_BYTES_128M.get());
        drivePackRecipe(ModItems.DRIVE_1G.get(), ModItems.Q_BYTES_1G.get());

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

    protected void eightCompactingRecipe(ItemLike item1, ItemLike item2, String unlock){
        shaped(RecipeCategory.MISC, item2)
                .pattern("BBB")
                .pattern("B B")
                .pattern("BBB")
                .define('B', item1)
                .unlockedBy(unlock, has(item1)).save(output, getItemName(item2) + "_from_" + getItemName(item1));

        shapeless(RecipeCategory.MISC, item1, 8)
                .requires(item2)
                .unlockedBy(unlock, has(item1)).save(output, getItemName(item1) + "_from_" + getItemName(item2));
    }

    protected void driveUnpackRecipe(ItemLike drive, ItemLike data) {
        shapeless(RecipeCategory.MISC, data, 1)
                .requires(drive)
                .unlockedBy("has_" + getItemName(drive), has(ModItems.DRIVE_8.get())).save(output, getItemName(data) + "_from_" + getItemName(drive));
    }

    protected void drivePackRecipe(ItemLike drive, ItemLike data) {
        shapeless(RecipeCategory.MISC, drive, 1)
                .requires(data)
                .requires(ModItems.DRIVE_CASING)
                .unlockedBy("has_" + getItemName(data), has(ModItems.DRIVE_8.get())).save(output, getItemName(drive) + "_from_" + getItemName(data));
    }
}
