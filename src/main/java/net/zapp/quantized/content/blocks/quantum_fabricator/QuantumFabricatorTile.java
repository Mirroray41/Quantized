package net.zapp.quantized.content.blocks.quantum_fabricator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.zapp.quantized.content.blocks.ProcessingCurves;
import net.zapp.quantized.content.blocks.quantum_destabilizer.QuantumDestabilizerTile;
import net.zapp.quantized.content.item.custom.drive_item.DriveItem;
import net.zapp.quantized.core.fluxdata.FluxDataFixerUpper;
import net.zapp.quantized.core.init.ModBlockEntities;
import net.zapp.quantized.core.init.ModFluids;
import net.zapp.quantized.core.init.ModSounds;
import net.zapp.quantized.core.utils.DataFluxPair;
import net.zapp.quantized.content.item.custom.upgrade.UpgradeType;
import net.zapp.quantized.core.utils.module.DriveInterfaceModule;
import net.zapp.quantized.core.utils.module.EnergyModule;
import net.zapp.quantized.core.utils.module.ItemModule;
import net.zapp.quantized.core.utils.module.TankModule;
import net.zapp.quantized.core.utils.module.UpgradeModule;
import net.zapp.quantized.core.utils.module.identifiers.HasDriveInterfaceModule;
import net.zapp.quantized.core.utils.module.identifiers.HasEnergyModule;
import net.zapp.quantized.core.utils.module.identifiers.HasItemModule;
import net.zapp.quantized.core.utils.module.identifiers.HasTankModule;
import net.zapp.quantized.core.utils.module.identifiers.HasUpgradeModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class QuantumFabricatorTile extends BlockEntity implements MenuProvider, HasEnergyModule, HasItemModule, HasTankModule, HasDriveInterfaceModule, HasUpgradeModule {
    // ---- Rendering init ----
    private static final float ROTATION = 10f;

    public float prevRotation;
    public float rotation;

    // ---- Slots ----
    private static final int OUTPUT_SLOT = 0;
    private static final int[] DRIVE_SLOTS = new int[] {1, 2, 3, 4, 5, 6};
    private static final int[] DRIVE_GHOST_SLOTS = new int[] {7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33};


    // ---- Energy/Fluids constants ----
    public static final int FE_CAPACITY = 1_000_000;
    public static final int TANK_CAPACITY = 8_000_000;

    // ---- Modules (storage-only) ----
    private final String ownerName = "QuantumFabricatorTile";
    private final ItemModule itemM = new ItemModule(ownerName, new ItemStackHandler(34) {
        @Override
        protected void onContentsChanged(int slot) {
            if (slot >= 1 && slot <= 6) {
                driveM.recacheDisks();
            }
            markDirtyAndUpdate();
        }

        @Override
        @NotNull
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (slot >= 7) return stack;
            if (stack.getItem() instanceof DriveItem) {
                driveM.recacheDisks();
            }
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot >= 7) return ItemStack.EMPTY;
            if (slot >= 1) {
                driveM.recacheDisks();
            }
            return super.extractItem(slot, amount, simulate);
        }
    });
    private final EnergyModule energyM = new EnergyModule(ownerName, FE_CAPACITY, Integer.MAX_VALUE, Integer.MAX_VALUE, true, true);
    private final TankModule tankM = new TankModule(ownerName, TANK_CAPACITY, fs -> fs.getFluidType() == ModFluids.QUANTUM_FLUX.get().getFluidType(), s -> markDirtyAndUpdate());
    private final DriveInterfaceModule driveM = new DriveInterfaceModule(getItemHandler(), DRIVE_SLOTS, DRIVE_GHOST_SLOTS, 3, 9, this::markDirtyAndUpdate);
    private final UpgradeModule upgradeM = new UpgradeModule(ownerName,
            List.of(UpgradeType.SPEED, UpgradeType.EFFICIENCY), this::markDirtyAndUpdate);

    // ---- Menu sync data ----
    private int progress = 0;
    private int maxProgress = 72;
    public int powerConsumption = 16;
    private int outputAmount = 0;
    private int selectedSlot = -1;
    int viewers = 0;


    private boolean wasWorking = false;
    private ItemStack selectedItem = ItemStack.EMPTY;
    private ItemStack cachedOut = ItemStack.EMPTY;

    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int i) {
            return switch (i) {
                case 0 -> progress;
                case 1 -> maxProgress;
                case 2 -> powerConsumption;
                case 3 -> energyM.getHandler().getEnergy();
                case 4 -> energyM.getHandler().getMaxEnergyStored();
                case 5 -> tankM.getHandler().getCapacity();
                case 6 -> driveM.getFilteredSize();
                case 7 -> driveM.getRowOffset();
                case 8 -> outputAmount;
                case 9 -> selectedSlot;
                default -> 0;
            };
        }

        @Override
        public void set(int i, int value) {
            switch (i) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
                case 2 -> powerConsumption = value;
                case 7 -> driveM.setRowOffset(value);
                case 8 -> outputAmount = value;
                case 9 -> selectedSlot = value;
            }
        }

        @Override
        public int getCount() {
            return 10;
        }
    };

    public QuantumFabricatorTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.QUANTUM_FABRICATOR_TILE.get(), pos, state);
    }

    // ---- UI / Menu ----
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.quantized.tile.quantum_fabricator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        driveM.recacheDisks();
        return new QuantumFabricatorMenu(id, inv, this, data);
    }

    // --- Tick ---
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        if (selectedItem.isEmpty() || outputAmount <= 0) {
            resetCraft();
            return;
        }
        // Stop fabricating if the drive that supplied the selected item was removed/changed so it is
        // no longer present in any inserted drive. Without this the tile keeps running on the stale
        // cached selectedItem reference even after every drive is pulled out.
        if (!driveM.containsItem(selectedItem.getItem())) {
            selectItem(-1);
            setWorking(level, pos, state, false);
            return;
        }
        if (cachedOut.isEmpty()) {
            resetCraft();
            cachedOut = selectedItem;
        }
        else if (!selectedItem.is(cachedOut.getItem())) {
            resetCraft();
            cachedOut = selectedItem;
        }

        DataFluxPair df = FluxDataFixerUpper.getDataFluxFromStack(selectedItem);
        if (!DataFluxPair.isValid(df)) {
            resetCraft();
            setWorking(level, pos, state, false);
            return;
        }

        maxProgress = upgradeM.speedTicks(ProcessingCurves.timeTicks(df.data()));
        int toConsume = upgradeM.efficiencyCost(ProcessingCurves.powerPerTick(df.flux()));
        int fluxCost = upgradeM.efficiencyCost(df.flux());

        boolean canPay = energyM.canPay(toConsume) && tankM.canPay(fluxCost);
        boolean canOut = itemM.canOutput(OUTPUT_SLOT, 1, selectedItem.getItem());
        boolean working = canPay && canOut;
        setWorking(level, pos, state, working);
        if (!working) {
            powerConsumption = 0; // Fixes people thinking the machine is using power even when it's not.
            return;
        }

        powerConsumption = toConsume;
        progress++;
        energyM.extractPower(powerConsumption);

        level.playSound(null, pos, ModSounds.QUANTUM_FABRICATOR_WORK.value(), SoundSource.BLOCKS, 1f, 1f + (float) progress / (float) maxProgress);

        if (progress >= maxProgress) {
            tankM.drainFluid(fluxCost);
            itemM.getHandler().insertItem(OUTPUT_SLOT, selectedItem.copy(), false);
            outputAmount--;
            if (outputAmount == 0) {
                selectedSlot = -1;
                selectedItem = ItemStack.EMPTY;
            }
            progress = 0;
            driveM.recomputeItemSlots();
        }
    }

    // ---- helpers ----
    private void setWorking(Level level, BlockPos pos, BlockState state, boolean working) {
        if (wasWorking != working) {
            wasWorking = working;
            BlockState ns = state.setValue(QuantumFabricator.ON, working);
            setChanged(level, pos, ns);
            level.setBlock(pos, ns, 3);
        }
    }

    private void resetCraft() {
        progress = 0;
        powerConsumption = 0;
        maxProgress = 72;
        cachedOut = ItemStack.EMPTY;
    }


    // ---- Drop items when broken ----
    public void drops() {
        if (level == null) return;
        SimpleContainer inv = new SimpleContainer(itemM.getHandler().getSlots());
        for (int i = 0; i < itemM.getHandler().getSlots() - 27; i++) {
            inv.setItem(i, itemM.getHandler().getStackInSlot(i));
        }
        Containers.dropContents(level, worldPosition, inv);
        upgradeM.dropAll(level, worldPosition);
    }

    // ---- Save / Load ----
    @Override
    protected void saveAdditional(CompoundTag out, HolderLookup.Provider registries) {
        //HolderLookup.Provider regs = level != null ? level.registryAccess() : null;

        // modules
        itemM.save(out, registries);
        energyM.save(out, registries);
        tankM.save(out, registries);
        upgradeM.save(out, registries);

        // local fields
        out.putInt("progress", progress);
        out.putInt("maxProgress", maxProgress);
        out.putInt("outputAmount", outputAmount);
        super.saveAdditional(out, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag in, HolderLookup.Provider registries) {
        super.loadAdditional(in, registries);
        //HolderLookup.Provider regs = level != null ? level.registryAccess() : null;

        // modules
        itemM.load(in, registries);
        energyM.load(in, registries);
        tankM.load(in, registries);
        upgradeM.load(in, registries);

        // local fields
        progress = in.getInt("progress");
        maxProgress = in.getInt("maxProgress");
        outputAmount = in.getInt("outputAmount");
    }

    // ---- Network sync ----
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }


    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void markDirtyAndUpdate() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void selectItem(int slot) {
        if (slot == -1) {
            selectedSlot = -1;
            selectedItem = ItemStack.EMPTY;
            outputAmount = 0;
            resetCraft();
            markDirtyAndUpdate();
            return;
        }
        selectedSlot = slot;
        // Copy so the selection is a stable snapshot, not a live reference to the ghost slot stack
        // (which gets overwritten/cleared whenever the drive list is recomputed).
        selectedItem = itemM.getHandler().getStackInSlot(slot).copy();
        markDirtyAndUpdate();
    }


    public ItemStack getSelectedItem() {
        return selectedItem;
    }

    @Override
    public @NotNull EnergyModule getEnergyModule() {
        return energyM;
    }

    @Override
    public @NotNull ItemModule getItemModule() {
        return itemM;
    }

    @Override
    public @NotNull TankModule getTankModule() {
        return tankM;
    }
    @Override
    public @NotNull DriveInterfaceModule getDriveInterfaceModule() {
        return driveM;
    }

    @Override
    public @NotNull UpgradeModule getUpgradeModule() {
        return upgradeM;
    }

    // ---- Rendering ----
    public float getRotationSpeed() {
        return ROTATION;
    }

    public void onMenuOpened() {
        viewers++;
    }

    public void onMenuClosed() {
        viewers = Math.max(0, viewers - 1);
        if (viewers == 0) {
            resetFilterAndScroll();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, QuantumFabricatorTile blockEntity) {
        blockEntity.prevRotation = blockEntity.rotation;

        float speed = (float) (ROTATION +
                (ROTATION * ((float) blockEntity.data.get(0) / blockEntity.data.get(1))));

        blockEntity.rotation += speed;

        if (blockEntity.rotation >= 360) {
            blockEntity.rotation -= 360;
            blockEntity.prevRotation -= 360;
        }
    }
}
