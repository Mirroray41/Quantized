package net.zapp.quantized.api.module;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zapp.quantized.api.energy.CustomEnergyStorage;

import java.util.function.IntConsumer;

public class EnergyModule implements Module {
    private final CustomEnergyStorage energy;
    private final String moduleOwner;

    public EnergyModule(String moduleOwner, int capacity, int maxReceive, int maxExtract, boolean canReceive, boolean canExtract, IntConsumer onChange) {
        this.moduleOwner = moduleOwner;
        IntConsumer change = onChange != null ? onChange : s -> {};
        this.energy = new CustomEnergyStorage(capacity, maxReceive, maxExtract, canReceive, canExtract);
    }

    public CustomEnergyStorage getEnergy() { return energy; }

    @Override
    public void save(ValueOutput out, HolderLookup.Provider registries) {
        out.putInt(moduleOwner + ".energy", energy.getEnergyStored());
        out.putInt(moduleOwner + ".max_energy", energy.getMaxEnergyStored());
        out.putBoolean(moduleOwner + ".energy_can_receive", energy.canReceive());
        out.putBoolean(moduleOwner + ".energy_can_extract", energy.canExtract());
    }

    @Override
    public void load(ValueInput in, HolderLookup.Provider registries) {
        energy.setCapacity(in.getIntOr(moduleOwner + ".energy", energy.getMaxEnergyStored()));
        energy.setEnergy(in.getIntOr(moduleOwner + ".energy", 0));
    }
}
