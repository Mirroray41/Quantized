package net.zapp.quantized.compat.jei;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.blaze3d.textures.GpuTextureView;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.client.render.FluidTankRenderState;
import net.zapp.quantized.content.blocks.quantum_stabilizer.QuantumStabilizerScreen;
import net.zapp.quantized.core.init.ModBlocks;
import net.zapp.quantized.core.init.ModItems;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StabilizationCategory implements IRecipeCategory<JeiStabilizationRecipe> {
    public static final ResourceLocation UID = Quantized.id("qbit_crafting");
    public static final ResourceLocation RECIPE_TEXTURE = Quantized.id("textures/gui/quantum_stabilizer/stabilization_recipe.png");
    public static final IRecipeType<JeiStabilizationRecipe> TYPE = IRecipeType.create(UID, JeiStabilizationRecipe.class);

    // simple bar dimensions for FE draw
    private static final int FE_BAR_X = 50, FE_BAR_Y = 40, FE_BAR_W = 70, FE_BAR_H = 8;
    // pick whatever you like as a “max” to scale the bar; only visual
    private static final int FE_BAR_MAX = 1_000_000;

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;
    private final IIngredientTypeWithSubtypes<Fluid, FluidStack> fluidType;

    private int x;
    private int y;


    public StabilizationCategory(IGuiHelper guiHelper, IPlatformFluidHelper<FluidStack> platformFluidHelper) {
        this.background = guiHelper.createBlankDrawable(156, 54);
        ItemStack machineStack = new ItemStack(ModBlocks.QUANTUM_STABILIZER.asItem());
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, machineStack);

        this.title = Component.translatable("jei.quantized.qbit_crafting.title");
        this.fluidType = platformFluidHelper.getFluidIngredientType();
    }

    @Override
    public IRecipeType<JeiStabilizationRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 156;
    }

    @Override
    public int getHeight() {
        return 54;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, JeiStabilizationRecipe recipe, IFocusGroup focuses) {
        if (recipe.output().getItem() == ModItems.Q_BIT.get()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 18)
                    .add(recipe.output())
                    .addRichTooltipCallback((view, tooltip) -> {
                        tooltip.add(Component.literal(recipe.chancePercent() + "% ").withStyle(ChatFormatting.GOLD)
                                .append(Component.literal("Chance").withStyle(ChatFormatting.GRAY)));
                    });
        } else {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 79, 18)
                    .add(recipe.output())
                    .addRichTooltipCallback((view, tooltip) -> {
                        tooltip.add(Component.literal(recipe.chancePercent() + "% ").withStyle(ChatFormatting.GOLD)
                                .append(Component.literal("Chance").withStyle(ChatFormatting.GRAY)));
                    });
        }

    }

    @Override
    public void draw(JeiStabilizationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;

        x = ( Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 ) - ( getWidth() / 2 );
        y = (int) (( Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2 ) + ( getHeight() / 2 ) - ( Minecraft.getInstance().getWindow().getGuiScaledHeight() * 0.4) + 27);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RECIPE_TEXTURE, 0, 0, 0, 0, getWidth(), getHeight(), getWidth(), getHeight());

        renderEnergyBar(guiGraphics, 0, -54, recipe.energyCostFE());
        renderFluidTank(guiGraphics, 0, 0, recipe.flux());

        renderTooltip(guiGraphics, (int) mouseX, (int) mouseY, recipe.energyCostFE(), recipe.flux());

        // Chance text under output
        guiGraphics.drawCenteredString(font, Component.literal(recipe.chancePercent() + "%"), 78, 38, Color.MAGENTA.getRGB());
    }

    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, int fluxAmount, FluidStack fluid) {
        Font font = Minecraft.getInstance().font;
        if (isHovering(0, 0, 12, 54, mouseX, mouseY)) {
            List<Component> components = new ArrayList<>(2);
            components.add(Component.translatable("jei.tooltip.quantized.battery.energy_cost", fluxAmount));
            guiGraphics.setTooltipForNextFrame(font, components, Optional.empty(), x + mouseX, y + mouseY);
        } else if (isHovering(144, 0, 12, 54, mouseX, mouseY)) {
            List<Component> components = new ArrayList<>(2);
            components.add(fluid.getHoverName());
            components.add(Component.translatable("jei.tooltip.quantized.tank.fluid_cost", fluid.getAmount()));
            guiGraphics.setTooltipForNextFrame(font, components, Optional.empty(), x + mouseX, y + mouseY);
        }
    }

    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return mouseX >= x - 1 && mouseX < x + width + 1 && mouseY >= y - 1 && mouseY < y + height + 1;
    }

    private void renderEnergyBar(GuiGraphics guiGraphics, int x, int y, int feCost) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, QuantumStabilizerScreen.ENERGY_BAR_TEXTURE,x, y + 54, 0, 54, 12, 54, 12, 54);
    }

    private void renderFluidTank(GuiGraphics guiGraphics, int x, int y, FluidStack stack) {
        renderFluidMeterContent(guiGraphics, stack, stack.getAmount(), x + 145, y + 1, 10, 52);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, QuantumStabilizerScreen.FLUID_BAR_OVERLAY_TEXTURE, x + 144, y, 0, 0, 12, 54, 12, 54);
    }

    protected void renderFluidMeterContent(GuiGraphics guiGraphics, FluidStack fluidStack, int tankCapacity, int x, int y, int w, int h) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x, y);
        renderFluidStack(guiGraphics, fluidStack, tankCapacity, w, h);
        guiGraphics.pose().popMatrix();
    }

    private void renderFluidStack(GuiGraphics guiGraphics, FluidStack fluidStack, int tankCapacity, int w, int h) {
        if (fluidStack.isEmpty())
            return;

        Fluid fluid = fluidStack.getFluid();
        IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluid);
        ResourceLocation stillFluidImageId = fluidTypeExtensions.getStillTexture(fluidStack);
        TextureAtlasSprite stillFluidSprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).
                apply(stillFluidImageId);

        int fluidColorTint = fluidTypeExtensions.getTintColor(fluidStack);

        int fluidMeterPos = tankCapacity == -1 || (fluidStack.getAmount() > 0 && fluidStack.getAmount() == tankCapacity) ?
                0 : (h - ((fluidStack.getAmount() <= 0 || tankCapacity == 0) ? 0 :
                (Math.min(fluidStack.getAmount(), tankCapacity - 1) * h / tankCapacity + 1)));

        for (int yOffset = h; yOffset > fluidMeterPos; yOffset -= 16) {
            for (int xOffset = 0; xOffset < w; xOffset += 16) {
                int width = Math.min(w - xOffset, 16);
                int height = Math.min(yOffset - fluidMeterPos, 16);

                float u0 = stillFluidSprite.getU0();
                float u1 = stillFluidSprite.getU1();
                float v0 = stillFluidSprite.getV0();
                float v1 = stillFluidSprite.getV1();
                u1 = u1 - ((16 - width) / 16.f * (u1 - u0));
                v0 = v0 - ((16 - height) / 16.f * (v0 - v1));

                GpuTextureView gpuTextureView = Minecraft.getInstance().getTextureManager().getTexture(stillFluidSprite.atlasLocation()).getTextureView();
                guiGraphics.guiRenderState.submitGuiElement(new FluidTankRenderState(
                        RenderPipelines.GUI_TEXTURED, TextureSetup.singleTexture(gpuTextureView),
                        new Matrix3x2f(guiGraphics.pose()),
                        xOffset, yOffset, width, height,
                        u0, u1, v0, v1, fluidColorTint,
                        guiGraphics.scissorStack.peek()
                ));
            }
        }
    }

}
