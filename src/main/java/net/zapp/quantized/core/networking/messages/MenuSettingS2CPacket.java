package net.zapp.quantized.core.networking.messages;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zapp.quantized.content.blocks.quantum_analyzer.QuantumAnalyzerMenu;

public record MenuSettingS2CPacket(BlockPos pos, int newValue) implements CustomPacketPayload {
    public static final Type<MenuSettingS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("quantized", "menu_setting_s2c"));

    public static final StreamCodec<FriendlyByteBuf, MenuSettingS2CPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, msg) -> { buf.writeBlockPos(msg.pos); buf.writeInt(msg.newValue); },
                    buf -> new MenuSettingS2CPacket(buf.readBlockPos(), buf.readInt())
            );

    @Override public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var mc = Minecraft.getInstance();
            if (mc.player == null) return;
            if (mc.player.containerMenu instanceof QuantumAnalyzerMenu menu) {
                menu.setRowOffset(this.newValue); // update client copy
            }
        });
    }
}