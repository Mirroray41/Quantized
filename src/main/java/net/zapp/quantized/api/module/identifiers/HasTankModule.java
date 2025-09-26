package net.zapp.quantized.api.module.identifiers;

import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.zapp.quantized.api.module.TankModule;

import javax.annotation.Nonnull;

public interface HasTankModule {
    @Nonnull
    TankModule getTankModule();
    @Nonnull
    default IFluidHandler getFluidHandler() {
        return getTankModule().getHandler();
    }
}
