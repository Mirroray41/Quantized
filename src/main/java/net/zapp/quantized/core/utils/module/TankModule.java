package net.zapp.quantized.core.utils.module;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    public void save(ValueOutput out, HolderLookup.Provider registries) {
        if (tank.getFluid().isEmpty()) return; // Avoid serializing nothing.
        out.store(moduleOwner + ".fluid", FluidStack.CODEC, tank.getFluid());
        out.putInt(moduleOwner + ".fluid_capacity", tank.getCapacity());
    }

    @Override
    public void load(ValueInput in, HolderLookup.Provider registries) {
        tank.setFluid(in.read(moduleOwner + ".fluid", FluidStack.CODEC).orElse(FluidStack.EMPTY));
        tank.setCapacity(in.getIntOr(moduleOwner + ".fluid_capacity", tank.getCapacity()));
    }

    public boolean canPay(int amount) {
        return tank.drain(amount, IFluidHandler.FluidAction.SIMULATE).getAmount() == amount;
    }

    public void drainFluid(int toDrain) {
        tank.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
    }
}
