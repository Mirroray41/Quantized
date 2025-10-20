package net.zapp.quantized.content.blocks.quantum_analyzer.renderer;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.zapp.quantized.content.blocks.quantum_analyzer.QuantumAnalyzerTile;

public class QuantumAnalyzerRenderer implements BlockEntityRenderer<QuantumAnalyzerTile> {
    private float rotation;


    public QuantumAnalyzerRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(QuantumAnalyzerTile pBlockEntity, float pPartialTick, PoseStack pPoseStack,
                       MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay, Vec3 vec3) {

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack stack = pBlockEntity.getItemHandler().getStackInSlot(0);

        pPoseStack.pushPose();
        pPoseStack.translate(0.5f, 0.5f, 0.5f);
        pPoseStack.scale(0.5f, 0.5f, 0.5f);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(getRotation(pBlockEntity)));

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, getLightLevel(pBlockEntity.getLevel(),
                pBlockEntity.getBlockPos()), OverlayTexture.NO_OVERLAY, pPoseStack, pBufferSource, pBlockEntity.getLevel(), 1);

        pPoseStack.popPose();

    }

    private int getLightLevel(Level level, BlockPos pos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);

        return LightTexture.pack(bLight, sLight);
    }

    private float getRotation(QuantumAnalyzerTile blockEntity) {
        float deltaTime = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();

        rotation += (blockEntity.getRotationSpeed()  + ( blockEntity.getRotationSpeed() * ( (float) blockEntity.data.get(0) / blockEntity.data.get(1)))) * deltaTime;

        if(rotation >= 360) {
            rotation = rotation - 360;
        }

        return rotation;
    }
}