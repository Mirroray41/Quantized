package net.zapp.quantized.content.blocks.quantum_stabilizer;

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
import net.neoforged.neoforge.items.ItemStackHandler;
import net.zapp.quantized.core.init.ModBlockEntities;
import net.zapp.quantized.core.init.ModFluids;
import net.zapp.quantized.core.init.ModItems;
import net.zapp.quantized.core.init.ModSounds;
import net.zapp.quantized.core.utils.module.EnergyModule;
import net.zapp.quantized.core.utils.module.ItemModule;
import net.zapp.quantized.core.utils.module.TankModule;
import net.zapp.quantized.core.utils.module.identifiers.HasEnergyModule;
import net.zapp.quantized.core.utils.module.identifiers.HasItemModule;
import net.zapp.quantized.core.utils.module.identifiers.HasTankModule;
import net.zapp.quantized.core.utils.random.RandomUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuantumStabilizerTile extends BlockEntity implements MenuProvider, HasEnergyModule, HasItemModule, HasTankModule {
    private static final int BIT_OUT_SLOT = 0;
    private static final int BYTE_OUT_SLOT = 1;

    private static final int FE_CAPACITY = 1_000_000;
    private static final int TANK_CAPACITY = 16_000;

    private final String ownerName = "QuantumStabilizerTile";
    private final ItemModule itemM = new ItemModule(ownerName, new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirtyAndUpdate();
        }
    });

    private final EnergyModule energyM = new EnergyModule(ownerName, FE_CAPACITY, Integer.MAX_VALUE, true, true);
    private final TankModule tankM = new TankModule(ownerName, TANK_CAPACITY, fs -> fs.getFluidType() == ModFluids.QUANTUM_FLUX.get().getFluidType(), i -> markDirtyAndUpdate());

    public QuantumStabilizerTile(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.QUANTUM_STABILIZER_TILE.get(), pos, blockState);
    }

    private static final int DEFAULT_POWER_CONSUME = 16;
    private static final int DEFAULT_FLUX_CONSUME = 16;

    private int progress = 0;
    private int maxProgress = 20;
    private int powerConsumption = DEFAULT_POWER_CONSUME;
    private int fluxConsumption = DEFAULT_FLUX_CONSUME;
    private boolean wasWorking = false;

    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                case 2 -> powerConsumption;
                case 3 -> fluxConsumption;
                case 4 -> energyM.getHandler().getEnergy();
                case 5 -> energyM.getHandler().getMaxEnergyStored();
                case 6 -> tankM.getHandler().getCapacity();
                default ->  0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
                case 2 -> powerConsumption = value;
                case 3 -> fluxConsumption = value;
            }
        }

        @Override
        public int getCount() {
            return 7;
        }
    };

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.quantized.tile.quantum_stabilizer");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new QuantumStabilizerMenu(id, inv, this, this.data);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        boolean canPay = energyM.canPay(powerConsumption) && tankM.canPay(fluxConsumption);
        boolean canOut = itemM.canOutput(BIT_OUT_SLOT, 1, ModItems.Q_BIT.get())
                && itemM.canOutput(BYTE_OUT_SLOT, 1, ModItems.Q_BYTE.get());
        boolean hasInput = tankM.getHandler().getFluidAmount() > fluxConsumption;
        boolean working = canPay && canOut && hasInput;

        setWorking(level, pos, state, working);
        // We don't wanna be mean.
        if (!working) {
            powerConsumption = 0;
            fluxConsumption = 0;
            return;
        }

        powerConsumption = DEFAULT_POWER_CONSUME;
        fluxConsumption = DEFAULT_FLUX_CONSUME;


        progress++;
        energyM.extractPower(powerConsumption);
        tankM.drainFluid(fluxConsumption);

        level.playSound(null, pos, ModSounds.QUANTUM_STABILIZER_WORK.value(), SoundSource.BLOCKS, 1f, 1f + (float) progress / maxProgress);

        // This machine is not perfect, sometimes you get nothing, other times you get more than you asked for.
        if (progress >= maxProgress) {
            progress = 0;
            if (RandomUtils.percentChance(75)) {
                if (RandomUtils.percentChance(5) && itemM.canOutput(BYTE_OUT_SLOT, 1, ModItems.Q_BYTE.get())) {
                    itemM.getHandler().insertItem(BYTE_OUT_SLOT, new ItemStack(ModItems.Q_BYTE.get(), 1), false);
                    return;
                }
                itemM.getHandler().insertItem(BIT_OUT_SLOT, new ItemStack(ModItems.Q_BIT.get(), 1), false);
            }
        }
    }

    private void setWorking(Level level, BlockPos pos, BlockState state, boolean working) {
        if (wasWorking != working) {
            wasWorking = working;
            BlockState ns = state.setValue(QuantumStabilizer.ON, working);
            setChanged(level, pos, ns);
            level.setBlock(pos, ns, 3);
        }
    }

    private void drops() {
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


    @Override
    protected void saveAdditional(ValueOutput out) {
        HolderLookup.Provider regs = level != null ? level.registryAccess() : null;

        itemM.save(out, regs);
        energyM.save(out, regs);
        tankM.save(out, regs);

        out.putInt("progress", progress);
        out.putInt("maxProgress", maxProgress);

        super.saveAdditional(out);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        HolderLookup.Provider regs = level != null ? level.registryAccess() : null;

        itemM.load(in, regs);
        energyM.load(in, regs);
        tankM.load(in, regs);

        progress = in.getIntOr("progress", 0);
        maxProgress = in.getIntOr("maxProgress", 20);
    }

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

}
