package net.zapp.quantized.api.events;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.zapp.quantized.FluxDataConfig;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.api.utils.FluxDataPair;
import net.zapp.quantized.init.ModComponents;

@EventBusSubscriber(modid = Quantized.MOD_ID)
public class FluxDataFixerUpper {

    @SubscribeEvent
    public static void onModifyDefaults(ModifyDefaultComponentsEvent event) {
        FluxDataConfig.load();
        for (Item item : BuiltInRegistries.ITEM) {
            FluxDataPair pair = FluxDataConfig.getWithFallback(item);

            if (pair.isZero()) continue;

            event.modify(item, fixerUpper -> fixerUpper.set(ModComponents.INT_PAIR.get(), pair));
        }
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        FluxDataPair pair = stack.getOrDefault(ModComponents.INT_PAIR.get(), new FluxDataPair(0, 0));
        if (pair.isZero()) return;

        event.getToolTip().add(Component.translatable("tooltip.quantized.item.data_value", pair.data()));
        event.getToolTip().add(Component.translatable("tooltip.quantized.item.flux_value", pair.flux()));
    }


}
