package net.zapp.quantized.content.blocks.flux_generator;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.client.render.ImageTextButton;
import net.zapp.quantized.core.init.ModFluids;
import net.zapp.quantized.core.utils.screen.ScreenUtils;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FluxGeneratorScreen extends AbstractContainerScreen<FluxGeneratorMenu> {
    private static final ResourceLocation GUI_TEXTURE = Quantized.id("textures/gui/flux_generator/flux_generator_screen.png");
    private static final ResourceLocation REACTOR_OVERLAY = Quantized.id("textures/gui/flux_generator/flux_reactor_overlay.png");
    private static final ResourceLocation REACTOR_OVERLAY_STABLE = Quantized.id("textures/gui/flux_generator/flux_reactor_overlay_stable.png");

    private static final ResourceLocation REACTOR_OVERLAY_UNSTABLE_1 = Quantized.id("textures/gui/flux_generator/flux_reactor_overlay_unstable_1.png");
    private static final ResourceLocation REACTOR_OVERLAY_UNSTABLE_2 = Quantized.id("textures/gui/flux_generator/flux_reactor_overlay_unstable_2.png");


    private static final ResourceLocation ENERGY_BAR_TEXTURE = Quantized.id("textures/gui/energy_bar.png");
    private static final ResourceLocation FLUID_BAR_OVERLAY_TEXTURE = Quantized.id("textures/gui/fluid_bar_overlay.png");

    private static final ResourceLocation UPGRADE_BUTTON = Quantized.id("textures/gui/upgrade_button.png");
    private static final ResourceLocation UPGRADE_BUTTON_PRESSED = Quantized.id("textures/gui/upgrade_button_pressed.png");

    public FluxGeneratorScreen(FluxGeneratorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        addRenderableWidget(new ImageTextButton(UPGRADE_BUTTON, UPGRADE_BUTTON_PRESSED, x + imageWidth + 4, y + 4, 18, 18,
                        p -> net.zapp.quantized.core.networking.ModMessages.sendToServer(
                                new net.zapp.quantized.core.networking.messages.OpenUpgradesC2S(menu.blockEntity.getBlockPos()))
                )
        );
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        renderWorkingSprite(guiGraphics, x, y);
        renderEnergyBar(guiGraphics, x, y);
        renderFluidTank(guiGraphics, x, y);
    }


    private void renderWorkingSprite(GuiGraphics guiGraphics, int x, int y) {
        if(menu.getFluid().is(ModFluids.QUANTUM_FLUX)) {
            FluidStack full = menu.getFluid().copy();
            full.setAmount(menu.getFluidCapacity());
            renderFluidMeterContent(guiGraphics, full, menu.getFluidCapacity(), x + 61, y + 16, 54, 54);
        }
        if(menu.isWorking()) {
            if (randInt(0, 3) < 2) {
                guiGraphics.blit(REACTOR_OVERLAY_UNSTABLE_1, x + 61, y + 16, 0, 0, 54, 54, 54, 54);
            } else {
                guiGraphics.blit(REACTOR_OVERLAY_UNSTABLE_2, x + 61, y + 16, 0, 0, 54, 54, 54, 54);
            }
        } else {
            guiGraphics.blit(REACTOR_OVERLAY_STABLE, x + 61, y + 16, 0, 0, 54, 54, 54, 54);
        }
    }

    private int randInt(int min, int max) {
        return min + (int)(Math.random() * ((max - min) + 1));
    }

    private void renderEnergyBar(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(ENERGY_BAR_TEXTURE,x + 10, y + 16 + 54 - menu.getScaledEnergyBar(), 0, 54 - menu.getScaledEnergyBar(), 12, menu.getScaledEnergyBar(), 12, 54);
    }

    private void renderFluidTank(GuiGraphics guiGraphics, int x, int y) {
        renderFluidMeterContent(guiGraphics, menu.getFluid(), menu.getFluidCapacity(), x + 155, y + 17, 10, 52);
        guiGraphics.blit(FLUID_BAR_OVERLAY_TEXTURE, x + 154, y + 16, 0, 0, 12, 54, 12, 54);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFF5e6469, false);
        ScreenUtils.drawCenteredString(guiGraphics, font, title, 0xFF5e6469, imageWidth / 2, titleLabelY, false);
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
        RenderSystem.enableBlend();
        guiGraphics.pose().pushPose();

        guiGraphics.pose().translate(x, y, 0);

        renderFluidStack(guiGraphics, fluidStack, tankCapacity, w, h);

        guiGraphics.pose().popPose();
        RenderSystem.setShaderColor(1.f, 1.f, 1.f, 1.f);
        RenderSystem.disableBlend();
    }

    private void renderFluidStack(GuiGraphics guiGraphics, FluidStack fluidStack, int tankCapacity, int w, int h) {
        if(fluidStack.isEmpty())
            return;

        Fluid fluid = fluidStack.getFluid();
        IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluid);
        ResourceLocation stillFluidImageId = fluidTypeExtensions.getStillTexture(fluidStack);
        if(stillFluidImageId == null)
            stillFluidImageId = ResourceLocation.withDefaultNamespace("air");
        TextureAtlasSprite stillFluidSprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).
                apply(stillFluidImageId);

        int fluidColorTint = fluidTypeExtensions.getTintColor(fluidStack);

        int fluidMeterPos = tankCapacity == -1 || (fluidStack.getAmount() > 0 && fluidStack.getAmount() == tankCapacity)?
                0:(h - ((fluidStack.getAmount() <= 0 || tankCapacity == 0)?0:
                (Math.min(fluidStack.getAmount(), tankCapacity - 1) * h / tankCapacity + 1)));

        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor((fluidColorTint >> 16 & 0xFF) / 255.f,
                (fluidColorTint >> 8 & 0xFF) / 255.f, (fluidColorTint & 0xFF) / 255.f,
                (fluidColorTint >> 24 & 0xFF) / 255.f);

        Matrix4f mat = guiGraphics.pose().last().pose();

        for(int yOffset = h;yOffset > fluidMeterPos;yOffset -= 16) {
            for(int xOffset = 0;xOffset < w;xOffset += 16) {
                int width = Math.min(w - xOffset, 16);
                int height = Math.min(yOffset - fluidMeterPos, 16);

                float u0 = stillFluidSprite.getU0();
                float u1 = stillFluidSprite.getU1();
                float v0 = stillFluidSprite.getV0();
                float v1 = stillFluidSprite.getV1();
                u1 = u1 - ((16 - width) / 16.f * (u1 - u0));
                v0 = v0 - ((16 - height) / 16.f * (v0 - v1));

                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                bufferBuilder.addVertex(mat, xOffset, yOffset, 0).setUv(u0, v1);
                bufferBuilder.addVertex(mat, xOffset + width, yOffset, 0).setUv(u1, v1);
                bufferBuilder.addVertex(mat, xOffset + width, yOffset - height, 0).setUv(u1, v0);
                bufferBuilder.addVertex(mat, xOffset, yOffset - height, 0).setUv(u0, v0);
                BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
            }
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        if (isHovering(10, 16, 12, 54, mouseX, mouseY)) {
            List<Component> components = new ArrayList<>(2);
            components.add(Component.translatable("tooltip.quantized.battery.energy_stored", menu.getEnergyStored(), menu.getEnergyCapacity()));
            components.add(Component.translatable("tooltip.quantized.battery.energy_production", menu.getPowerProduction()));

            guiGraphics.renderTooltip(font, components, Optional.empty(), mouseX, mouseY);
        } else if (isHovering(154, 16, 12, 54, mouseX, mouseY)) {
            List<Component> components = new ArrayList<>(2);
            components.add(menu.getFluid().getHoverName());
            components.add(Component.translatable("tooltip.quantized.tank.fluid_stored", menu.getFluid().getAmount(), menu.getFluidCapacity()));
            components.add(Component.translatable("tooltip.quantized.tank.fluid_consumption", menu.getFluxConsumption()));

            guiGraphics.renderTooltip(font, components, Optional.empty(), mouseX, mouseY);
        }
    }
}
