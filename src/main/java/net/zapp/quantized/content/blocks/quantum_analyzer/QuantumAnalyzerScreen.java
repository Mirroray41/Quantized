package net.zapp.quantized.content.blocks.quantum_analyzer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.zapp.quantized.Quantized;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuantumAnalyzerScreen extends AbstractContainerScreen<QuantumAnalyzerMenu> {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Quantized.MOD_ID,"textures/gui/quantum_analyzer/quantum_analyzer_screen.png");
    private static final ResourceLocation PROGRESS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Quantized.MOD_ID,"textures/gui/quantum_analyzer/progress.png");
    private static final ResourceLocation ENERGY_BAR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Quantized.MOD_ID,"textures/gui/energy_bar.png");
    private static final ResourceLocation SCROLL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Quantized.MOD_ID,"textures/gui/scroll.png");

    protected int imageHeight = 180;

    public QuantumAnalyzerScreen(QuantumAnalyzerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SCROLL_TEXTURE, x + 154, y + 31, 0, 0, 12, 15, 12, 15);


        renderProgressArrow(guiGraphics, x, y);
        renderEnergyBar(guiGraphics, x, y);
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if(menu.isCrafting()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, PROGRESS_TEXTURE,x + 31, y + 73, 0, 0, menu.getScaledArrowProgress(), 4, 24, 4);
        }
    }

    private void renderEnergyBar(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ENERGY_BAR_TEXTURE,x + 10, y + 23 + 54 - menu.getScaledEnergyBar(), 0, 54 - menu.getScaledEnergyBar(), 12, menu.getScaledEnergyBar(), 12, 54);
    }


    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, (imageWidth / 2) - (getTextLen(this.title.getString()) / 2), this.titleLabelY - 7, 0xFF5e6469, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY + 7, 0xFF5e6469, false);
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

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        if (isHovering(10, 16, 12, 54, mouseX, mouseY)) {
            List<Component> components = new ArrayList<>(2);
            components.add(Component.translatable("tooltip.quantized.machine_block.energy_stored", menu.getEnergyStored(), menu.getEnergyCapacity()));
            components.add(Component.translatable("tooltip.quantized.machine_block.energy_usage", menu.getEnergyConsumption()));

            guiGraphics.setTooltipForNextFrame(font, components, Optional.empty(), mouseX, mouseY);
        }
    }
}
