package net.zapp.quantized.core.utils.energy;

import net.minecraft.util.Mth;

public class CustomEnergyStorage implements ICustomEnergyStorage {
    private final boolean canReceive;
    private final boolean canExtract;

    protected int energy;
    protected int capacity;
    protected int maxReceive;
    protected int maxExtract;

    public CustomEnergyStorage(int capacity,int maxReceive, int maxExtract, boolean canReceive, boolean canExtract) {
        this.capacity = capacity;
        this.canReceive = canReceive;
        this.canExtract = canExtract;
        this.maxExtract = maxExtract;
        this.maxReceive = maxReceive;
        this.energy = 0;
    }

    public CustomEnergyStorage(CustomEnergyStorage storage) {
        this.capacity = storage.getCapacity();
        this.canReceive = storage.canReceive();
        this.canExtract = storage.canExtract();
        this.maxExtract = storage.getMaxReceive();
        this.maxReceive = storage.getMaxExtract();
        this.energy = 0;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (this.canReceive() && maxReceive > 0) {
            int energyReceived = Mth.clamp(this.capacity - this.energy, 0, Math.min(this.maxReceive, maxReceive));
            if (!simulate) {
                this.energy += energyReceived;
            }

            return energyReceived;
        } else {
            return 0;
        }
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (this.canExtract() && maxExtract > 0) {
            int energyExtracted = Math.min(this.energy, Math.min(this.maxExtract, maxExtract));
            if (!simulate) {
                this.energy -= energyExtracted;
            }

            return energyExtracted;
        } else {
            return 0;
        }
    }

    @Override
    public int getEnergyStored() {
        return this.energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return this.capacity;
    }

    @Override
    public boolean canExtract() {
        return this.canExtract;
    }

    @Override
    public boolean canReceive() {
        return this.canReceive;
    }

    @Override
    public int getEnergy() {
        return energy;
    }

    @Override
    public void setEnergy(int energy) {
        this.energy = energy;
    }


    @Override
    public int getCapacity() {
        return this.capacity;
    }

    @Override
    public void setCapacity(int capacity) {
         this.capacity = capacity;
    }

    public int getMaxReceive() {
        return this.maxReceive;
    }

    public void setMaxReceive(int maxReceive) {
        this.maxReceive = maxReceive;
    }

    public int getMaxExtract() {
        return this.maxExtract;
    }

    public void setMaxExtract(int maxExtract) {
        this.maxExtract = maxExtract;
    }
}