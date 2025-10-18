package net.zapp.quantized.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.core.init.ModFluids;
import net.zapp.quantized.core.init.ModItems;

import java.util.List;

@JeiPlugin
public class JeiCompatPlugin implements IModPlugin {
    private static final ResourceLocation UID = Quantized.id("jei_compat");
    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IJeiHelpers helpers = registration.getJeiHelpers();
        IGuiHelper gui = helpers.getGuiHelper();

        IPlatformFluidHelper<FluidStack> fluidHelper = (IPlatformFluidHelper<FluidStack>) helpers.getPlatformFluidHelper();
        registration.addRecipeCategories(new StabilizationCategory(gui, fluidHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registerStabilizationRecipes(registration);
    }

    private void registerStabilizationRecipes(IRecipeRegistration registration) {
        FluidStack flux = new FluidStack(ModFluids.QUANTUM_FLUX.get(), 320);
        int feCost = 320;

        ItemStack qbit = new ItemStack(ModItems.Q_BIT.get());
        ItemStack qByte = new ItemStack(ModItems.Q_BYTE.get());
        float chance = 0.75f;

        List<JeiStabilizationRecipe> recipes = List.of(
                JeiStabilizationRecipe.of(flux, feCost, qbit, chance),
                JeiStabilizationRecipe.of(flux, feCost, qByte, chance * 0.05f)
                );
        registration.addRecipes(StabilizationCategory.TYPE, recipes);
    }
}
