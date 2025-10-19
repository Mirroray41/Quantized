package net.zapp.quantized.core.utils.module;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.zapp.quantized.core.utils.energy.CustomEnergyStorage;

public class EnergyModule implements Module {
    private final CustomEnergyStorage energy;
    private final String moduleOwner;

    public EnergyModule(String moduleOwner, int capacity, int transferRate, boolean canReceive, boolean canExtract) {
        this(moduleOwner, capacity, transferRate, transferRate, canReceive, canExtract);
    }

    public EnergyModule(String moduleOwner, int capacity, int maxReceive, int maxExtract, boolean canReceive, boolean canExtract) {
        this.moduleOwner = moduleOwner;
        this.energy = new CustomEnergyStorage(capacity, maxReceive, maxExtract, canReceive, canExtract);
    }

    public CustomEnergyStorage getHandler() { return energy; }

    @Override
    public void save(ValueOutput out, HolderLookup.Provider registries) {
        out.putInt(moduleOwner + ".energy", energy.getEnergyStored());
        out.putInt(moduleOwner + ".max_energy", energy.getMaxEnergyStored());
        out.putBoolean(moduleOwner + ".energy_can_receive", energy.canReceive());
        out.putBoolean(moduleOwner + ".energy_can_extract", energy.canExtract());
    }

    @Override
    public void load(ValueInput in, HolderLookup.Provider registries) {
        energy.setCapacity(in.getIntOr(moduleOwner + ".max_energy", energy.getMaxEnergyStored()));
        energy.setEnergy(in.getIntOr(moduleOwner + ".energy", 0));
    }

    public boolean canPay(int powerConsumption) {
        return energy.extractEnergy(powerConsumption, true) == powerConsumption;
    }

    public boolean canInsert(int toInsert) {
        return energy.getEnergyStored() + toInsert <= energy.getMaxEnergyStored();
    }

    public void extractPower(int toDrain) {
        energy.extractEnergy(toDrain, false);
    }

    public void pushEnergy(Level level, BlockPos pos) {
        for (Direction side : Direction.values()) {
            BlockPos nPos = pos.relative(side);
            BlockState nState = level.getBlockState(nPos);
            BlockEntity nBE = level.getBlockEntity(nPos);

            IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, nPos, nState, nBE, side.getOpposite());
            if (storage != null) {
                int received = storage.receiveEnergy(getHandler().getEnergy(), false);
                extractPower(received);
            }
        }
    }
}
