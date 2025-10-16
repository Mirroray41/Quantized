package net.zapp.quantized.core.utils.fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class FluidInputWrapper implements IFluidHandler {
    private final IFluidHandler compose;

    public FluidInputWrapper(IFluidHandler compose) {
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
        return compose.fill(resource, action);
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }
}
