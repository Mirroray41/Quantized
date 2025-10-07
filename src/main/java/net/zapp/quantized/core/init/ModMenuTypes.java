package net.zapp.quantized.core.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.content.blocks.machine_block.MachineBlockMenu;
import net.zapp.quantized.content.blocks.quantum_analyzer.QuantumAnalyzerMenu;
import net.zapp.quantized.content.blocks.quantum_destabilizer.QuantumDestabilizerMenu;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Quantized.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<MachineBlockMenu>> MACHINE_BLOCK_MENU =
            registerMenuType("machine_block_menu", MachineBlockMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<QuantumDestabilizerMenu>> QUANTUM_DESTABILIZER_MENU =
            registerMenuType("quantum_destabilizer_menu", QuantumDestabilizerMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<QuantumAnalyzerMenu>> QUANTUM_ANALYZER_MENU =
            registerMenuType("quantum_analyzer_menu", QuantumAnalyzerMenu::new);

    private static <T extends AbstractContainerMenu>DeferredHolder<MenuType<?>, MenuType<T>> registerMenuType(String name,
                                                                                                              IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
