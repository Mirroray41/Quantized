package net.zapp.quantized.core.utils.module;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.function.IntConsumer;

public class ItemModule implements Module {
    private final String moduleOwner;
    private final ItemStackHandler items;
    private final IntConsumer onChange;

    public ItemModule(String moduleOwner, int slots, IntConsumer onChange) {
        this.moduleOwner = moduleOwner;
        this.onChange = onChange != null ? onChange : s -> {};
        this.items = new ItemStackHandler(slots) {
            @Override
            protected void onContentsChanged(int slot) {
                ItemModule.this.onChange.accept(slot);
            }
        };
    }

    public ItemModule(String moduleOwner, ItemStackHandler handler) {
        this.moduleOwner = moduleOwner;
        this.onChange = s -> {};
        this.items = handler;
    }

    public ItemStackHandler getHandler() {
       return items;
   }

    public void dropAll(Level level, BlockPos blockPos) {
        SimpleContainer inv = new SimpleContainer(items.getSlots());
        for (int i = 0; i < items.getSlots(); i++) inv.setItem(i, items.getStackInSlot(i));
        Containers.dropContents(level, blockPos, inv);
    }

    @Override
    public void save(ValueOutput out, HolderLookup.Provider registries) {
        items.serialize(out);
    }

    @Override
    public void load(ValueInput in, HolderLookup.Provider registries) {
        items.deserialize(in);
    }

    public boolean canOutput(int outputSlot, int outputAmount, Item outputItem) {
        return items.insertItem(outputSlot, new ItemStack(outputItem, outputAmount), true).isEmpty();
    }
}
