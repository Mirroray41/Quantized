package net.zapp.quantized.block.custom.machine_block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.zapp.quantized.block.ModRecipes;

public record MachineBlockRecipe(Ingredient inputItem, ItemStack output) implements Recipe<MachineBlockRecipeInput> {
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(inputItem);
        return list;
    }

    @Override
    public boolean matches(MachineBlockRecipeInput machineBlockRecipieInput, Level level) {
        if (level.isClientSide()) {
            return false;
        }

        return inputItem.test(machineBlockRecipieInput.getItem(0));
    }

    @Override
    public ItemStack assemble(MachineBlockRecipeInput machineBlockRecipieInput, HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public RecipeSerializer<? extends Recipe<MachineBlockRecipeInput>> getSerializer() {
        return ModRecipes.MACHINE_MACHINE_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<MachineBlockRecipeInput>> getType() {
        return ModRecipes.MACHINE_BLOCK_TYPE.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(inputItem);
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public static class Serializer implements RecipeSerializer<MachineBlockRecipe> {
        public static final MapCodec<MachineBlockRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(MachineBlockRecipe::inputItem),
                ItemStack.CODEC.fieldOf("result").forGetter(MachineBlockRecipe::output)
        ).apply(inst, MachineBlockRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, MachineBlockRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, MachineBlockRecipe::inputItem,
                        ItemStack.STREAM_CODEC, MachineBlockRecipe::output,
                        MachineBlockRecipe::new);

        @Override
        public MapCodec<MachineBlockRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MachineBlockRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
