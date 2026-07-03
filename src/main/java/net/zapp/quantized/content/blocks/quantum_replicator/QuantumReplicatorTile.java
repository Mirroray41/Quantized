package net.zapp.quantized.content.blocks.quantum_replicator;

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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.zapp.quantized.content.blocks.ProcessingCurves;
import net.zapp.quantized.content.item.custom.drive_item.DriveItem;
import net.zapp.quantized.content.item.custom.drive_item.SingularityDriveItem;
import net.zapp.quantized.content.item.custom.upgrade.UpgradeType;
import net.zapp.quantized.core.fluxdata.FluxDataFixerUpper;
import net.zapp.quantized.core.init.ModBlockEntities;
import net.zapp.quantized.core.init.ModFluids;
import net.zapp.quantized.core.init.ModSounds;
import net.zapp.quantized.core.utils.DataFluxPair;
import net.zapp.quantized.core.utils.module.EnergyModule;
import net.zapp.quantized.core.utils.module.ItemModule;
import net.zapp.quantized.core.utils.module.TankModule;
import net.zapp.quantized.core.utils.module.UpgradeModule;
import net.zapp.quantized.core.utils.module.identifiers.HasEnergyModule;
import net.zapp.quantized.core.utils.module.identifiers.HasItemModule;
import net.zapp.quantized.core.utils.module.identifiers.HasTankModule;
import net.zapp.quantized.core.utils.module.identifiers.HasUpgradeModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class QuantumReplicatorTile extends BlockEntity implements MenuProvider, HasEnergyModule, HasItemModule, HasTankModule, HasUpgradeModule {
    private static final float ROTATION = 10f;

    public float prevRotation;
    public float rotation;

    public float prevScale;
    public float scale;

    // ---- Slots ----
    public static final int DISK_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;

    // ---- Energy/Fluids constants ----
    public static final int FE_CAPACITY = 1_000_000;
    public static final int TANK_CAPACITY = 8_000_000;

    private final String ownerName = "QuantumReplicatorTile";
    private final ItemModule itemM = new ItemModule(ownerName, new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirtyAndUpdate();
        }

        @Override
        @NotNull
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (slot == DISK_SLOT && !(stack.getItem() instanceof SingularityDriveItem)) return stack;
            return super.insertItem(slot, stack, simulate);
        }
    });
    private final EnergyModule energyM = new EnergyModule(ownerName, FE_CAPACITY, Integer.MAX_VALUE, true, true);
    private final TankModule tankM = new TankModule(ownerName, TANK_CAPACITY,
            fs -> fs.getFluidType() == ModFluids.QUANTUM_FLUX.get().getFluidType(), s -> markDirtyAndUpdate());
    private final UpgradeModule upgradeM = new UpgradeModule(ownerName,
            List.of(UpgradeType.SPEED, UpgradeType.EFFICIENCY), this::markDirtyAndUpdate);

    // ---- Menu sync data ----
    private int progress = 0;
    private int maxProgress = 72;
    private int powerConsumption = 0;
    private boolean wasWorking = false;

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
                default -> 0;
            };
        }

        @Override
        public void set(int i, int value) {
            switch (i) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
                case 2 -> powerConsumption = value;
            }
        }

        @Override
        public int getCount() {
            return 6;
        }
    };

    public QuantumReplicatorTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.QUANTUM_REPLICATOR_TILE.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.quantized.tile.quantum_replicator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new QuantumReplicatorMenu(id, inv, this, data);
    }

    // --- Tick ---
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;


        Item target = getImprintedItem();
        if (target == null) {
            resetCraft();
            setWorking(level, pos, state, false);
            return;
        }

        DataFluxPair df = FluxDataFixerUpper.getDataFlux(target);
        if (!DataFluxPair.isValid(df)) {
            resetCraft();
            setWorking(level, pos, state, false);
            return;
        }

        maxProgress = upgradeM.speedTicks(ProcessingCurves.timeTicks(df.data()));
        int toConsume = upgradeM.efficiencyCost(ProcessingCurves.powerPerTick(df.flux()));
        int fluxCost = df.flux();

        boolean canPay = energyM.canPay(toConsume) && tankM.canPay(fluxCost);
        boolean canOut = itemM.canOutput(OUTPUT_SLOT, 1, target);
        boolean working = canPay && canOut;

        setWorking(level, pos, state, working);
        if (!working) {
            powerConsumption = 0;
            return;
        }

        powerConsumption = toConsume;
        progress++;
        energyM.extractPower(powerConsumption);

        level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);

        level.playSound(null, pos, ModSounds.QUANTUM_REPLICATOR_WORK.value(),
                SoundSource.BLOCKS, 1f, 1f + (float) progress / (float) maxProgress);

        if (progress >= maxProgress) {
            tankM.drainFluid(fluxCost);
            itemM.getHandler().insertItem(OUTPUT_SLOT, new ItemStack(target, 1), false);
            progress = 0;
        }
    }

    /** The single item imprinted on the inserted singularity drive, or {@code null}. */
    public @Nullable Item getImprintedItem() {
        ItemStack disk = itemM.getHandler().getStackInSlot(DISK_SLOT);
        if (!(disk.getItem() instanceof SingularityDriveItem)) return null;
        List<Item> stored = DriveItem.getStoredItems(disk);
        return stored.isEmpty() ? null : stored.get(0);
    }

    // ---- helpers ----
    private void setWorking(Level level, BlockPos pos, BlockState state, boolean working) {
        if (wasWorking != working) {
            wasWorking = working;
            BlockState ns = state.setValue(QuantumReplicator.ON, working);
            setChanged(level, pos, ns);
            level.setBlock(pos, ns, 3);
        }
    }

    private void resetCraft() {
        progress = 0;
        powerConsumption = 0;
        maxProgress = 72;
    }

    // ---- Drop items when broken ----
    public void drops() {
        if (level == null) return;
        itemM.dropAll(level, worldPosition);
        upgradeM.dropAll(level, worldPosition);
    }

    // ---- Save / Load ----
    @Override
    protected void saveAdditional(CompoundTag out, HolderLookup.Provider registries) {
        itemM.save(out, registries);
        energyM.save(out, registries);
        tankM.save(out, registries);
        upgradeM.save(out, registries);

        out.putInt("progress", progress);
        out.putInt("maxProgress", maxProgress);

        super.saveAdditional(out, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag in, HolderLookup.Provider registries) {
        super.loadAdditional(in, registries);

        itemM.load(in, registries);
        energyM.load(in, registries);
        tankM.load(in, registries);
        upgradeM.load(in, registries);

        progress = in.getInt("progress");
        maxProgress = in.getInt("maxProgress");
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
    public @NotNull UpgradeModule getUpgradeModule() {
        return upgradeM;
    }

    public FluidStack getFluid() {
        return tankM.getHandler().getFluid();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, QuantumReplicatorTile blockEntity) {
        blockEntity.prevRotation = blockEntity.rotation;
        blockEntity.prevScale = blockEntity.scale;

        float speed = (float) (ROTATION +
                (ROTATION * ((float) blockEntity.data.get(0) / blockEntity.data.get(1))));

        blockEntity.rotation += speed;

        blockEntity.scale = (float) (0.5 * ((double) blockEntity.data.get(0) / blockEntity.data.get(1)));

        if (blockEntity.rotation >= 360) {
            blockEntity.rotation -= 360;
            blockEntity.prevRotation -= 360;
        }
    }
}
