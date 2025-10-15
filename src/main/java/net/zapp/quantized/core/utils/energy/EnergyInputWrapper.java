package net.zapp.quantized.core.utils.energy;

import com.google.common.base.Preconditions;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class EnergyInputWrapper implements IEnergyStorage {
    private final IEnergyStorage compose;

    public EnergyInputWrapper(IEnergyStorage compose) {
        Preconditions.checkArgument(compose != null && compose.canReceive(), "This IEnergyStorage must be able to receive to be wrapped in an EnergyInputWrapper");
        this.compose = compose;
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        return compose.receiveEnergy(toReceive, simulate);
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        return 0;
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
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }
}
