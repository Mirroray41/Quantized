package net.zapp.quantized.core.networking.messages;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zapp.quantized.Quantized;

/** Re-opens a machine's own menu from the separate upgrade GUI (the "back" button). */
public record OpenMachineMenuC2S(BlockPos pos) implements CustomPacketPayload {
    public static final Type<OpenMachineMenuC2S> TYPE = new Type<>(Quantized.id("open_machine_menu_c2s"));

    public static final StreamCodec<FriendlyByteBuf, OpenMachineMenuC2S> STREAM_CODEC =
            StreamCodec.of((buf, msg) -> buf.writeBlockPos(msg.pos),
                    buf -> new OpenMachineMenuC2S(buf.readBlockPos()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if (player == null) return;
            Level lvl = player.level();
            BlockEntity be = lvl.getBlockEntity(pos);
            if (!(be instanceof MenuProvider provider)) return;
            if (!player.canInteractWithBlock(pos, 4.0)) return;

            player.openMenu(provider, pos);
        });
    }
}
