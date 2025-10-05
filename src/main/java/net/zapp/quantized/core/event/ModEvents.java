package net.zapp.quantized.core.event;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.core.fluxdata.FluxDataJsonLoader;
import net.zapp.quantized.core.fluxdata.FluxDataFixerUpper;
import net.zapp.quantized.core.init.ModBlockEntities;
import net.zapp.quantized.core.networking.ModMessages;
import net.zapp.quantized.core.utils.DataFluxPair;

import java.util.List;

@EventBusSubscriber(modid = Quantized.MOD_ID)
public class ModEvents {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MACHINE_BLOCK_TILE.get(),
                (be, side) -> {
                    ItemStackHandler handler = be.getItemHandler();
                    if (side == Direction.UP) {
                        return new RangedWrapper(handler, 0, 1);
                    } else if (side == Direction.DOWN) {
                        return new RangedWrapper(handler, 1, 2);
                    } else {
                        return new RangedWrapper(handler, 0, 2);
                    }
                });

        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.MACHINE_BLOCK_TILE.get(),
                (be, side) -> be.getEnergyHandler());

        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.MACHINE_BLOCK_TILE.get(),
                (be, side) -> be.getFluidHandler());


        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.QUANTUM_DESTABILIZER_TILE.get(),
                (be, side) -> {
                    ItemStackHandler handler = be.getItemHandler();
                    if (side == Direction.UP) {
                        return new RangedWrapper(handler, 0, 1);
                    } else if (side == Direction.DOWN) {
                        return new RangedWrapper(handler, 1, 2);
                    } else {
                        return new RangedWrapper(handler, 0, 2);
                    }
                });

        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.QUANTUM_DESTABILIZER_TILE.get(),
                (be, side) -> be.getEnergyHandler());

        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.QUANTUM_DESTABILIZER_TILE.get(),
                (be, side) -> be.getFluidHandler());
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
        if (pair.isZero()) return;
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
