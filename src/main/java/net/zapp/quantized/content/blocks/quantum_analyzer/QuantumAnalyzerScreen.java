package net.zapp.quantized.content.blocks.quantum_analyzer;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.SessionSearchTrees;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.CreativeModeTabSearchRegistry;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.core.networking.messages.MenuSettingC2SPacket;
import org.lwjgl.glfw.GLFW;

import java.util.*;

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

    private final int scrollHeight = 37;

    private float scrollAmount = 0;

    private float scrollStep = 1;

    private int rowOffest;

    private int rows;

    public QuantumAnalyzerScreen(QuantumAnalyzerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        rows = menu.getItemCount() / 5;

        if (menu.getItemCount() % 5 != 0) {
            rows++;
        }

        if (rows > 3) {
            scrollStep = (float) scrollHeight / (rows - 3);
        }

        //System.out.println(rows);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SCROLL_TEXTURE, x + 154, y + 31 + Math.round(scrollAmount), 0, 0, 12, 15, 12, 15);


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

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        //System.out.println(rows + ", " + (scrollAmount - (scrollStep * scrollY)));
        if (isHovering(62, 30, 104, 54, mouseX, mouseY)) {
            if (rows > 3 && scrollAmount - (scrollStep * scrollY) <= scrollHeight && scrollAmount - (scrollStep * scrollY) >= 0) {
                rowOffest -= scrollY;
                scrollAmount = rowOffest * scrollStep;
                //System.out.println(rowOffest + ", " + scrollAmount);
                ClientPacketDistributor.sendToServer(new MenuSettingC2SPacket(menu.blockEntity.getBlockPos(), rowOffest));
                menu.setRowOffset(rowOffest);
            }
        }

        return true;
    }

    /*
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.ignoreTextInput) {
            return false;
        } else if (!selectedTab.hasSearchBar()) {
            return false;
        } else {
            String s = this.searchBox.getValue();
            if (this.searchBox.charTyped(codePoint, modifiers)) {
                if (!Objects.equals(s, this.searchBox.getValue())) {
                    this.refreshSearchResults();
                }

                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.ignoreTextInput = false;
        if (!selectedTab.hasSearchBar()) {
            if (this.minecraft.options.keyChat.matches(keyCode, scanCode)) {
                this.ignoreTextInput = true;
                this.selectTab(CreativeModeTabs.searchTab());
                return true;
            } else {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        } else {
            boolean flag = !this.isCreativeSlot(this.hoveredSlot) || this.hoveredSlot.hasItem();
            boolean flag1 = InputConstants.getKey(keyCode, scanCode).getNumericKeyValue().isPresent();
            if (flag && flag1 && this.checkHotbarKeyPressed(keyCode, scanCode)) {
                this.ignoreTextInput = true;
                return true;
            } else {
                String s = this.searchBox.getValue();
                if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
                    if (!Objects.equals(s, this.searchBox.getValue())) {
                        this.refreshSearchResults();
                    }

                    return true;
                } else {
                    return this.searchBox.isFocused() && this.searchBox.isVisible() && keyCode != 256 ? true : super.keyPressed(keyCode, scanCode, modifiers);
                }
            }
        }
    }


    private void refreshSearchResults() {

    }*/
}
