package net.zapp.quantized.content.blocks.quantum_replicator.renderer;


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
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.zapp.quantized.content.blocks.quantum_replicator.QuantumReplicatorTile;
import net.zapp.quantized.content.blocks.quantum_stabilizer.QuantumStabilizerTile;
import net.zapp.quantized.core.init.ModItems;

public class QuantumReplicatorRenderer implements BlockEntityRenderer<QuantumReplicatorTile> {
    public QuantumReplicatorRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(QuantumReplicatorTile pBlockEntity, float pPartialTick, PoseStack pPoseStack,
                       MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack stack = new ItemStack(Items.AIR);
        if (pBlockEntity.getImprintedItem() != null) {
            stack = pBlockEntity.getImprintedItem().getDefaultInstance();
        }

        float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);


        pPoseStack.pushPose();
        pPoseStack.translate(0.5f, 0.5f, 0.5f);
        pPoseStack.scale(getScale(pBlockEntity, partialTick), getScale(pBlockEntity, partialTick), getScale(pBlockEntity, partialTick));
        pPoseStack.mulPose(Axis.YP.rotationDegrees(getRotation(pBlockEntity, partialTick)));

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, getLightLevel(pBlockEntity.getLevel(),
                pBlockEntity.getBlockPos()), OverlayTexture.NO_OVERLAY, pPoseStack, pBufferSource, pBlockEntity.getLevel(), 1);

        pPoseStack.popPose();

    }

    private int getLightLevel(Level level, BlockPos pos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);

        return LightTexture.pack(bLight, sLight);
    }

    private float getScale(QuantumReplicatorTile blockEntity, float partialTick) {
        return Mth.lerp(partialTick, blockEntity.prevScale, blockEntity.scale);
    }

    private float getRotation(QuantumReplicatorTile blockEntity, float partialTick) {
        return Mth.lerp(partialTick, blockEntity.prevRotation, blockEntity.rotation);
    }
}