package net.zapp.quantized.core.networking.messages;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.content.blocks.upgrade.UpgradeMenu;
import net.zapp.quantized.core.utils.module.identifiers.HasUpgradeModule;

/** Asks the server to open the separate upgrade GUI for the machine at {@code pos}. */
public record OpenUpgradesC2S(BlockPos pos) implements CustomPacketPayload {
    public static final Type<OpenUpgradesC2S> TYPE = new Type<>(Quantized.id("open_upgrades_c2s"));

    public static final StreamCodec<FriendlyByteBuf, OpenUpgradesC2S> STREAM_CODEC =
            StreamCodec.of((buf, msg) -> buf.writeBlockPos(msg.pos),
                    buf -> new OpenUpgradesC2S(buf.readBlockPos()));

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
            if (!(be instanceof HasUpgradeModule)) return;
            if (!player.canInteractWithBlock(pos, 4.0)) return;

            player.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new UpgradeMenu(id, inv, be),
                    Component.translatable("gui.quantized.upgrades")), pos);
        });
    }
}
