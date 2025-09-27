package net.zapp.quantized.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.blocks.machine_block.recipe.MachineBlockRecipe;
import net.zapp.quantized.blocks.quantum_destabilizer.recipe.QuantumDestabilizerRecipe;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Quantized.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, Quantized.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MachineBlockRecipe>> MACHINE_BLOCK_SERIALIZER =
            SERIALIZERS.register("machine_block", MachineBlockRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineBlockRecipe>> MACHINE_BLOCK_TYPE =
            TYPES.register("machine_block", () -> new RecipeType<MachineBlockRecipe>() {
                @Override
                public String toString() {
                    return "machine_block";
                }
            });

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<QuantumDestabilizerRecipe>> QUANTUM_DESTABILIZER_SERIALIZER =
            SERIALIZERS.register("quantum_destabilizer", QuantumDestabilizerRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<QuantumDestabilizerRecipe>> QUANTUM_DESTABILIZER_TYPE =
            TYPES.register("quantum_destabilizer", () -> new RecipeType<QuantumDestabilizerRecipe>() {
                @Override
                public String toString() {
                    return "quantum_destabilizer";
                }
            });


    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}
