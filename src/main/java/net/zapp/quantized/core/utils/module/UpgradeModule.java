package net.zapp.quantized.core.utils.module;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.zapp.quantized.content.item.custom.upgrade.UpgradeCardItem;
import net.zapp.quantized.content.item.custom.upgrade.UpgradeType;

import java.util.List;

/**
 * Holds a machine's upgrade cards: exactly one slot per accepted {@link UpgradeType}, in the order
 * given at construction. Each slot only accepts a card of its own type and at most one card, so the
 * number of unique upgrades a machine has equals the number of accepted types.
 *
 * <p>The tile reads {@link #speedTicks}, {@link #efficiencyCost} and {@link #outputMultiplied} when
 * computing its per-operation numbers; an empty/absent slot contributes a neutral 1.0 multiplier.
 */
public class UpgradeModule implements Module {
    private final String owner;
    private final List<UpgradeType> types;
    private final ItemStackHandler handler;
    private final Runnable onChange;

    public UpgradeModule(String owner, List<UpgradeType> types, Runnable onChange) {
        this.owner = owner;
        this.types = List.copyOf(types);
        this.onChange = onChange != null ? onChange : () -> {};
        this.handler = new ItemStackHandler(this.types.size()) {
            @Override
            protected void onContentsChanged(int slot) {
                UpgradeModule.this.onChange.run();
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return accepts(slot, stack);
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (!accepts(slot, stack)) return stack;
                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    private boolean accepts(int slot, ItemStack stack) {
        return stack.getItem() instanceof UpgradeCardItem card && card.type() == types.get(slot);
    }

    public ItemStackHandler getHandler() {
        return handler;
    }

    public List<UpgradeType> types() {
        return types;
    }

    /** The {@link UpgradeType} the slot at this index accepts. */
    public UpgradeType typeForSlot(int slot) {
        return types.get(slot);
    }

    /** The card multiplier for a stat, or 1.0 if the type is unsupported / its slot is empty. */
    private double multiplier(UpgradeType type) {
        int slot = types.indexOf(type);
        if (slot < 0) return 1.0;
        ItemStack s = handler.getStackInSlot(slot);
        return s.getItem() instanceof UpgradeCardItem card && card.type() == type ? card.value() : 1.0;
    }

    /** Base processing ticks scaled by the SPEED card (faster = fewer ticks). Floored at 1. */
    public int speedTicks(int baseTicks) {
        return Math.max(1, (int) Math.round(baseTicks / multiplier(UpgradeType.SPEED)));
    }

    /** Base resource cost scaled by the EFFICIENCY card (cheaper). Floored at 1. */
    public int efficiencyCost(int baseCost) {
        return Math.max(1, (int) Math.round(baseCost / multiplier(UpgradeType.EFFICIENCY)));
    }

    /** Base scaled UP by the EFFICIENCY card, e.g. fuel burning longer per item. Never below base. */
    public int efficiencyScaled(int base) {
        return Math.max(base, (int) Math.round(base * multiplier(UpgradeType.EFFICIENCY)));
    }

    /** Base production scaled by the OUTPUT card (more). Never below the base amount. */
    public int outputMultiplied(int baseAmount) {
        return Math.max(baseAmount, (int) Math.round(baseAmount * multiplier(UpgradeType.OUTPUT)));
    }

    public void dropAll(Level level, BlockPos pos) {
        SimpleContainer inv = new SimpleContainer(handler.getSlots());
        for (int i = 0; i < handler.getSlots(); i++) inv.setItem(i, handler.getStackInSlot(i));
        Containers.dropContents(level, pos, inv);
    }

    @Override
    public void save(CompoundTag out, HolderLookup.Provider registries) {
        out.put(owner + ".upgrades", handler.serializeNBT(registries));
    }

    @Override
    public void load(CompoundTag in, HolderLookup.Provider registries) {
        if (in.contains(owner + ".upgrades")) {
            handler.deserializeNBT(registries, in.getCompound(owner + ".upgrades"));
        }
    }
}
