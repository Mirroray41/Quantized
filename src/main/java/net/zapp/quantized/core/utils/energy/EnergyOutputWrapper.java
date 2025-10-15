package net.zapp.quantized.core.utils.energy;

import com.google.common.base.Preconditions;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class EnergyOutputWrapper implements IEnergyStorage {
    private final IEnergyStorage compose;

    public EnergyOutputWrapper(IEnergyStorage compose) {
        Preconditions.checkArgument(compose != null && compose.canExtract(), "This IEnergyStorage must be able to extract to be wrapped in an EnergyOutputWrapper");
        this.compose = compose;
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        return compose.extractEnergy(toExtract, simulate);
    }

    @Override
    public int getEnergyStored() {
        return compose.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return compose.getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return false;
    }
}
