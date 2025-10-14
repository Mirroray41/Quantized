package net.zapp.quantized.core.networking.messages;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zapp.quantized.content.blocks.quantum_analyzer.QuantumAnalyzerMenu;
import net.zapp.quantized.content.blocks.quantum_analyzer.QuantumAnalyzerTile;

public record MenuScrollC2S(BlockPos pos, int newValue) implements CustomPacketPayload {
    public static final Type<MenuScrollC2S> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("quantized", "menu_setting_c2s"));

    public static final StreamCodec<FriendlyByteBuf, MenuScrollC2S> STREAM_CODEC =
            StreamCodec.of(
                    (buf, msg) -> { buf.writeBlockPos(msg.pos); buf.writeInt(msg.newValue); },
                    buf -> new MenuScrollC2S(buf.readBlockPos(), buf.readInt())
            );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if (!(player instanceof ServerPlayer sp)) return;
            if (sp == null) return;

            var level = sp.level();
            if (level.getBlockEntity(this.pos) instanceof QuantumAnalyzerTile be) {
                if (sp.containerMenu instanceof QuantumAnalyzerMenu menu) {
                    menu.setRowOffset(this.newValue);
                    be.setChanged();
                }
            }
        });
    }
}
