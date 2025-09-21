package net.zapp.quantized.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.block.ModBlocks;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Quantized.MOD_ID);

    public static final Supplier<CreativeModeTab> QUANTIZED_TAB = CREATIVE_MODE_TAB.register("quantized_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.QUANTUM_MATTER.get()))
                    .title(Component.translatable("creativetab.quantized.misc"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.QUANTUM_MATTER);
                        output.accept(ModBlocks.QUANTUM_MATTER_BLOCK);
                        output.accept(ModItems.STEEL_INGOT);
                        output.accept(ModBlocks.STEEL_BLOCK);
                        output.accept(ModItems.STEEL_NUGGET);
                        output.accept(ModItems.STEEL_GEAR);
                        output.accept(ModItems.STEEL_PLATE);
                        output.accept(ModItems.STEEL_ROD);
                        output.accept(ModItems.Q_BIT);
                        output.accept(ModItems.Q_BYTE);
                        output.accept(ModItems.MALLET);
                        output.accept(ModItems.WIRE_CUTTERS);
                        output.accept(ModItems.COPPER_WIRE);
                        output.accept(ModItems.INDUCTOR);
                    }).build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
