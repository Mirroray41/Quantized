package net.zapp.quantized.content.blocks.quantum_destabilizer.recipe;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.zapp.quantized.core.init.ModRecipes;

import java.util.Optional;

public record QuantumDestabilizerRecipe(Ingredient inputItem, int craftingTicks, int powerUsage, FluidStack output) implements Recipe<QuantumDestabilizerRecipeInput> {
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(inputItem);
        return list;
    }

    @Override
    public boolean matches(QuantumDestabilizerRecipeInput input, Level level) {
        if (level.isClientSide()) {
            return false;
        }

        return inputItem.test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(QuantumDestabilizerRecipeInput input, HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
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
        private static final Codec<Ingredient> INGREDIENT_CODEC = Codec.either(ResourceLocation.CODEC, Ingredient.CODEC).xmap(
                either -> either.map(
                        rl -> {
                            Optional<Holder.Reference<Item>> itemOptional = BuiltInRegistries.ITEM.get(rl);
                            if (!itemOptional.isPresent()) {
                                throw new IllegalArgumentException("Unknown item id: " + rl);
                            }
                            return Ingredient.of(itemOptional.get().value());
                        },
                        ing -> ing
                ),
                Either::right
        );

        public static final MapCodec<QuantumDestabilizerRecipe> CODEC =
                RecordCodecBuilder.mapCodec(inst -> inst.group(
                        INGREDIENT_CODEC.fieldOf("ingredient")
                                .forGetter(QuantumDestabilizerRecipe::inputItem),
                        Codec.INT.fieldOf("crafting_time")
                                .forGetter(QuantumDestabilizerRecipe::craftingTicks),
                        Codec.INT.fieldOf("power_usage")
                                        .forGetter(QuantumDestabilizerRecipe::powerUsage),
                        FluidStack.CODEC.fieldOf("result")
                                .forGetter(QuantumDestabilizerRecipe::output)
                ).apply(inst, QuantumDestabilizerRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, QuantumDestabilizerRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, QuantumDestabilizerRecipe::inputItem,
                        ByteBufCodecs.VAR_INT, QuantumDestabilizerRecipe::craftingTicks,
                        ByteBufCodecs.VAR_INT, QuantumDestabilizerRecipe::powerUsage,
                        FluidStack.STREAM_CODEC, QuantumDestabilizerRecipe::output,
                        QuantumDestabilizerRecipe::new
                );

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
