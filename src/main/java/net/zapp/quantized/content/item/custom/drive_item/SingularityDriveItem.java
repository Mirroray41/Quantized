package net.zapp.quantized.content.item.custom.drive_item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Optional;

/**
 * A drive that stores exactly <b>one</b> item pattern, regardless of that item's size/data value.
 *
 * <p>Backed by the same {@link DriveRecord} machinery as the tiered drives, but with a
 * {@link DriveRecord#singular() singular} record so {@link DriveRecord#canInsert} only accepts a
 * pattern while the drive is still empty. Crafted from the highest tier of compacted q-bit and used
 * by the Quantum Item Generator, which keeps fabricating the imprinted item as long as it has flux.
 */
public class SingularityDriveItem extends DriveItem {
    public SingularityDriveItem(Properties properties) {
        // capacity/maxSize are unused for singular drives; makeDefaultRecord() defines the real record.
        super(properties, DriveRecord.SINGULAR_CAPACITY, Integer.MAX_VALUE);
    }

    @Override
    public DriveRecord makeDefaultRecord() {
        return DriveRecord.singular();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        List<Item> stored = getStoredItems(stack);
        tooltip.add(Component.translatable("tooltip.quantized.disk.singular"));
        if (stored.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.quantized.disk.singular_empty"));
        } else {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stored.get(0));
            Optional<Item> itemOpt = BuiltInRegistries.ITEM.getOptional(id);
            String name = itemOpt.map(it -> it.getName(ItemStack.EMPTY).getString()).orElse(id.toString());
            tooltip.add(Component.translatable("tooltip.quantized.disk.singular_stored", name));
        }
    }
}
