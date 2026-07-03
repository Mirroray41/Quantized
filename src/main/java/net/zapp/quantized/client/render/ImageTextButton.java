package net.zapp.quantized.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.zapp.quantized.Quantized;

public class ImageTextButton extends Button {
    private final ResourceLocation TEXTURE;
    private final ResourceLocation TEXTURE_PRESSED;

    // full texture size (pixels) — set these to your PNG's actual size
    private final int TEX_W;
    private final int TEX_H;

    private boolean pressed;
    private boolean shadow = true;

    public ImageTextButton(ResourceLocation texture, ResourceLocation texturePressed, int x, int y, int width, int height, OnPress onPress, Component message, boolean shadow) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        TEXTURE = texture;
        TEXTURE_PRESSED = texturePressed;
        TEX_W = width;
        TEX_H = height;
        this.shadow = shadow;
    }

    public ImageTextButton(ResourceLocation texture, ResourceLocation texturePressed, int x, int y, int width, int height, OnPress onPress, Component message) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        TEXTURE = texture;
        TEXTURE_PRESSED = texturePressed;
        TEX_W = width;
        TEX_H = height;
    }

    public ImageTextButton(ResourceLocation texture, ResourceLocation texturePressed, int x, int y, int width, int height, OnPress onPress) {
        this(texture, texturePressed, x, y, width, height, onPress, CommonComponents.EMPTY);
    }

    @Override
    protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        if (!pressed) {
            gfx.blit(TEXTURE, getX(), getY(), 0, 0, TEX_W, TEX_H, TEX_W, TEX_H);    // full texture size
        } else {
            gfx.blit(TEXTURE_PRESSED, getX(), getY(), 0, 0, TEX_W, TEX_H, TEX_W, TEX_H);    // full texture size
            pressed = false;
        }

        var font = Minecraft.getInstance().font;

        int color = 0xFFFFFFFF;
        var tc = getMessage().getStyle().getColor();
        if (tc != null) color = 0xFF000000 | tc.getValue();

        gfx.pose().pushPose();
        gfx.pose().translate(0, 0, 0);

        // 1. Calculate centered X position manually
        int textWidth = font.width(getMessage());
        int textX = getX() + this.width / 2 - textWidth / 2;
        int textY = getY() + (this.height - 8) / 2;

        // 2. Use drawString with your custom shadow boolean (e.g., this.renderTextShadow)
        gfx.drawString(font, getMessage(), textX, textY, color, this.shadow);

        gfx.pose().popPose();

        RenderSystem.setShaderColor(1.f, 1.f, 1.f, 1.f);
    }

    @Override
    public void onPress() {
        super.onPress();
        this.pressed = true;
    }
}

