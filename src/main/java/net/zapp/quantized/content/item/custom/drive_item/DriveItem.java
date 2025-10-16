package net.zapp.quantized.content.item.custom.drive_item;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.zapp.quantized.core.init.ModDataComponents;
import net.zapp.quantized.core.init.ModItems;
import net.zapp.quantized.core.utils.DataFluxPair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class DriveItem extends Item {
    private final int capacity;
    private final int maxPatternSize;

    public DriveItem(Properties properties, int capacity, int maxPatternSize) {
        super(properties);
        this.capacity = capacity;
        this.maxPatternSize = maxPatternSize;
    }

    public static List<String> getStoredItemNames(ItemStack drive) {
        if (!(drive.getItem() instanceof DriveItem)) return List.of();
        if (!drive.has(ModDataComponents.DRIVE_DATA)) {
            drive.set(ModDataComponents.DRIVE_DATA, DriveRecord.blank());
            return List.of();
        }
        DriveRecord diskData = drive.get(ModDataComponents.DRIVE_DATA);
        return Arrays.asList(diskData.items());
    }

    public static List<Item> getStoredItems(ItemStack drive) {
        if (!(drive.getItem() instanceof DriveItem)) return List.of();
        if (!drive.has(ModDataComponents.DRIVE_DATA)) {
            drive.set(ModDataComponents.DRIVE_DATA, DriveRecord.blank());
            return List.of();
        }
        DriveRecord diskData = drive.get(ModDataComponents.DRIVE_DATA);
        List<String> items = new ArrayList<>(Arrays.stream(diskData.items()).toList());
        if (items.isEmpty()) return List.of();
        List<Item> storedItems = new ArrayList<>();
        for (String s : items) {
            ResourceLocation id = ResourceLocation.tryParse(s);
            if (id == null) continue;
            BuiltInRegistries.ITEM.get(id).ifPresent(holder -> storedItems.add(holder.value()));
        }
        return storedItems;
    }
    
    public static void addItem(ItemStack drive, ItemStack toAdd, DataFluxPair df) {
        DriveRecord diskData = drive.get(ModDataComponents.DRIVE_DATA);
        if (diskData == null) return;
        List<Item> items = new ArrayList<>(getStoredItems(drive));
        items.add(toAdd.getItem());
        List<String> itemStrs = items.stream().map(Item::toString).toList();
        
        DriveRecord newData = new DriveRecord(diskData.capacity(), diskData.maxSizePerItem(), diskData.dataUsed() + df.data(), itemStrs.toArray(new String[diskData.count() + 1]), diskData.count() + 1);
        drive.set(ModDataComponents.DRIVE_DATA, newData);
        
    }

    @Override
    public @NotNull ItemStack getCraftingRemainder(ItemStack itemStack) {
        return new ItemStack(ModItems.DRIVE_CASING.get());
    }

    private void initializeDriveData(ItemStack stack) {
        if (!stack.has(ModDataComponents.DRIVE_DATA.get())) {
            stack.set(ModDataComponents.DRIVE_DATA.get(),
                    new DriveRecord(capacity, maxPatternSize, 0, new String[0], 0));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);

        initializeDriveData(stack);
        DriveRecord data = stack.get(ModDataComponents.DRIVE_DATA);
        tooltipAdder.accept(Component.translatable("tooltip.quantized.disk.data", data.dataUsed(), data.capacity()));
        tooltipAdder.accept(Component.translatable("tooltip.quantized.disk.max_size",data.maxSizePerItem()));
        tooltipAdder.accept(Component.translatable("tooltip.quantized.disk.count",data.count()));
        if (data.count() > 0) {
            if (!Screen.hasShiftDown()) {
                tooltipAdder.accept(Component.translatable("tooltip.quantized.disk.items"));
            } else {
                tooltipAdder.accept(Component.translatable("tooltip.quantized.disk.items_shift"));
                if (data.count() < 8) {
                    for (int i = 0; i < data.count(); i++) {
                        ResourceLocation location = ResourceLocation.parse(data.items()[i]);
                        Item item = BuiltInRegistries.ITEM.get(location).get().value();
                        tooltipAdder.accept(Component.translatable("tooltip.quantized.disk.item",item.getName().getString()));
                    }
                } else {
                    for (int i = 0; i < 8; i++) {
                        ResourceLocation location = ResourceLocation.parse(data.items()[i]);
                        Item item = BuiltInRegistries.ITEM.get(location).get().value();
                        tooltipAdder.accept(Component.translatable("tooltip.quantized.disk.item",item.getName().getString()));
                    }
                    tooltipAdder.accept(Component.translatable("tooltip.quantized.disk.item_more", data.count() - 8));
                }
            }
        }
    }
}
