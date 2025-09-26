package net.zapp.quantized.api.fluid;

import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.zapp.quantized.networking.utils.FluidStoragePacketUpdate;

public interface IFluidTIle<F extends IFluidHandler> extends FluidStoragePacketUpdate {
}
