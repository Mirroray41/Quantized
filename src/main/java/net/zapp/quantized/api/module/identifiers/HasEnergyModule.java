package net.zapp.quantized.api.module.identifiers;

import net.zapp.quantized.api.energy.CustomEnergyStorage;
import net.zapp.quantized.api.module.EnergyModule;

import javax.annotation.Nonnull;

public interface HasEnergyModule {
    @Nonnull
    EnergyModule getEnergyModule();
    @Nonnull
    default CustomEnergyStorage getEnergyHandler() {
        return getEnergyModule().getHandler();
    }
}
