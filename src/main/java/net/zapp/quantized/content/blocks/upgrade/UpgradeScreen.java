package net.zapp.quantized.content.blocks.upgrade;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.client.render.ImageTextButton;
import net.zapp.quantized.content.item.custom.upgrade.UpgradeType;
import net.zapp.quantized.core.networking.ModMessages;
import net.zapp.quantized.core.networking.messages.OpenMachineMenuC2S;
import net.zapp.quantized.core.utils.module.identifiers.HasUpgradeModule;
import net.zapp.quantized.core.utils.screen.ScreenUtils;

import java.util.List;

public class UpgradeScreen extends AbstractContainerScreen<UpgradeMenu> {
    private static final ResourceLocation GUI_TEXTURE = Quantized.id("textures/gui/upgrade_menu/upgrade_menu_screen.png");
    private static final ResourceLocation BACK_BUTTON = Quantized.id("textures/gui/upgrade_menu/back_button.png");
    private static final ResourceLocation BACK_BUTTON_PRESSED = Quantized.id("textures/gui/upgrade_menu/back_button_pressed.png");

    private static final ResourceLocation UPGRADE_SLOT = Quantized.id("textures/gui/upgrade_menu/upgrade_slot.png");

    private static final ResourceLocation SPEED_UPGRADE_IDENTIFIER = Quantized.id("textures/gui/upgrade_menu/speed_upgrade_identifier.png");
    private static final ResourceLocation EFFICIENCY_UPGRADE_IDENTIFIER = Quantized.id("textures/gui/upgrade_menu/efficiency_upgrade_identifier.png");
    private static final ResourceLocation OUTPUT_UPGRADE_IDENTIFIER = Quantized.id("textures/gui/upgrade_menu/output_upgrade_identifier.png");

    public UpgradeScreen(UpgradeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        addRenderableWidget(new ImageTextButton(BACK_BUTTON, BACK_BUTTON_PRESSED, x + imageWidth + 4, y + 4, 18, 18,
                b -> ModMessages.sendToServer(new OpenMachineMenuC2S(menu.getMachinePos())))
        );
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        ItemStackHandler upgrades = menu.entity instanceof HasUpgradeModule hum
                ? hum.getUpgradeHandler()
                : new ItemStackHandler(0);

        List<UpgradeType> types = menu.entity.getUpgradeModule().types();

        int count = upgrades.getSlots();
        int startX = 88 - count * 9;
        for (int i = 0; i < count; i++) {
            guiGraphics.blit(UPGRADE_SLOT, x + startX + i * 18, y + 33, 0, 0, 18, 18, 18, 18);
            switch (types.get(i)) {
                case EFFICIENCY -> guiGraphics.blit(EFFICIENCY_UPGRADE_IDENTIFIER, x + startX + 2 + i * 18, y + 52, 0, 0, 14, 2, 14, 2);
                case SPEED -> guiGraphics.blit(SPEED_UPGRADE_IDENTIFIER, x + startX + 2 + i * 18, y + 52, 0, 0, 14, 2, 14, 2);
                case OUTPUT -> guiGraphics.blit(OUTPUT_UPGRADE_IDENTIFIER, x + startX + 2 + i * 18, y + 52, 0, 0, 14, 2, 14, 2);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ScreenUtils.drawCenteredString(guiGraphics, font, title, 0xFF5e6469, imageWidth / 2, titleLabelY, false);
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFF5e6469, false);
    }
}
