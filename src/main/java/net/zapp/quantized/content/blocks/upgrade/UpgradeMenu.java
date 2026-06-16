package net.zapp.quantized.content.blocks.upgrade;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.zapp.quantized.core.init.ModMenuTypes;
import net.zapp.quantized.core.utils.module.identifiers.HasUpgradeModule;

/**
 * A standalone container that exposes only a machine's upgrade slots (one per accepted card type),
 * so upgrades live in their own GUI instead of crowding each machine's interface. Opened via the
 * machine screen's upgrades button; a back button re-opens the machine.
 */
public class UpgradeMenu extends AbstractContainerMenu {
    private final Level level;
    private final BlockPos pos;
    private final ItemStackHandler upgrades;

    public UpgradeMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public UpgradeMenu(int id, Inventory inv, BlockEntity entity) {
        super(ModMenuTypes.UPGRADE_MENU.get(), id);
        this.level = inv.player.level();
        this.pos = entity != null ? entity.getBlockPos() : inv.player.blockPosition();
        this.upgrades = entity instanceof HasUpgradeModule hum
                ? hum.getUpgradeHandler()
                : new ItemStackHandler(0);

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        // Upgrade slots, centred as a horizontal row.
        int count = upgrades.getSlots();
        int startX = 88 - count * 9;
        for (int i = 0; i < count; i++) {
            addSlot(new SlotItemHandler(upgrades, i, startX + i * 18, 35));
        }
    }

    public BlockPos getMachinePos() {
        return pos;
    }

    private static final int VANILLA_SLOT_COUNT = 36;

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copy = sourceStack.copy();

        int upgradeStart = VANILLA_SLOT_COUNT;
        int upgradeEnd = slots.size();

        if (index < VANILLA_SLOT_COUNT) {
            // Player inventory -> upgrade slots (type restriction enforced by the handler).
            if (!moveItemStackTo(sourceStack, upgradeStart, upgradeEnd, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Upgrade slot -> player inventory.
            if (!moveItemStackTo(sourceStack, 0, VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(player, sourceStack);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return level.getBlockEntity(pos) instanceof HasUpgradeModule
                && player.canInteractWithBlock(pos, 4.0);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}
