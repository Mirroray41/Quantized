package net.zapp.quantized.content.blocks.quantum_stabilizer;

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
import net.zapp.quantized.client.render.FluidTankRenderState;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuantumStabilizerScreen extends AbstractContainerScreen<QuantumStabilizerMenu> {
    // TODO: CHANGE THIS WHEN TEXTURES ARE MADE
    public static final ResourceLocation GUI_TEXTURE = Quantized.id("textures/gui/quantum_stabilizer/quantum_stabilizer_screen.png");
    public static final ResourceLocation PROGRESS_SPRITE = Quantized.id("textures/gui/quantum_stabilizer/spiral_progress.png");

    public static final ResourceLocation ENERGY_BAR_TEXTURE = Quantized.id("textures/gui/energy_bar.png");
    public static final ResourceLocation FLUID_BAR_OVERLAY_TEXTURE = Quantized.id("textures/gui/fluid_bar_overlay.png");

    public QuantumStabilizerScreen(QuantumStabilizerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        renderProgressArrow(guiGraphics, x, y);
        renderEnergyBar(guiGraphics, x, y);
        renderFluidTank(guiGraphics, x, y);

    }


    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if(menu.isCrafting()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, PROGRESS_SPRITE, x + 88 - menu.getScaledArrowProgress(), y + 42 - menu.getScaledArrowProgress(), 24 - menu.getScaledArrowProgress(), 24 - menu.getScaledArrowProgress(), menu.getScaledArrowProgress() * 2, menu.getScaledArrowProgress() * 2, 48, 48);
        }
    }


    private void renderEnergyBar(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ENERGY_BAR_TEXTURE,x + 10, y + 16 + 54 - menu.getScaledEnergyBar(), 0, 54 - menu.getScaledEnergyBar(), 12, menu.getScaledEnergyBar(), 12, 54);
    }

    private void renderFluidTank(GuiGraphics guiGraphics, int x, int y) {
        renderFluidMeterContent(guiGraphics, menu.getFluid(), menu.getFluidCapacity(), x + 155, y + 17, 10, 52);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, FLUID_BAR_OVERLAY_TEXTURE, x + 154, y + 16, 0, 0, 12, 54, 12, 54);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, (imageWidth / 2) - (getTextLen(this.title.getString()) / 2), this.titleLabelY, 0xFF5e6469, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFF5e6469, false);
    }

    protected int getTextLen(String text) {
        int out = 0;
        for (int i = 0 ; i < text.length() ; i++) {
            switch (text.charAt(i)) {
                case 'I', 'k', ' ', 'f': out+=5; break;
                case 't': out+=4; break;
                case 'l': out+=3; break;
                case 'i': out+=2; break;
                default: out+=6; break;
            }
        }
        return out;
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
            components.add(Component.translatable("tooltip.quantized.battery.energy_stored", menu.getEnergyStored(), menu.getEnergyCapacity()));
            components.add(Component.translatable("tooltip.quantized.battery.energy_usage", menu.getEnergyConsumption()));

            guiGraphics.setTooltipForNextFrame(font, components, Optional.empty(), mouseX, mouseY);
        } else if (isHovering(154, 16, 12, 54, mouseX, mouseY)) {
            List<Component> components = new ArrayList<>(2);
            components.add(menu.getFluid().getHoverName());
            components.add(Component.translatable("tooltip.quantized.tank.fluid_stored", menu.getFluid().getAmount(), menu.getFluidCapacity()));
            components.add(Component.translatable("tooltip.quantized.tank.fluid_consumption", menu.getFluxConsumption()));

            guiGraphics.setTooltipForNextFrame(font, components, Optional.empty(), mouseX, mouseY);
        }
    }
}
