package net.zapp.quantized.content.blocks.flux_generator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.zapp.quantized.core.init.ModBlockEntities;
import net.zapp.quantized.core.init.ModFluids;
import net.zapp.quantized.core.utils.module.EnergyModule;
import net.zapp.quantized.core.utils.module.TankModule;
import net.zapp.quantized.core.utils.module.identifiers.HasEnergyModule;
import net.zapp.quantized.core.utils.module.identifiers.HasTankModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluxGeneratorTile extends BlockEntity implements MenuProvider, HasEnergyModule, HasTankModule {
    private static final int FE_CAPACITY = 1_000_000;
    private static final int TANK_CAPACITY = 16_000;

    private final String ownerName = "FluxGeneratorTile";
    private final EnergyModule energyM = new EnergyModule(ownerName, FE_CAPACITY, Integer.MAX_VALUE, true, true);
    private final TankModule tankM = new TankModule(ownerName, TANK_CAPACITY, fs -> fs.getFluidType() == ModFluids.QUANTUM_FLUX.get().getFluidType(), i -> markDirtyAndUpdate());

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> fluxConsumption;
                case 1 -> powerProduction;
                case 2 -> isWorking ? 1 : 0;
                case 3 -> energyM.getHandler().getEnergy();
                case 4 -> energyM.getHandler().getMaxEnergyStored();
                case 5 -> tankM.getHandler().getCapacity();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> fluxConsumption = value;
                case 1 -> powerProduction = value;
            }
        }

        @Override
        public int getCount() {
            return 6;
        }
    };

    private static final int DEFAULT_FLUX_CONSUMPTION = 16;
    private static final int DEFAULT_POWER_PRODUCTION = 200;

    private boolean isWorking;
    private int fluxConsumption = DEFAULT_FLUX_CONSUMPTION;
    private int powerProduction = DEFAULT_POWER_PRODUCTION;
    private boolean wasWorking;


    public FluxGeneratorTile(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.FLUX_GENERATOR_TILE.get(), pos, blockState);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.quantized.tile.flux_generator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new FluxGeneratorMenu(id, inv, this, this.data);
    }


    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        pushEnergy(level, pos);

        boolean canPay = tankM.canPay(fluxConsumption);
        boolean canOutput = energyM.canInsert(powerProduction);
        boolean canWork = canPay && canOutput;

        setWorking(level, pos, state, canWork);

        if (!canWork) {
            fluxConsumption = 0;
            return;
        }
        fluxConsumption = DEFAULT_FLUX_CONSUMPTION;

        energyM.getHandler().receiveEnergy(powerProduction, false);
        tankM.drainFluid(fluxConsumption);
        // level.playSound(null, pos, PRODUCTION_SOUND, SoundSource.BLOCKS, 1f, 1f);
    }

    private void pushEnergy(Level level, BlockPos pos) {
        for (Direction side : Direction.values()) {
            BlockPos nPos = pos.relative(side);
            BlockState nState = level.getBlockState(nPos);
            BlockEntity nBE = level.getBlockEntity(nPos);

            IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, nPos, nState, nBE, side.getOpposite());
            if (storage != null) {
                int received = storage.receiveEnergy(energyM.getHandler().getEnergy(), false);
                energyM.extractPower(received);
            }
        }
    }

    private void setWorking(Level level, BlockPos pos, BlockState state, boolean working) {
        if (wasWorking != working) {
            wasWorking = working;
            isWorking = working;
            BlockState ns = state.setValue(FluxGenerator.ON, working);
            setChanged(level, pos, state);
            level.setBlock(pos, ns, 3);
        }
    }


    @Override
    protected void saveAdditional(ValueOutput out) {
        HolderLookup.Provider regs = level != null ? level.registryAccess() : null;

        energyM.save(out, regs);
        tankM.save(out, regs);

        super.saveAdditional(out);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        HolderLookup.Provider regs = level != null ? level.registryAccess() : null;

        energyM.load(in, regs);
        tankM.load(in, regs);
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
    public @NotNull TankModule getTankModule() {
        return tankM;
    }
}
