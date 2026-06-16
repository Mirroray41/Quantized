package net.zapp.quantized.content.blocks.quantum_replicator;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.zapp.quantized.content.item.custom.drive_item.SingularityDriveItem;
import net.zapp.quantized.core.fluxdata.FluxDataFixerUpper;
import net.zapp.quantized.core.init.ModBlocks;
import net.zapp.quantized.core.init.ModMenuTypes;
import net.zapp.quantized.core.utils.DataFluxPair;

public class QuantumReplicatorMenu extends AbstractContainerMenu {
    public final QuantumReplicatorTile blockEntity;
    private final Level level;
    private final ContainerData data;

    public QuantumReplicatorMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(6));
    }

    public QuantumReplicatorMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.QUANTUM_REPLICATOR_MENU.get(), pContainerId);
        this.blockEntity = (QuantumReplicatorTile) entity;
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        // Disk slot — only the singularity drive.
        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), QuantumReplicatorTile.DISK_SLOT, 44, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof SingularityDriveItem;
            }
        });
        // Sample slot — any item with a flux value; used to imprint the disk.
        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), QuantumReplicatorTile.SAMPLE_SLOT, 80, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return DataFluxPair.isValid(FluxDataFixerUpper.getDataFluxFromStack(stack));
            }
        });
        // Output slot — replicated items, extract only.
        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), QuantumReplicatorTile.OUTPUT_SLOT, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        addDataSlots(data);
    }

    public int getProgress() {
        return data.get(0);
    }

    public int getMaxProgress() {
        return data.get(1);
    }

    public boolean isWorking() {
        return data.get(0) > 0;
    }

    public int getPowerConsumption() {
        return data.get(2);
    }

    public int getEnergyStored() {
        return data.get(3);
    }

    public int getEnergyCapacity() {
        return data.get(4);
    }

    public int getFluidCapacity() {
        return data.get(5);
    }

    public FluidStack getFluid() {
        return blockEntity.getFluid();
    }

    public int getScaledEnergyBar() {
        int energyStored = data.get(3);
        int maxEnergy = data.get(4);
        int barPixelSize = 54;
        return maxEnergy != 0 && energyStored != 0 ? energyStored * barPixelSize / maxEnergy : 0;
    }

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    private static final int TE_INVENTORY_SLOT_COUNT = 3;

    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // From player inventory into the machine (disk/sample slots).
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
                    + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            // From the machine back to the player inventory.
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                pPlayer, ModBlocks.QUANTUM_REPLICATOR.get());
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
