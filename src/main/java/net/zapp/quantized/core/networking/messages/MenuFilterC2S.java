package net.zapp.quantized.core.networking.messages;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.core.utils.module.identifiers.HasDriveInterfaceModule;

public record MenuFilterC2S(BlockPos pos, String query) implements CustomPacketPayload {
    public static final Type<MenuFilterC2S> TYPE =
            new Type<>(Quantized.id("menu_filter_c2s"));

    public static final StreamCodec<FriendlyByteBuf, MenuFilterC2S> STREAM_CODEC =
            StreamCodec.of(
                    (buf, msg) -> { buf.writeBlockPos(msg.pos); buf.writeUtf(msg.query, 256); },
                    buf -> new MenuFilterC2S(buf.readBlockPos(), buf.readUtf(256))
            );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if (player == null) return;
            Level lvl = player.level();
            BlockEntity be = lvl.getBlockEntity(pos);
            if (be instanceof HasDriveInterfaceModule di) {
                di.setFilter(query); // rebuilds and syncs count + slots
            }
        });
    }
}
