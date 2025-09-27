package net.zapp.quantized.blocks.quantum_destabilizer;

import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.api.fluid.FluidTankRenderState;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuantumDestabilizerScreen extends AbstractContainerScreen<QuantumDestabilizerMenu> {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Quantized.MOD_ID,"textures/gui/quantum_destabilizer/quantum_destabilizer_screen.png");
    private static final ResourceLocation SPIRAL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Quantized.MOD_ID,"textures/gui/quantum_destabilizer/spiral_progress.png");
    private static final ResourceLocation ENERGY_BAR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Quantized.MOD_ID,"textures/gui/energy_bar.png");
    private static final ResourceLocation FLUID_BAR_OVERLAY_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Quantized.MOD_ID,"textures/gui/fluid_bar_overlay.png");

    public QuantumDestabilizerScreen(QuantumDestabilizerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        renderProgressArrow(guiGraphics, x, y);
        renderEnergyBar(guiGraphics, x, y);
        renderFluidTank(guiGraphics, x, y);
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if(menu.isCrafting()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SPIRAL_TEXTURE,x + 82, y + 35, 0, 0, menu.getScaledArrowProgress(), 16, 24, 16);
        }
    }

    private void renderEnergyBar(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ENERGY_BAR_TEXTURE,x + 10, y + 16 + 54 - menu.getScaledEnergyBar(), 0, 54 - menu.getScaledEnergyBar(), 12, menu.getScaledEnergyBar(), 12, 54);
    }

    private void renderFluidTank(GuiGraphics guiGraphics, int x, int y) {
        renderFluidMeterContent(guiGraphics, menu.getFluid(), menu.getFluidCapacity(), x + 29, y + 17, 10, 52);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, FLUID_BAR_OVERLAY_TEXTURE, x + 28, y + 16, 0, 0, 12, 54, 12, 54);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    protected void renderFluidMeterContent(GuiGraphics guiGraphics, FluidStack fluidStack, int tankCapacity, int x, int y,
                                           int w, int h) {
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

                GpuTextureView gpuTextureView = this.minecraft.getTextureManager().getTexture(stillFluidSprite.atlasLocation()).getTextureView();
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

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        if (isHovering(10, 16, 12, 54, mouseX, mouseY)) {
            List<Component> components = new ArrayList<>(2);
            components.add(Component.translatable("tooltip.quantized.machine_block.energy_stored", menu.getEnergyCapacity(), menu.getEnergyStored()));
            components.add(Component.translatable("tooltip.quantized.machine_block.energy_usage", menu.getCurrentEnergyConsumption(), menu.getEnergyConsumption()));

            guiGraphics.setTooltipForNextFrame(font, components, Optional.empty(), mouseX, mouseY);
        } else if (isHovering(28, 16, 12, 54, mouseX, mouseY)) {
            List<Component> components = new ArrayList<>(2);
            components.add(menu.getFluid().getHoverName());
            components.add(Component.translatable("tooltip.quantized.machine_block.fluid_stored", menu.getFluid().getAmount(), menu.getFluidCapacity()));

            guiGraphics.setTooltipForNextFrame(font, components, Optional.empty(), mouseX, mouseY);
        }
    }
}
