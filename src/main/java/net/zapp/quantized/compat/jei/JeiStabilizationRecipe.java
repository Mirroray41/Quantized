package net.zapp.quantized.compat.jei;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public record JeiStabilizationRecipe(FluidStack flux, int energyCostFE, ItemStack output, float chance) {
    public static JeiStabilizationRecipe of(FluidStack flux, int energyCostFE, ItemStack output, float chance) {
        return new JeiStabilizationRecipe(flux.copy(), energyCostFE, output.copy(), chance);
    }
    public int chancePercent() { return Math.round(chance * 100.0f); }
}
