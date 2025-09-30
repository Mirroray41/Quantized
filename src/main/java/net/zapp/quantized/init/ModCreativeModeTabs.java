package net.zapp.quantized.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zapp.quantized.Quantized;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Quantized.MOD_ID);

    public static final Supplier<CreativeModeTab> QUANTIZED_DATA = CREATIVE_MODE_TAB.register("quantized_data",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.DRIVE_CASING.get()))
                    .title(Component.translatable("creativetab.quantized.data"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.DRIVE_CASING);
                        output.accept(ModItems.Q_BYTES_8);
                        output.accept(ModItems.DRIVE_8);
                        output.accept(ModItems.Q_BYTES_64);
                        output.accept(ModItems.DRIVE_64);
                        output.accept(ModItems.Q_BYTES_512);
                        output.accept(ModItems.DRIVE_512);
                        output.accept(ModItems.Q_BYTES_4K);
                        output.accept(ModItems.DRIVE_4K);
                        output.accept(ModItems.Q_BYTES_32K);
                        output.accept(ModItems.DRIVE_32K);
                        output.accept(ModItems.Q_BYTES_256K);
                        output.accept(ModItems.DRIVE_256K);
                        output.accept(ModItems.Q_BYTES_2M);
                        output.accept(ModItems.DRIVE_2M);
                        output.accept(ModItems.Q_BYTES_16M);
                        output.accept(ModItems.DRIVE_16M);
                        output.accept(ModItems.Q_BYTES_128M);
                        output.accept(ModItems.DRIVE_128M);
                        output.accept(ModItems.Q_BYTES_1G);
                        output.accept(ModItems.DRIVE_1G);
                    }).build());


    public static final Supplier<CreativeModeTab> QUANTIZED_MISC = CREATIVE_MODE_TAB.register("quantized_misc",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.Q_BIT.get()))
                    .title(Component.translatable("creativetab.quantized.misc"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.Q_BIT);
                        output.accept(ModItems.Q_BYTE);
                        output.accept(ModItems.STEEL_INGOT);
                        output.accept(ModBlocks.STEEL_BLOCK);
                        output.accept(ModItems.STEEL_NUGGET);
                        output.accept(ModItems.STEEL_GEAR);
                        output.accept(ModItems.STEEL_PLATE);
                        output.accept(ModItems.STEEL_ROD);
                        output.accept(ModItems.MALLET);
                        output.accept(ModItems.WIRE_CUTTERS);
                        output.accept(ModItems.COPPER_WIRE);
                        output.accept(ModItems.INDUCTOR);
                        output.accept(ModBlocks.QUANTUM_DESTABILIZER);
                    }).build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
