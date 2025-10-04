package net.zapp.quantized.core.utils.energy;

import net.neoforged.neoforge.energy.IEnergyStorage;

public interface ICustomEnergyStorage extends IEnergyStorage {
    int getEnergy();
    void setEnergy(int energy);

    int getCapacity();
    void setCapacity(int capacity);
}
