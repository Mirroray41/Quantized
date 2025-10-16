package net.zapp.quantized.core.networking.messages;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.content.blocks.quantum_fabricator.QuantumFabricatorMenu;
import net.zapp.quantized.content.blocks.quantum_fabricator.QuantumFabricatorTile;

public record ModifyAmountButtonC2S(BlockPos pos, int valueModifier, boolean reset) implements CustomPacketPayload {
    public static final Type<ModifyAmountButtonC2S> TYPE =
            new Type<>(Quantized.id("menu_button_click_c2s"));

    public static final StreamCodec<FriendlyByteBuf, ModifyAmountButtonC2S> STREAM_CODEC =
            StreamCodec.of((buf, msg) -> {
                        buf.writeBlockPos(msg.pos); buf.writeInt(msg.valueModifier); buf.writeBoolean(msg.reset);
                        },
                    buf -> new ModifyAmountButtonC2S(buf.readBlockPos(), buf.readInt(), buf.readBoolean()));

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
            if (be instanceof QuantumFabricatorTile qf) {
                AbstractContainerMenu container = player.containerMenu;
                if (container instanceof QuantumFabricatorMenu qm) {
                    if (reset) {
                        qm.resetAmount();
                        qm.unselectItem();
                    }
                    else
                        qm.modifyAmount(valueModifier);
                }
            }
        });
    }
}
