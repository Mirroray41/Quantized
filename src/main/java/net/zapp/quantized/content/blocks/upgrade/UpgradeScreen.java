package net.zapp.quantized.content.blocks.upgrade;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.core.networking.ModMessages;
import net.zapp.quantized.core.networking.messages.OpenMachineMenuC2S;

/**
 * Standalone screen for a machine's upgrade slots. Uses a placeholder background reused from the
 * sterling engine GUI until dedicated textures exist; a back button returns to the machine's menu.
 */
public class UpgradeScreen extends AbstractContainerScreen<UpgradeMenu> {
    // Placeholder background - reuse an existing 176x166 machine GUI until a dedicated texture is made.
    private static final ResourceLocation GUI_TEXTURE =
            Quantized.id("textures/gui/sterling_engine/sterling_engine_screen.png");

    public UpgradeScreen(UpgradeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        addRenderableWidget(Button.builder(
                        Component.translatable("gui.quantized.upgrades.back"),
                        b -> ModMessages.sendToServer(new OpenMachineMenuC2S(menu.getMachinePos())))
                .bounds(x + imageWidth - 54, y + 4, 50, 16)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 0xFF5e6469, false);
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFF5e6469, false);
    }
}
