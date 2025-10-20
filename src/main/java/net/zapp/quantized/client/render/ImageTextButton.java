package net.zapp.quantized.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
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
            gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, getX(), getY(), 0, 0, TEX_W, TEX_H, TEX_W, TEX_H);    // full texture size
        } else {
            gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_PRESSED, getX(), getY(), 0, 0, TEX_W, TEX_H, TEX_W, TEX_H);    // full texture size
            pressed = false;
        }

        var font = Minecraft.getInstance().font;

        int color = 0xFFFFFFFF;
        var tc = getMessage().getStyle().getColor();
        if (tc != null) color = 0xFF000000 | tc.getValue();

        gfx.pose().pushMatrix();
        gfx.pose().translate(0, 0);
        gfx.drawCenteredString(font, getMessage(), getX() + this.width / 2, getY() + (this.height - 8) / 2, color);
        gfx.pose().popMatrix();
    }

    @Override
    public void onPress() {
        super.onPress();
        this.pressed = true;
    }
}

