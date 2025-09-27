package net.zapp.quantized.blocks.quantum_destabilizer.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.zapp.quantized.init.ModRecipes;

public record QuantumDestabilizerRecipe(Ingredient inputItem, FluidStack output) implements Recipe<QuantumDestabilizerRecipeInput> {
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(inputItem);
        return list;
    }

    @Override
    public boolean matches(QuantumDestabilizerRecipeInput quantumDestabilizerRecipeInput, Level level) {
        if (level.isClientSide()) {
            return false;
        }

        return inputItem.test(quantumDestabilizerRecipeInput.getItem(0));
    }

    @Override
    public ItemStack assemble(QuantumDestabilizerRecipeInput quantumDestabilizerRecipeInput, HolderLookup.Provider provider) {
        return null;
    }

    
    public FluidStack assembleFluid(QuantumDestabilizerRecipeInput quantumDestabilizerRecipeInput, HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public RecipeSerializer<? extends Recipe<QuantumDestabilizerRecipeInput>> getSerializer() {
        return ModRecipes.QUANTUM_DESTABILIZER_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<QuantumDestabilizerRecipeInput>> getType() {
        return ModRecipes.QUANTUM_DESTABILIZER_TYPE.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(inputItem);
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public static class Serializer implements RecipeSerializer<QuantumDestabilizerRecipe> {
        public static final MapCodec<QuantumDestabilizerRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(QuantumDestabilizerRecipe::inputItem),
                FluidStack.CODEC.fieldOf("id").forGetter(QuantumDestabilizerRecipe::output)
        ).apply(inst, QuantumDestabilizerRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, QuantumDestabilizerRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, QuantumDestabilizerRecipe::inputItem,
                        FluidStack.STREAM_CODEC, QuantumDestabilizerRecipe::output,
                        QuantumDestabilizerRecipe::new);

        @Override
        public MapCodec<QuantumDestabilizerRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, QuantumDestabilizerRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
