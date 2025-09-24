package net.zapp.quantized.block.custom;

import net.neoforged.neoforge.energy.IEnergyStorage;

public interface ICustomEnergyStorage extends IEnergyStorage {
    int getEnergy();
    void setEnergy(int energy);

    int getCapacity();
    void setCapacity(int capacity);
}
