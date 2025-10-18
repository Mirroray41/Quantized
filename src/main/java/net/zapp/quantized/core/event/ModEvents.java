package net.zapp.quantized.core.event;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.core.fluxdata.FluxDataFixerUpper;
import net.zapp.quantized.core.fluxdata.FluxDataJsonLoader;
import net.zapp.quantized.core.networking.ModMessages;
import net.zapp.quantized.core.utils.DataFluxPair;
import java.util.List;

import static net.zapp.quantized.core.event.TileCapabilities.*;

@EventBusSubscriber(modid = Quantized.MOD_ID)
public class ModEvents {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        destabilizerCaps(event);
        analyzerCaps(event);
        stabilizerCaps(event);
        fluxGeneratorCaps(event);
        fabricatorCaps(event);
    }

    @SubscribeEvent
    public static void registerPayloadHandlersEvent(RegisterPayloadHandlersEvent event) {
        ModMessages.register(event);
    }

    @SubscribeEvent
    public static void onAddReload(AddServerReloadListenersEvent event) {
        event.addListener(Quantized.id("data_flux_json_loader"), new FluxDataJsonLoader());
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        DataFluxPair pair = FluxDataFixerUpper.getDataFluxFromStack(stack);
        if (!DataFluxPair.isValid(pair)) return;
        if (stack.getCount() == 1) {
            addTooltipToItem(event.getToolTip(), pair.data(), pair.flux());
            return;
        }
        int data = pair.data() * stack.getCount();
        int flux = pair.flux() * stack.getCount();
        if (Screen.hasShiftDown()) {
            addShiftTooltipToItem(event.getToolTip(), data, flux, stack.getCount());
        } else {
            addTooltipToItem(event.getToolTip(), data, flux);
        }

    }

    private static void addTooltipToItem(List<Component> tooltip, int data, int flux) {
        if (data == 1)
            tooltip.add(Component.translatable("tooltip.quantized.item.data_value_singular"));
        else
            tooltip.add(Component.translatable("tooltip.quantized.item.data_value", data));
        tooltip.add(Component.translatable("tooltip.quantized.item.flux_value", flux));
    }

    private static void addShiftTooltipToItem(List<Component> tooltip, int data, int flux, int stackCount) {
        int individualData = data / stackCount;
        int individualFlux = flux / stackCount;
        if (data == 1)
            tooltip.add(Component.translatable("tooltip.quantized.item.data_value_singular"));
        else
            tooltip.add(Component.translatable("tooltip.quantized.item.data_value_shift", data, stackCount, individualData));
        tooltip.add(Component.translatable("tooltip.quantized.item.flux_value_shift", flux, stackCount, individualFlux));
    }


}
