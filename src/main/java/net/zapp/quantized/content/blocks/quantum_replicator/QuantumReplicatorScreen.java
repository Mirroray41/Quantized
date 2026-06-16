package net.zapp.quantized.content.blocks.quantum_replicator;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.zapp.quantized.Quantized;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuantumReplicatorScreen extends AbstractContainerScreen<QuantumReplicatorMenu> {
    // Placeholder GUI texture (art TODO); renders as missing-texture until authored.
    private static final ResourceLocation GUI_TEXTURE =
            Quantized.id("textures/gui/quantum_replicator/quantum_replicator_screen.png");
    private static final ResourceLocation ENERGY_BAR_TEXTURE = Quantized.id("textures/gui/energy_bar.png");

    public QuantumReplicatorScreen(QuantumReplicatorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                        Component.translatable("gui.quantized.upgrades.open"),
                        b -> net.zapp.quantized.core.networking.ModMessages.sendToServer(
                                new net.zapp.quantized.core.networking.messages.OpenUpgradesC2S(menu.blockEntity.getBlockPos())))
                .bounds(leftPos + imageWidth + 4, topPos + 4, 18, 18)
                .tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("gui.quantized.upgrades")))
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        int bar = menu.getScaledEnergyBar();
        if (bar > 0) {
            guiGraphics.blit(ENERGY_BAR_TEXTURE, x + 10, y + 16 + 54 - bar, 0, 54 - bar, 12, bar, 12, 54);
        }
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
            components.add(Component.translatable("tooltip.quantized.battery.energy_usage", menu.getPowerConsumption()));
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
        guiGraphics.drawString(font, title, 8, titleLabelY, 0xFF5e6469, false);
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFF5e6469, false);
    }
}
