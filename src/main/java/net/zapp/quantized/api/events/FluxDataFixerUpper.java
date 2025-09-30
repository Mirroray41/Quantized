package net.zapp.quantized.api.events;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.zapp.quantized.FluxDataConfig;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.api.utils.DataFluxPair;
import net.zapp.quantized.init.ModComponents;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static net.zapp.quantized.FluxDataConfig.*;

@EventBusSubscriber(modid = Quantized.MOD_ID)
public class FluxDataFixerUpper {


    @SubscribeEvent
    public static void onModifyDefaults(ModifyDefaultComponentsEvent event) {
        load();
        for (Item item : BuiltInRegistries.ITEM) {
            DataFluxPair pair = getDataFlux(item);
            if (pair.isZero()) continue;

            event.modify(item, fixerUpper -> fixerUpper.set(ModComponents.DATA_FLUX_PAIR.get(), pair));
        }
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        DataFluxPair pair = stack.getOrDefault(ModComponents.DATA_FLUX_PAIR.get(), new DataFluxPair(0, 0));
        if (pair.isZero()) {
            pair = getAndConditionallyWriteFluxData(stack, event.getContext().level());
        }
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

    /**
     * Gets the DataFluxPair associated with this Item
     * @param item The item to get the data flux pair for.
     * @return The DataFluxPair, this pair can be 0 meaning there was no definition for this item,
     * either because it wasnt specified
     * or because the item's corresponding tag was used to give it a value.
     */
    public static DataFluxPair getDataFlux(Item item) {
        var id = BuiltInRegistries.ITEM.getKey(item);
        var d = itemMapView().get(id);
        if (d != null) return d;
        var holder = item.builtInRegistryHolder();
        for (var e : tagMapView().entrySet()) {
            if (holder.is(e.getKey()))
                return e.getValue();
        }
        return new DataFluxPair(0, 0);
    }

    /**
     * Gets the DataFluxPair associated with this itemStack and writes the value to the stack's dataComponent
     * This method is private since it can cause desync between server-client if used incorrectly.
     * @param stack The ItemStack to get and write the data for.
     * @return The DataFluxPair for this ItemStack, it could also be 0 (No Definition)
     * By this point Item Tags have been computed.
     */
    private static DataFluxPair getDataWriteToStack(ItemStack stack) {
        if (stack.isEmpty()) return DataFluxPair.zero();
        DataFluxPair existing = stack.get(ModComponents.DATA_FLUX_PAIR.get());
        if (existing != null && !existing.isZero()) return existing;
        DataFluxPair pair = getDataFlux(stack.getItem());
        if (!pair.isZero()) {
            stack.set(ModComponents.DATA_FLUX_PAIR.get(), pair);
            return pair;
        }
        return DataFluxPair.zero();
    }

    /**
     * Gets the DataFluxPair associated with this itemStack and writes the value to the stack's dataComponent
     * This method writes the datacomponent if it was called from the server, otherwise it fetches the last mapped
     * value for this ItemStack
     * @param stack The ItemStack to get and/or write the data from/to.
     * @param level The level this method was called from, can be null, if its null or client-side this method will NOT
     *              write the data component
     * @return The DataFluxPair associated with this itemStack, it can be 0.
     */
    public static DataFluxPair getAndConditionallyWriteFluxData(ItemStack stack, @Nullable Level level) {
        if (level != null && level.isClientSide) {
            return getDataFlux(stack.getItem());
        }
        return getDataWriteToStack(stack);
    }

    public static DataFluxPair getValuesFor(ResourceLocation id) { return itemMapView().get(id); }

    public static DataFluxPair getForTags(Set<TagKey<Item>> itemTags) {
        for (var k : tagMapView().keySet()) if (itemTags.contains(k)) return tagMapView().get(k);
        return null;
    }

}
