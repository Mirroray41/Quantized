package net.zapp.quantized.content.item.custom.drive_item;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.zapp.quantized.core.fluxdata.FluxDataFixerUpper;
import net.zapp.quantized.core.init.ModDataComponents;
import net.zapp.quantized.core.init.ModItems;
import net.zapp.quantized.core.utils.DataFluxPair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class DriveItem extends Item {
    private final int capacity;
    private final int maxPatternSize;

    public DriveItem(Properties properties, int capacity, int maxPatternSize) {
        super(properties.stacksTo(1));
        this.capacity = capacity;
        this.maxPatternSize = maxPatternSize;
    }

    /** The DRIVE_DATA a freshly-made disk of this item tier should start with. */
    public DriveRecord makeDefaultRecord() {
        return new DriveRecord(capacity, maxPatternSize, 0, new String[0], 0);
    }

    /** Tier-aware default record for whatever drive a stack is; blank for non-drives. */
    public static DriveRecord defaultRecordFor(ItemStack stack) {
        return stack.getItem() instanceof DriveItem d ? d.makeDefaultRecord() : DriveRecord.blank();
    }

    public static List<String> getStoredItemNames(ItemStack drive) {
        if (!(drive.getItem() instanceof DriveItem)) return List.of();
        if (!drive.has(ModDataComponents.DRIVE_DATA)) {
            drive.set(ModDataComponents.DRIVE_DATA, defaultRecordFor(drive));
            return List.of();
        }
        DriveRecord diskData = drive.get(ModDataComponents.DRIVE_DATA);
        return Arrays.asList(diskData.items());
    }

    public static List<Item> getStoredItems(ItemStack drive) {
        if (!(drive.getItem() instanceof DriveItem)) return List.of();
        if (!drive.has(ModDataComponents.DRIVE_DATA)) {
            drive.set(ModDataComponents.DRIVE_DATA, defaultRecordFor(drive));
            return List.of();
        }
        DriveRecord diskData = drive.get(ModDataComponents.DRIVE_DATA);
        List<String> items = new ArrayList<>(Arrays.stream(diskData.items()).toList());
        if (items.isEmpty()) return List.of();
        List<Item> storedItems = new ArrayList<>();
        for (String s : items) {
            ResourceLocation id = ResourceLocation.tryParse(s);
            if (id == null) continue;
            Optional<Item> itemOpt = BuiltInRegistries.ITEM.getOptional(id);
            itemOpt.ifPresent(storedItems::add);
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

    public static boolean removeItem(ItemStack drive, Item toRemove) {
        if (drive == null || drive.isEmpty() || !(drive.getItem() instanceof DriveItem)) return false;

        DriveRecord rec = drive.get(ModDataComponents.DRIVE_DATA);
        if (rec == null) rec = defaultRecordFor(drive);

        String key = DriveRecord.keyOf(toRemove);
        if (!rec.containsItemString(key)) return false;

        DataFluxPair df = FluxDataFixerUpper.getDataFlux(toRemove);
        int dfPerItem = DataFluxPair.isValid(df) ? df.data() : 0;

        DriveRecord updated = rec.withItemRemoved(key, dfPerItem);
        if (Objects.equals(updated, rec)) return false;

        drive.set(ModDataComponents.DRIVE_DATA, updated);
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return new ItemStack(ModItems.DRIVE_CASING.get());
    }

    private void initializeDriveData(ItemStack stack) {
        if (!stack.has(ModDataComponents.DRIVE_DATA.get())) {
            stack.set(ModDataComponents.DRIVE_DATA.get(), makeDefaultRecord());
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        initializeDriveData(stack);
        DriveRecord data = stack.get(ModDataComponents.DRIVE_DATA);
        tooltip.add(Component.translatable("tooltip.quantized.disk.data", data.dataUsed(), data.capacity()));
        tooltip.add(Component.translatable("tooltip.quantized.disk.max_size", data.maxSizePerItem()));
        tooltip.add(Component.translatable("tooltip.quantized.disk.count", data.count()));

        if (data.count() > 0) {
            if (!Screen.hasShiftDown()) {
                tooltip.add(Component.translatable("tooltip.quantized.disk.items"));
            } else {
                tooltip.add(Component.translatable("tooltip.quantized.disk.items_shift"));
                int itemsToShow = Math.min(data.count(), 8);

                for (int i = 0; i < itemsToShow; i++) {
                    ResourceLocation location = ResourceLocation.parse(data.items()[i]);
                    Optional<Item> itemOpt = BuiltInRegistries.ITEM.getOptional(location);
                    if (itemOpt.isPresent()) {
                        Item item = itemOpt.get();
                        tooltip.add(Component.translatable("tooltip.quantized.disk.item", item.getName(ItemStack.EMPTY).getString()));
                    }
                }

                if (data.count() > 8) {
                    tooltip.add(Component.translatable("tooltip.quantized.disk.item_more", data.count() - 8));
                }
            }
        }
    }
}
