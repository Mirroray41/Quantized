package net.zapp.quantized.core.utils.module;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.function.IntConsumer;
import java.util.function.Predicate;

public class TankModule implements Module {
    private final FluidTank tank;
    private final String moduleOwner;

    public TankModule(String moduleOwner, int capacity, Predicate<FluidStack> validator, IntConsumer onChange) {
        this.moduleOwner = moduleOwner;
        IntConsumer change =  onChange != null ? onChange : s -> {};
        Predicate<FluidStack> v = validator != null ? validator : fs -> true;

        this.tank = new FluidTank(capacity, v) {
            @Override
            protected void onContentsChanged() {
                change.accept(0);
            }
        };
     }

     public FluidTank getHandler() {
        return tank;
     }

    @Override
    public void save(CompoundTag out, HolderLookup.Provider registries) {
        // writeToNBT only writes the "Fluid" sub-tag when the tank is non-empty.
        tank.writeToNBT(registries, out);
        out.putInt(moduleOwner + ".fluid_capacity", tank.getCapacity());
    }

    @Override
    public void load(CompoundTag in, HolderLookup.Provider registries) {
        tank.readFromNBT(registries, in);
        int capacity = in.getInt(moduleOwner + ".fluid_capacity");
        if (capacity > 0) tank.setCapacity(capacity);
    }

    public boolean canPay(int amount) {
        return tank.drain(amount, IFluidHandler.FluidAction.SIMULATE).getAmount() == amount;
    }

    public void drainFluid(int toDrain) {
        tank.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
    }
}
