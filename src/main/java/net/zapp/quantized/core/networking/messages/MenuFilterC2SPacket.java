package net.zapp.quantized.core.networking.messages;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.content.blocks.quantum_analyzer.QuantumAnalyzerTile;
import net.zapp.quantized.core.utils.module.identifiers.HasDriveInterfaceModule;

public record MenuFilterC2SPacket(BlockPos pos, String query) implements CustomPacketPayload {
    public static final Type<MenuFilterC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Quantized.MOD_ID, "menu_filter_c2s"));

    public static final StreamCodec<FriendlyByteBuf, MenuFilterC2SPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, msg) -> { buf.writeBlockPos(msg.pos); buf.writeUtf(msg.query, 256); },
                    buf -> new MenuFilterC2SPacket(buf.readBlockPos(), buf.readUtf(256))
            );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            if (player == null) return;
            var lvl = player.level();
            var be = lvl.getBlockEntity(pos);
            if (be instanceof HasDriveInterfaceModule di) {
                di.setFilter(query); // rebuilds and syncs count + slots
            }
        });
    }
}
