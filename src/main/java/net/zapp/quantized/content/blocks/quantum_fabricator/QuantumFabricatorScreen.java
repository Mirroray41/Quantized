package net.zapp.quantized.content.blocks.quantum_fabricator;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.client.render.ImageTextButton;
import net.zapp.quantized.core.networking.messages.MenuFilterC2S;
import net.zapp.quantized.core.networking.messages.MenuScrollC2S;
import net.zapp.quantized.core.networking.messages.ModifyAmountButtonC2S;
import net.zapp.quantized.core.utils.screen.ScreenUtils;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.gui.Font;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class QuantumFabricatorScreen extends AbstractContainerScreen<QuantumFabricatorMenu> {
    private static final ResourceLocation GUI_TEXTURE =Quantized.id("textures/gui/quantum_fabricator/quantum_fabricator_screen.png");
    private static final ResourceLocation PROGRESS_TEXTURE = Quantized.id("textures/gui/progress.png");
    private static final ResourceLocation ENERGY_BAR_TEXTURE = Quantized.id("textures/gui/energy_bar.png");
    private static final ResourceLocation SCROLL_TEXTURE = Quantized.id("textures/gui/scroll.png");
    private static final ResourceLocation FLUID_BAR_OVERLAY_TEXTURE = Quantized.id("textures/gui/fluid_bar_overlay.png");
    private static final ResourceLocation BUTTON_TEXTURE = Quantized.id("textures/gui/button.png");
    private static final ResourceLocation BUTTON_PRESSED_TEXTURE = Quantized.id("textures/gui/button_pressed.png");
    private static final ResourceLocation SELECTED_TEXTURE = Quantized.id("textures/gui/quantum_fabricator/selected.png");

    private static final ResourceLocation UPGRADE_BUTTON = Quantized.id("textures/gui/upgrade_button.png");
    private static final ResourceLocation UPGRADE_BUTTON_PRESSED = Quantized.id("textures/gui/upgrade_button_pressed.png");


    protected int imageHeight;
    protected int imageWidth;

    private int standardImageWidth = 176;

    private final int scrollHeight = 37;

    private AbstractWidget sendButton;
    private AbstractWidget cancelButton;

    private float scrollAmount = 0;
    private float scrollStep = 1;
    private int rowOffest;
    private int rows;

    private EditBox searchBox;
    private String lastSent = "";

    private int count = 0;
    private Slot queued = null;
    private ItemStack queuedItem = ItemStack.EMPTY;

    // NEW: track which action button is currently shown and keep it in sync each frame
    private boolean showingCancel = false;

    public QuantumFabricatorScreen(QuantumFabricatorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 196;
        this.imageHeight = 233;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        rows = menu.getItemCount() / 9;
        if (menu.getItemCount() % 9 != 0) rows++;
        if (rows > 3) scrollStep = (float) scrollHeight / (rows - 3);

        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
        guiGraphics.blit(SCROLL_TEXTURE, x + 174, y + 30 + Math.round(scrollAmount), 0, 0, 12, 15, 12, 15);

        renderProgressArrow(guiGraphics, x, y);
        renderEnergyBar(guiGraphics, x, y);
        renderFluidTank(guiGraphics, x, y);
    }

    private void renderProgressArrow(GuiGraphics g, int x, int y) {
        if (menu.isCrafting()) {
            g.blit(PROGRESS_TEXTURE, x + 86, y + 124, 0, 0, menu.getScaledArrowProgress(), 4, 24, 4);
        }
    }

    private void renderEnergyBar(GuiGraphics g, int x, int y) {
        g.blit(ENERGY_BAR_TEXTURE, x + 4, y + 150 + 54 - menu.getScaledEnergyBar(), 0, 54 - menu.getScaledEnergyBar(), 12, menu.getScaledEnergyBar(), 12, 54);
    }

    private void renderFluidTank(GuiGraphics g, int x, int y) {
        ScreenUtils.renderFluidMeterContent(g, menu.getFluid(), menu.getFluidCapacity(), x + 181, y + 151, 10, 52);
        g.blit(FLUID_BAR_OVERLAY_TEXTURE, x + 180, y + 150, 0, 0, 12, 54, 12, 54);
    }

    @Override
    protected void init() {
        super.init();
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        searchBox = new EditBox(font, x + 82, y + 16, 90, 12, Component.literal("Search"));
        searchBox.setBordered(true);
        searchBox.setFGColor(0xFFFFFF);
        searchBox.setMaxLength(128);
        searchBox.setResponder(this::onSearchChanged);
        addRenderableWidget(searchBox);

        sendButton   = new ImageTextButton(BUTTON_TEXTURE, BUTTON_PRESSED_TEXTURE, x + 117, y + 106, 16, 12, p -> syncAmountSelector(false), Component.literal("✔").withColor(Color.GREEN.getRGB()), false);
        cancelButton = new ImageTextButton(BUTTON_TEXTURE, BUTTON_PRESSED_TEXTURE, x + 117, y + 106, 16, 12, p -> syncAmountSelector(true ), Component.literal("✘").withColor(Color.RED.getRGB()), false);

        addRenderableWidget(new ImageTextButton(BUTTON_TEXTURE, BUTTON_PRESSED_TEXTURE, x + 135, y + 97, 16, 12, p -> modifyCount(1),   Component.literal("+I").withColor(Color.GREEN.getRGB()), false));
        addRenderableWidget(new ImageTextButton(BUTTON_TEXTURE, BUTTON_PRESSED_TEXTURE, x + 153, y + 97, 16, 12, p -> modifyCount(10),  Component.literal("+X").withColor(Color.GREEN.getRGB()), false));
        addRenderableWidget(new ImageTextButton(BUTTON_TEXTURE, BUTTON_PRESSED_TEXTURE, x + 171, y + 97, 16, 12, p -> modifyCount(100), Component.literal("+C").withColor(Color.GREEN.getRGB()), false));

        addRenderableWidget(new ImageTextButton(BUTTON_TEXTURE, BUTTON_PRESSED_TEXTURE, x + 135, y + 115, 16, 12, p -> modifyCount(-1),   Component.literal("-I").withColor(Color.RED.getRGB()), false));
        addRenderableWidget(new ImageTextButton(BUTTON_TEXTURE, BUTTON_PRESSED_TEXTURE, x + 153, y + 115, 16, 12, p -> modifyCount(-10),  Component.literal("-X").withColor(Color.RED.getRGB()), false));
        addRenderableWidget(new ImageTextButton(BUTTON_TEXTURE, BUTTON_PRESSED_TEXTURE, x + 171, y + 115, 16, 12, p -> modifyCount(-100), Component.literal("-C").withColor(Color.RED.getRGB()), false));

        boolean hasWork = (menu.getAmount() > 0) || menu.isCrafting();
        if (hasWork) {
            addRenderableWidget(cancelButton);
            showingCancel = true;
        } else {
            addRenderableWidget(sendButton);
            showingCancel = false;
        }

        addRenderableWidget(new ImageTextButton(UPGRADE_BUTTON, UPGRADE_BUTTON_PRESSED, x + imageWidth + 4, y + 4, 18, 18,
                p -> net.zapp.quantized.core.networking.ModMessages.sendToServer(
                        new net.zapp.quantized.core.networking.messages.OpenUpgradesC2S(menu.blockEntity.getBlockPos()))
                )
        );
    }

    private void drawQueuedOverlay(GuiGraphics g) {
        if (queued == null) return;
        int x = leftPos + queued.x;
        int y = topPos + queued.y;
        g.blit(SELECTED_TEXTURE, x - 3, y - 3, 0, 0, 22, 22, 22, 22);
    }

    private void updateSendCancelButton() {
        boolean hasWork = (menu.getAmount() > 0) || menu.isCrafting();
        if (hasWork == showingCancel) return;

        if (hasWork) {
            removeWidget(sendButton);
            addRenderableWidget(cancelButton);
        } else {
            removeWidget(cancelButton);
            addRenderableWidget(sendButton);
        }
        showingCancel = hasWork;
    }


    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        int amnt = menu.getAmount() > 0 ? menu.getAmount() : count;
        ScreenUtils.drawCenteredString(g, font, Component.literal("" + amnt), 0xFF5e6469, x + 98, y + 86, false);

        this.queued = computeQueuedSlot();
        drawQueuedOverlay(g);

        // keep the button reflecting actual selection state
        updateSendCancelButton();

        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY + 33, 0xFF5e6469, false);
        // using standard gui size because renderLabels ignores custom imageWidth for some reason
        ScreenUtils.drawCenteredString(g, font, title, 0xFF5e6469, standardImageWidth / 2 , titleLabelY - 35, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics g, int mouseX, int mouseY) {
        super.renderTooltip(g, mouseX, mouseY);

        if (isHovering(-6, 116, 12, 54, mouseX, mouseY)) {
            List<Component> components = new ArrayList<>(2);
            components.add(Component.translatable("tooltip.quantized.battery.energy_stored", menu.getEnergyStored(), menu.getEnergyCapacity()));
            components.add(Component.translatable("tooltip.quantized.battery.energy_usage", menu.getEnergyConsumption()));
            g.renderTooltip(font, components, Optional.empty(), mouseX, mouseY);
        } else if (isHovering(170, 116, 12, 54, mouseX, mouseY)) {
            List<Component> components = new ArrayList<>(2);
            components.add(menu.getFluid().getHoverName());
            components.add(Component.translatable("tooltip.quantized.tank.fluid_stored", menu.getFluid().getAmount(), menu.getFluidCapacity()));
            g.renderTooltip(font, components, Optional.empty(), mouseX, mouseY);
        } else if (isHovering(76, 90, 24, 3, mouseX, mouseY)) {
            List<Component> components = new ArrayList<>(1);
            components.add(Component.translatable("tooltip.quantized.progress.progress_ticks", menu.getProgress(), menu.getMaxProgress(), menu.getProgressPercentage()));
            g.renderTooltip(font, components, Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isHovering(0, -4, 175, 54, mouseX, mouseY)) {
            if (rows > 3 && scrollAmount - (scrollStep * scrollY) <= scrollHeight && scrollAmount - (scrollStep * scrollY) >= 0) {
                rowOffest -= scrollY;
                scrollAmount = rowOffest * scrollStep;
                syncScrollOffset();
            }
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (searchBox != null && searchBox.isMouseOver(mouseX, mouseY)) {
            setFocused(searchBox);
            searchBox.setFocused(true);
            return searchBox.mouseClicked(mouseX, mouseY, button);
        }
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (searchBox != null) {
            searchBox.setFocused(false);
            if (getFocused() == searchBox) setFocused(null);
        }
        return handled;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchBox != null && searchBox.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                searchBox.setFocused(false);
                if (getFocused() == searchBox) setFocused(null);
                return true;
            }
            if (minecraft != null && minecraft.options.keyInventory.matches(keyCode, scanCode)) return true;
            if (checkHotbarKeyPressed(keyCode, scanCode)) return true;
            if (searchBox.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searchBox != null && searchBox.isFocused() && searchBox.charTyped(codePoint, modifiers)) return true;
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void onClose() {
        rowOffest = 0;
        scrollAmount = 0;
        if (searchBox != null) {
            searchBox.setValue("");
            lastSent = "";
        }
        PacketDistributor.sendToServer(new MenuFilterC2S(menu.blockEntity.getBlockPos(), ""));
        PacketDistributor.sendToServer(new MenuScrollC2S(menu.blockEntity.getBlockPos(), 0));
        super.onClose();
    }

    private void onSearchChanged(String text) {
        if (Objects.equals(text, lastSent)) return;
        lastSent = text;
        PacketDistributor.sendToServer(new MenuFilterC2S(menu.blockEntity.getBlockPos(), text));
        rowOffest = 0;
        scrollAmount = 0;
        syncScrollOffset();
    }

    private void syncScrollOffset() {
        PacketDistributor.sendToServer(new MenuScrollC2S(menu.blockEntity.getBlockPos(), rowOffest));
        menu.setRowOffset(rowOffest);
    }

    private void modifyCount(int count) {
        this.count = Math.max(this.count + count, 0);
    }

    private void syncAmountSelector(boolean reset) {
        PacketDistributor.sendToServer(new ModifyAmountButtonC2S(menu.blockEntity.getBlockPos(), count, reset));

        if (reset) {
            queued = null;
            menu.unselectItem();
            removeWidget(cancelButton);
            addRenderableWidget(sendButton);
            showingCancel = false;
        } else {
            removeWidget(sendButton);
            addRenderableWidget(cancelButton);
            showingCancel = true;
        }

        count = 0;
    }

    private Slot computeQueuedSlot() {
        if (hoveredSlot != null) {
            int idx = menu.slots.indexOf(hoveredSlot);
            if (idx >= 7+36 && idx <= 33+36) return hoveredSlot;
        }
        return menu.getQueuedItemSlot();
    }
}
