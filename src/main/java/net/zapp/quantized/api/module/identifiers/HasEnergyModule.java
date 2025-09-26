package net.zapp.quantized.api.module.identifiers;

import net.neoforged.neoforge.energy.IEnergyStorage;
import net.zapp.quantized.api.module.EnergyModule;

import javax.annotation.Nonnull;

public interface HasEnergyModule {
    @Nonnull
    EnergyModule getEnergyModule();
    @Nonnull
    default IEnergyStorage getEnergyHandler() {
        return getEnergyModule().getHandler();
    }
}
