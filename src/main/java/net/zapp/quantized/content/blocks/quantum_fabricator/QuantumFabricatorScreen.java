package net.zapp.quantized.content.blocks.quantum_fabricator;

import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.fluids.FluidStack;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.client.render.FluidTankRenderState;
import net.zapp.quantized.client.render.ImageTextButton;
import net.zapp.quantized.core.networking.messages.MenuFilterC2S;
import net.zapp.quantized.core.networking.messages.MenuScrollC2S;
import net.zapp.quantized.core.networking.messages.ModifyAmountButtonC2S;
import org.joml.Matrix3x2f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class QuantumFabricatorScreen extends AbstractContainerScreen<QuantumFabricatorMenu> {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Quantized.MOD_ID,"textures/gui/quantum_fabricator/quantum_fabricator_screen.png");
    private static final ResourceLocation PROGRESS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Quantized.MOD_ID,"textures/gui/progress.png");
    private static final ResourceLocation ENERGY_BAR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Quantized.MOD_ID,"textures/gui/energy_bar.png");
    private static final ResourceLocation SCROLL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Quantized.MOD_ID,"textures/gui/scroll.png");
    private static final ResourceLocation FLUID_BAR_OVERLAY_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Quantized.MOD_ID,"textures/gui/fluid_bar_overlay.png");
    private static final ResourceLocation BUTTON_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Quantized.MOD_ID,"textures/gui/button.png");
    private static final ResourceLocation BUTTON_PRESSED_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Quantized.MOD_ID,"textures/gui/button_pressed.png");
    private static final ResourceLocation SELECTED_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Quantized.MOD_ID,"textures/gui/quantum_fabricator/selected.png");

    protected int imageHeight = 233;
    protected int imageWidth = 196;

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

    // NEW: track which action button is currently shown and keep it in sync each frame
    private boolean showingCancel = false;

    public QuantumFabricatorScreen(QuantumFabricatorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        rows = menu.getItemCount() / 9;
        if (menu.getItemCount() % 9 != 0) rows++;
        if (rows > 3) scrollStep = (float) scrollHeight / (rows - 3);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SCROLL_TEXTURE, x + 174, y + 30 + Math.round(scrollAmount), 0, 0, 12, 15, 12, 15);

        renderProgressArrow(guiGraphics, x, y);
        renderEnergyBar(guiGraphics, x, y);
        renderFluidTank(guiGraphics, x, y);
    }

    private void renderProgressArrow(GuiGraphics g, int x, int y) {
        if (menu.isCrafting()) {
            g.blit(RenderPipelines.GUI_TEXTURED, PROGRESS_TEXTURE, x + 86, y + 124, 0, 0, menu.getScaledArrowProgress(), 4, 24, 4);
        }
    }

    private void renderEnergyBar(GuiGraphics g, int x, int y) {
        g.blit(RenderPipelines.GUI_TEXTURED, ENERGY_BAR_TEXTURE, x + 4, y + 150 + 54 - menu.getScaledEnergyBar(), 0, 54 - menu.getScaledEnergyBar(), 12, menu.getScaledEnergyBar(), 12, 54);
    }

    private void renderFluidTank(GuiGraphics g, int x, int y) {
        renderFluidMeterContent(g, menu.getFluid(), menu.getFluidCapacity(), x + 181, y + 151, 10, 52);
        g.blit(RenderPipelines.GUI_TEXTURED, FLUID_BAR_OVERLAY_TEXTURE, x + 180, y + 150, 0, 0, 12, 54, 12, 54);
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

        sendButton   = new ImageTextButton(BUTTON_TEXTURE, BUTTON_PRESSED_TEXTURE, x + 117, y + 106, 16, 12, p -> syncAmountSelector(false), Component.literal("✔").withColor(Color.GREEN.getRGB()));
        cancelButton = new ImageTextButton(BUTTON_TEXTURE, BUTTON_PRESSED_TEXTURE, x + 117, y + 106, 16, 12, p -> syncAmountSelector(true ), Component.literal("✘").withColor(Color.RED.getRGB()));

        addRenderableWidget(new ImageTextButton(BUTTON_TEXTURE, BUTTON_PRESSED_TEXTURE, x + 135, y + 97, 16, 12, p -> modifyCount(1),   Component.literal("+I").withColor(Color.GREEN.getRGB())));
        addRenderableWidget(new ImageTextButton(BUTTON_TEXTURE, BUTTON_PRESSED_TEXTURE, x + 153, y + 97, 16, 12, p -> modifyCount(10),  Component.literal("+X").withColor(Color.GREEN.getRGB())));
        addRenderableWidget(new ImageTextButton(BUTTON_TEXTURE, BUTTON_PRESSED_TEXTURE, x + 171, y + 97, 16, 12, p -> modifyCount(100), Component.literal("+C").withColor(Color.GREEN.getRGB())));

        addRenderableWidget(new ImageTextButton(BUTTON_TEXTURE, BUTTON_PRESSED_TEXTURE, x + 135, y + 115, 16, 12, p -> modifyCount(-1),   Component.literal("-I").withColor(Color.RED.getRGB())));
        addRenderableWidget(new ImageTextButton(BUTTON_TEXTURE, BUTTON_PRESSED_TEXTURE, x + 153, y + 115, 16, 12, p -> modifyCount(-10),  Component.literal("-X").withColor(Color.RED.getRGB())));
        addRenderableWidget(new ImageTextButton(BUTTON_TEXTURE, BUTTON_PRESSED_TEXTURE, x + 171, y + 115, 16, 12, p -> modifyCount(-100), Component.literal("-C").withColor(Color.RED.getRGB())));

        boolean hasWork = (menu.getAmount() > 0) || menu.isCrafting();
        if (hasWork) {
            addRenderableWidget(cancelButton);
            showingCancel = true;
        } else {
            addRenderableWidget(sendButton);
            showingCancel = false;
        }


    }

    private void drawQueuedOverlay(GuiGraphics g) {
        if (queued == null) return;
        int x = leftPos + queued.x;
        int y = topPos + queued.y;
        g.blit(RenderPipelines.GUI_TEXTURED, SELECTED_TEXTURE, x - 3, y - 3, 0, 0, 22, 22, 22, 22);
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
        g.drawCenteredString(font, Component.literal("" + amnt), x + 98, y + 86, 0xFFeaf2f8);

        this.queued = computeQueuedSlot();
        drawQueuedOverlay(g);

        // keep the button reflecting actual selection state
        updateSendCancelButton();

        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, (imageWidth / 2) - (getTextLen(title.getString()) / 2) - 6, titleLabelY - 35, 0xFF5e6469, false);
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY + 33, 0xFF5e6469, false);
    }

    protected void renderFluidMeterContent(GuiGraphics g, FluidStack fluidStack, int tankCapacity, int x, int y, int w, int h) {
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        renderFluidStack(g, fluidStack, tankCapacity, w, h);
        g.pose().popMatrix();
    }

    private void renderFluidStack(GuiGraphics g, FluidStack fluidStack, int tankCapacity, int w, int h) {
        if (fluidStack.isEmpty()) return;

        Fluid fluid = fluidStack.getFluid();
        IClientFluidTypeExtensions fx = IClientFluidTypeExtensions.of(fluid);
        ResourceLocation stillFluidImageId = fx.getStillTexture(fluidStack);
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(stillFluidImageId);

        int tint = fx.getTintColor(fluidStack);
        int fluidMeterPos = tankCapacity == -1 || (fluidStack.getAmount() > 0 && fluidStack.getAmount() == tankCapacity)
                ? 0 : (h - ((fluidStack.getAmount() <= 0 || tankCapacity == 0) ? 0 :
                (Math.min(fluidStack.getAmount(), tankCapacity - 1) * h / tankCapacity + 1)));

        for (int yOffset = h; yOffset > fluidMeterPos; yOffset -= 16) {
            for (int xOffset = 0; xOffset < w; xOffset += 16) {
                int width = Math.min(w - xOffset, 16);
                int height = Math.min(yOffset - fluidMeterPos, 16);

                float u0 = sprite.getU0();
                float u1 = sprite.getU1();
                float v0 = sprite.getV0();
                float v1 = sprite.getV1();
                u1 = u1 - ((16 - width) / 16.f * (u1 - u0));
                v0 = v0 - ((16 - height) / 16.f * (v0 - v1));

                GpuTextureView tex = minecraft.getTextureManager().getTexture(sprite.atlasLocation()).getTextureView();
                g.guiRenderState.submitGuiElement(new FluidTankRenderState(
                        RenderPipelines.GUI_TEXTURED, TextureSetup.singleTexture(tex),
                        new Matrix3x2f(g.pose()),
                        xOffset, yOffset, width, height,
                        u0, u1, v0, v1, tint,
                        g.scissorStack.peek()
                ));
            }
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics g, int mouseX, int mouseY) {
        super.renderTooltip(g, mouseX, mouseY);

        if (isHovering(-6, 116, 12, 54, mouseX, mouseY)) {
            List<Component> components = new ArrayList<>(2);
            components.add(Component.translatable("tooltip.quantized.battery.energy_stored", menu.getEnergyStored(), menu.getEnergyCapacity()));
            components.add(Component.translatable("tooltip.quantized.battery.energy_usage", menu.getEnergyConsumption()));
            g.setTooltipForNextFrame(font, components, Optional.empty(), mouseX, mouseY);
        } else if (isHovering(170, 116, 12, 54, mouseX, mouseY)) {
            List<Component> components = new ArrayList<>(2);
            components.add(menu.getFluid().getHoverName());
            components.add(Component.translatable("tooltip.quantized.tank.fluid_stored", menu.getFluid().getAmount(), menu.getFluidCapacity()));
            g.setTooltipForNextFrame(font, components, Optional.empty(), mouseX, mouseY);
        } else if (isHovering(76, 90, 24, 3, mouseX, mouseY)) {
            List<Component> components = new ArrayList<>(1);
            components.add(Component.translatable("tooltip.quantized.progress.progress_ticks", menu.getProgress(), menu.getMaxProgress(), menu.getProgressPercentage()));
            g.setTooltipForNextFrame(font, components, Optional.empty(), mouseX, mouseY);
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
        ClientPacketDistributor.sendToServer(new MenuFilterC2S(menu.blockEntity.getBlockPos(), ""));
        ClientPacketDistributor.sendToServer(new MenuScrollC2S(menu.blockEntity.getBlockPos(), 0));
        super.onClose();
    }

    private void onSearchChanged(String text) {
        if (Objects.equals(text, lastSent)) return;
        lastSent = text;
        ClientPacketDistributor.sendToServer(new MenuFilterC2S(menu.blockEntity.getBlockPos(), text));
        rowOffest = 0;
        scrollAmount = 0;
        syncScrollOffset();
    }

    private void syncScrollOffset() {
        ClientPacketDistributor.sendToServer(new MenuScrollC2S(menu.blockEntity.getBlockPos(), rowOffest));
        menu.setRowOffset(rowOffest);
    }

    private void modifyCount(int count) {
        this.count = Math.max(this.count + count, 0);
    }

    private void syncAmountSelector(boolean reset) {
        ClientPacketDistributor.sendToServer(new ModifyAmountButtonC2S(menu.blockEntity.getBlockPos(), count, reset));

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

    private Slot computeQueuedSlot() {
        if (hoveredSlot != null) {
            int idx = menu.slots.indexOf(hoveredSlot);
            if (idx >= 7+36 && idx <= 33+36) return hoveredSlot;
        }
        return menu.getQueuedItemSlot();
    }
}
