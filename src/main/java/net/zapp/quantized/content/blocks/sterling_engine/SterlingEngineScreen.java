package net.zapp.quantized.content.blocks.sterling_engine;

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

public class SterlingEngineScreen extends AbstractContainerScreen<SterlingEngineMenu> {
    private static final ResourceLocation GUI_TEXTURE = Quantized.id("textures/gui/sterling_engine/sterling_engine_screen.png");
    private static final ResourceLocation ENERGY_BAR_TEXTURE = Quantized.id("textures/gui/energy_bar.png");

    private static final ResourceLocation FIRE_TEXTURE = Quantized.id("textures/gui/sterling_engine/fire.png");
    private static final ResourceLocation TEMPERATURE_GAGE = Quantized.id("textures/gui/sterling_engine/temp_progress.png");


    public SterlingEngineScreen(SterlingEngineMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        renderEnergyBar(guiGraphics, x, y);
        renderFire(guiGraphics, x, y);
    }

    private void renderFire(GuiGraphics guiGraphics, int x, int y) {
        if(menu.isWorking()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, FIRE_TEXTURE, x + 80, y + 26, 0, 0, 14, 14, 14, 14);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEMPERATURE_GAGE,x + 155, y + 17 + 52 - menu.getScaledFire(), 0, 52 - menu.getScaledFire(), 10, menu.getScaledFire(), 10, 52);
        }
    }


    private void renderEnergyBar(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ENERGY_BAR_TEXTURE,x + 10, y + 16 + 54 - menu.getScaledEnergyBar(), 0, 54 - menu.getScaledEnergyBar(), 12, menu.getScaledEnergyBar(), 12, 54);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, (imageWidth / 2) - (getTextLen(title.getString()) / 2), titleLabelY, 0xFF5e6469, false);
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFF5e6469, false);
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
            components.add(Component.translatable("tooltip.quantized.battery.energy_stored", menu.getEnergyStored(), menu.getEnergyCapacity()));
            components.add(Component.translatable("tooltip.quantized.battery.energy_production", menu.getPowerProduction()));

            guiGraphics.setTooltipForNextFrame(font, components, Optional.empty(), mouseX, mouseY);
        }
        // TODO: CHANGE THIS TO LOCATION OF FIRE SPRITE
        else if (isHovering(154, 16, 12, 54, mouseX, mouseY)) {
            List<Component> components = new ArrayList<>(2);
            components.add(Component.translatable("tooltip.quantized.burning.info", menu.getBurnTime(), menu.getMaxBurnTime()));

            guiGraphics.setTooltipForNextFrame(font, components, Optional.empty(), mouseX, mouseY);
        }
    }
}
