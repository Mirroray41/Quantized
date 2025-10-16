package net.zapp.quantized.core.utils.module;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.zapp.quantized.content.item.custom.drive_item.DriveItem;
import net.zapp.quantized.content.item.custom.drive_item.DriveRecord;
import net.zapp.quantized.core.fluxdata.FluxDataFixerUpper;
import net.zapp.quantized.core.init.ModDataComponents;
import net.zapp.quantized.core.utils.DataFluxPair;

import java.util.*;

public class DriveInterfaceModule implements Module {
    private final ItemStackHandler backingHandler;
    private final List<String> driveItems;
    private final List<String> filteredItems;
    private final int[] driveSlots;
    private final int[] driveGhostSlots;
    private final DriveRecord[] driveData;
    private final int ghostRows;
    private final int ghostCols;
    private final Runnable onUpdate;

    private String filter = "";
    private int rowOffset = 0;

    public DriveInterfaceModule(ItemStackHandler backingHandler, int driveSlotStart, int driveSlotsSize, int ghostSlotStart, int ghostSlotSize, int ghostRows, int ghostCols, Runnable onUpdate) {
        this(backingHandler, createSlotRange(driveSlotStart, driveSlotsSize), createSlotRange(ghostSlotStart, ghostSlotSize), ghostRows, ghostCols, onUpdate);
    }

    public DriveInterfaceModule(ItemStackHandler backingHandler, int[] driveSlots, int[] driveGhostSlots, int ghostRows, int ghostCols, Runnable onUpdate) {
        this.backingHandler = backingHandler;
        this.driveItems = new ArrayList<>();
        this.filteredItems = new ArrayList<>();
        this.driveSlots = driveSlots;
        this.driveGhostSlots = driveGhostSlots;
        this.ghostRows = ghostRows;
        this.ghostCols = ghostCols;
        this.onUpdate = onUpdate;
        this.driveData = new DriveRecord[driveSlots.length];

        getDriveData();
    }

    private void getDriveData() {
        for (int i : driveSlots) {
            ItemStack stack = backingHandler.getStackInSlot(i);
            if (!(stack.getItem() instanceof DriveItem)) {
                driveData[i - driveSlots[0]] = DriveRecord.blank();
                continue;
            }
            if (!stack.has(ModDataComponents.DRIVE_DATA)) {
                stack.set(ModDataComponents.DRIVE_DATA, DriveRecord.blank());
            }
            driveData[i - driveSlots[0]] = stack.get(ModDataComponents.DRIVE_DATA);
        }
    }

    public void recacheDisks() {
        driveItems.clear();
        getDriveData();
        List<String> temp = new ArrayList<>();
        for (int i : driveSlots) {
            temp.addAll(List.of(driveData[i - driveSlots[0]].items()));
        }
        driveItems.addAll(temp.stream().distinct().toList());
        recomputeItemSlots();
    }

    public void recomputeItemSlots() {
        recomputeFiltered();
        for (int i = 0; i < driveGhostSlots.length; i++) {
            int idx = i + (rowOffset * ghostCols);
            if (idx < filteredItems.size()) {
                ResourceLocation rl = ResourceLocation.parse(filteredItems.get(idx));
                Optional<Holder.Reference<Item>> opt = BuiltInRegistries.ITEM.get(rl);
                if (opt.isPresent()) {
                    backingHandler.setStackInSlot(i + driveGhostSlots[0], new ItemStack(opt.get().value()));
                } else {
                    backingHandler.setStackInSlot(i + driveGhostSlots[0], ItemStack.EMPTY);
                }
            } else {
                backingHandler.setStackInSlot(i + driveGhostSlots[0], ItemStack.EMPTY);
            }
        }
        onUpdate.run();
    }

    public void filter(String newFilter) {
        String f = newFilter == null ? "" : newFilter.trim().toLowerCase(Locale.ROOT);
        if (!Objects.equals(filter, f)) {
            filter = f;
            recomputeItemSlots();
        }
    }

    private void recomputeFiltered() {
        if (filter.isEmpty()) {
            filteredItems.clear();
            filteredItems.addAll(driveItems);
        } else {
            filteredItems.clear();
            for (String s: driveItems) {
                if (s == null) continue;
                String lc = s.toLowerCase(Locale.ROOT);
                if (lc.contains(filter)) filteredItems.add(s);
                else {
                    try {
                        ResourceLocation rl = ResourceLocation.parse(s);
                        Optional<Holder.Reference<Item>> opt = BuiltInRegistries.ITEM.get(rl);
                        if (opt.isPresent()) {
                            Item it = opt.get().value();
                            String name = it.getDescriptionId().toLowerCase(Locale.ROOT);
                            if (name.contains(filter)) filteredItems.add(s);
                        }
                    } catch (Exception ignored) {}
                }
            }

            int rows = (int) Math.ceil(filteredItems.size() / (double) ghostCols);
            if (rowOffset >= Math.max(0, rows - ghostRows)) {
                rowOffset = Math.max(0, rows - ghostRows);
            }
        }
        onUpdate.run();
    }

    public void resetFilterAndScroll() {
        filter = "";
        rowOffset = 0;
        recomputeItemSlots();
        onUpdate.run();
    }

    public static int[] createSlotRange(int startIndex, int size) {
        int[] slots = new int[size];
        for (int i = 0; i < size; i++) {
            slots[i] = i + startIndex;
        }
        return slots;
    }

    public int getFilteredSize() {
        return filteredItems.size();
    }

    public void setRowOffset(int offset) {
        rowOffset = offset;
        recomputeItemSlots();
    }

    public int getRowOffset() {
        return rowOffset;
    }


    public boolean containsItem(Item item) {
        return driveItems.contains(item.toString());
    }

    public boolean canInsertIntoDrives(Item item) {
        if (driveItems.isEmpty()) return false;
        DataFluxPair df = FluxDataFixerUpper.getDataFlux(item);
        if (!DataFluxPair.isValid(df)) return false;
        if (containsItem(item)) return false;
        for (int i = 0; i < driveData.length; i++) {
            DriveRecord record = driveData[i];
            if (record.canInsert(df)) return true;
        }
        return false;
    }

    public void insertIntoDrives(Item item) {
        DataFluxPair df = FluxDataFixerUpper.getDataFlux(item);
        if (!DataFluxPair.isValid(df)) return;
        if (containsItem(item)) return;
        for (int i = 0; i < driveData.length; i++) {
            DriveRecord rcd = driveData[i];
            if (rcd.canInsert(df)) {
                ItemStack drive = backingHandler.getStackInSlot(i + driveSlots[0]);
                DriveItem.addItem(drive, new ItemStack(item, 1), df);
                recacheDisks();
                return;
            }
        }
        recacheDisks();
    }

}
