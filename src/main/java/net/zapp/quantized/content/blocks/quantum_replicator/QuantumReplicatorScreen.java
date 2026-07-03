package net.zapp.quantized.content.blocks.quantum_replicator;

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
import net.zapp.quantized.core.utils.screen.ScreenUtils;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuantumReplicatorScreen extends AbstractContainerScreen<QuantumReplicatorMenu> {
    private static final ResourceLocation GUI_TEXTURE =
            Quantized.id("textures/gui/quantum_replicator/quantum_replicator_screen.png");
    private static final ResourceLocation CRAFTING_PROGRESS =
            Quantized.id("textures/gui/quantum_replicator/crafting_progress.png");
    private static final ResourceLocation ENERGY_BAR_TEXTURE =
            Quantized.id("textures/gui/energy_bar.png");
    private static final ResourceLocation FLUID_BAR_OVERLAY_TEXTURE =
            Quantized.id("textures/gui/fluid_bar_overlay.png");

    private static final ResourceLocation UPGRADE_BUTTON = Quantized.id("textures/gui/upgrade_button.png");
    private static final ResourceLocation UPGRADE_BUTTON_PRESSED = Quantized.id( "textures/gui/upgrade_button_pressed.png");

    public QuantumReplicatorScreen(QuantumReplicatorMenu menu, Inventory playerInventory, Component title) {
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

        renderProgressArrow(guiGraphics, x, y);
        renderEnergyBar(guiGraphics, x, y);
        renderFluidTank(guiGraphics, x, y);
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if(menu.isCrafting()) {
            guiGraphics.blit(CRAFTING_PROGRESS,x + 50 , y + 17, 0, 0, menu.getScaledArrowProgress(), 50, 77, 50);
        }
    }

    private void renderEnergyBar(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(ENERGY_BAR_TEXTURE,x + 10, y + 16 + 54 - menu.getScaledEnergyBar(), 0, 54 - menu.getScaledEnergyBar(), 12, menu.getScaledEnergyBar(), 12, 54);
    }

    private void renderFluidTank(GuiGraphics guiGraphics, int x, int y) {
        ScreenUtils.renderFluidMeterContent(guiGraphics, menu.getFluid(), menu.getFluidCapacity(), x + 155, y + 17, 10, 52);
        guiGraphics.blit(FLUID_BAR_OVERLAY_TEXTURE, x + 154, y + 16, 0, 0, 12, 54, 12, 54);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        if (isHovering(10, 16, 12, 54, mouseX, mouseY)) {
            List<Component> components = new ArrayList<>(2);
            components.add(Component.translatable("tooltip.quantized.battery.energy_stored", menu.getEnergyStored(), menu.getEnergyCapacity()));
            components.add(Component.translatable("tooltip.quantized.battery.energy_usage", menu.getEnergyConsumption()));
            guiGraphics.renderTooltip(font, components, Optional.empty(), mouseX, mouseY);
        } else if (isHovering(154, 16, 12, 54, mouseX, mouseY)) {
            List<Component> components = new ArrayList<>(2);
            components.add(menu.getFluid().getHoverName());
            components.add(Component.translatable("tooltip.quantized.tank.fluid_stored", menu.getFluid().getAmount(), menu.getFluidCapacity()));
            guiGraphics.renderTooltip(font, components, Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ScreenUtils.drawCenteredString(guiGraphics, font, title, 0xFF5e6469, imageWidth / 2, titleLabelY , false);
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFF5e6469, false);
    }
}
