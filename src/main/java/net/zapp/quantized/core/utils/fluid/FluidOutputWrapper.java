package net.zapp.quantized.core.utils.fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class FluidOutputWrapper implements IFluidHandler {
    private final IFluidHandler compose;

    public FluidOutputWrapper(IFluidHandler compose) {
        this.compose = compose;
    }

    @Override
    public int getTanks() {
        return compose.getTanks();
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return compose.getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return compose.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return compose.isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return 0;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return compose.drain(resource, action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return compose.drain(maxDrain, action);
    }
}
