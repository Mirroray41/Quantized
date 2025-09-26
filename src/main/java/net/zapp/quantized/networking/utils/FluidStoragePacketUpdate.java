package net.zapp.quantized.networking.utils;

import net.neoforged.neoforge.fluids.FluidStack;

public interface FluidStoragePacketUpdate {
    void setFluid(int tank, FluidStack fluidStack);
    void setTankCapacity(int tank, int capacity);
}
