package net.zapp.quantized.core.utils.module.identifiers;

import net.zapp.quantized.core.utils.energy.CustomEnergyStorage;
import net.zapp.quantized.core.utils.module.EnergyModule;

import javax.annotation.Nonnull;

public interface HasEnergyModule {
    @Nonnull
    EnergyModule getEnergyModule();
    @Nonnull
    default CustomEnergyStorage getEnergyHandler() {
        return getEnergyModule().getHandler();
    }
}
