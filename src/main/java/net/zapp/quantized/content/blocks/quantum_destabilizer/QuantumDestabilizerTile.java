package net.zapp.quantized.content.blocks.quantum_destabilizer;

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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.zapp.quantized.core.fluxdata.FluxDataFixerUpper;
import net.zapp.quantized.core.init.ModBlockEntities;
import net.zapp.quantized.core.init.ModFluids;
import net.zapp.quantized.core.init.ModSounds;
import net.zapp.quantized.core.utils.DataFluxPair;
import net.zapp.quantized.core.utils.module.EnergyModule;
import net.zapp.quantized.core.utils.module.ItemModule;
import net.zapp.quantized.core.utils.module.TankModule;
import net.zapp.quantized.core.utils.module.identifiers.HasEnergyModule;
import net.zapp.quantized.core.utils.module.identifiers.HasItemModule;
import net.zapp.quantized.core.utils.module.identifiers.HasTankModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuantumDestabilizerTile extends BlockEntity implements MenuProvider, HasEnergyModule, HasTankModule, HasItemModule {
    // ---- Rendering init ----
    private static final float ROTATION = 5f;

    // ---- Slots ----
    private static final int INPUT_SLOT = 0;

    // ---- Energy/Fluids constants ----
    public static final int FE_CAPACITY = 100_000;
    public static final int TANK_CAPACITY = 8_000;

    // ---- Modules (storage-only) ----
    private final String ownerName = "QuantumDestabilizerTile";
    private final ItemModule itemM = new ItemModule(ownerName, 1, slot -> markDirtyAndUpdate());
    private final EnergyModule energyM = new EnergyModule(ownerName, FE_CAPACITY, Integer.MAX_VALUE, Integer.MAX_VALUE, true, true);
    private final TankModule tankM = new TankModule(ownerName, TANK_CAPACITY, fs -> true, s -> markDirtyAndUpdate());

    // ---- Menu sync data ----
    private int progress = 0;
    private int maxProgress = 72;
    public int powerConsumption = 16;

    private boolean wasWorking = false;
    private FluidStack cachedOut = FluidStack.EMPTY;

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

    public QuantumDestabilizerTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.QUANTUM_DESTABILIZER_TILE.get(), pos, state);
    }

    // ---- UI / Menu ----
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.quantized.tile.name");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new QuantumDestabilizerMenu(id, inv, this, this.data);
    }

    // --- Tick ---
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        ItemStack in = itemM.getHandler().getStackInSlot(INPUT_SLOT);
        DataFluxPair df = FluxDataFixerUpper.getDataFluxFromStack(in);
        if (df == null || df.isZero()) {
            resetCraft();
            setWorking(level, pos, state, false);
            return;
        }

        maxProgress = ProcessingCurves.timeTicks(df.data());
        powerConsumption = ProcessingCurves.powerPerTick(df.flux());
        if (cachedOut.isEmpty() || cachedOut.getAmount() != df.flux()) {
            cachedOut = new FluidStack(ModFluids.QUANTUM_FLUX.get(), df.flux());
        }

        boolean canPay = energyM.getHandler().extractEnergy(powerConsumption, true) == powerConsumption;
        boolean canOut = tankM.getHandler().fill(cachedOut, IFluidHandler.FluidAction.SIMULATE) == cachedOut.getAmount();
        boolean hasInput = !in.isEmpty();
        boolean working = canPay && canOut && hasInput;

        setWorking(level, pos, state, working);
        if (!working) {
            if (progress > 0) progress = Math.max(0, progress - 1);
            return;
        }

        progress++;
        energyM.getHandler().extractEnergy(powerConsumption, false);

            level.playSound(null, pos, ModSounds.QUANTUM_DESTABILIZER_WORK.value(),
                    SoundSource.BLOCKS, 1f, 1f + (float) progress / (float) maxProgress);


        if (progress >= maxProgress) {
            if (tankM.getHandler().fill(cachedOut, IFluidHandler.FluidAction.SIMULATE) == cachedOut.getAmount()) {
                itemM.getHandler().extractItem(INPUT_SLOT, 1, false);
                tankM.getHandler().fill(cachedOut, IFluidHandler.FluidAction.EXECUTE);
            }
            progress = 0;
        }
    }

    // ---- helpers ----
    private void setWorking(Level level, BlockPos pos, BlockState state, boolean working) {
        if (wasWorking != working) {
            wasWorking = working;
            BlockState ns = state.setValue(QuantumDestabilizer.ON, working);
            setChanged(level, pos, ns);
            level.setBlock(pos, ns, 3);
        }
    }

    private void resetCraft() {
        progress = 0;
        powerConsumption = 0;
        maxProgress = 72;
        cachedOut = FluidStack.EMPTY;
    }


    // ---- Drop items when broken ----
    public void drops() {
        if (level == null) return;
        SimpleContainer inv = new SimpleContainer(itemM.getHandler().getSlots());
        for (int i = 0; i < itemM.getHandler().getSlots(); i++) {
            inv.setItem(i, itemM.getHandler().getStackInSlot(i));
        }
        Containers.dropContents(level, worldPosition, inv);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        drops();
        super.preRemoveSideEffects(pos, state);
    }

    // ---- Save / Load ----
    @Override
    protected void saveAdditional(ValueOutput out) {
        HolderLookup.Provider regs = level != null ? level.registryAccess() : null;

        // modules
        itemM.save(out, regs);
        energyM.save(out, regs);
        tankM.save(out, regs);

        // local fields
        out.putInt("progress", progress);
        out.putInt("maxProgress", maxProgress);

        super.saveAdditional(out);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        HolderLookup.Provider regs = level != null ? level.registryAccess() : null;

        // modules
        itemM.load(in, regs);
        energyM.load(in, regs);
        tankM.load(in, regs);

        // local fields
        progress = in.getIntOr("progress", 0);
        maxProgress = in.getIntOr("maxProgress", 72);
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

    // ---- Rendering ----
    public float getRotationSpeed() {
        return ROTATION;
    }
}
