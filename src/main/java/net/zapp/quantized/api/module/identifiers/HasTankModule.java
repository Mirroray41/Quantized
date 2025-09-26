package net.zapp.quantized.api.module.identifiers;

import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.zapp.quantized.api.module.TankModule;

import javax.annotation.Nonnull;

public interface HasTankModule {
    @Nonnull
    TankModule getTankModule();
    @Nonnull
    default FluidTank getFluidHandler() {
        return getTankModule().getHandler();
    }
}
