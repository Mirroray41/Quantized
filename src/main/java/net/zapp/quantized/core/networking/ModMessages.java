package net.zapp.quantized.core.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.zapp.quantized.core.networking.messages.EnergyS2C;
import net.zapp.quantized.core.networking.messages.FluidSyncS2C;

public final class ModMessages {
    private ModMessages() {}

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1.0");

        registrar.playToClient(EnergyS2C.ID, EnergyS2C.STREAM_CODEC);
        registrar.playToClient(FluidSyncS2C.ID, FluidSyncS2C.STREAM_CODEC);
    }

    public static void sendToServer(CustomPacketPayload message) {
        ClientPacketDistributor.sendToServer(message);
    }

    public static void sendToPlayer(CustomPacketPayload message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public static void sendToPlayersWithinXBlocks(CustomPacketPayload message, BlockPos pos, ServerLevel level, int distance) {
        PacketDistributor.sendToPlayersNear(level, null, pos.getX(), pos.getY(), pos.getZ(), distance, message);
    }

    public static void sendToAllPlayers(CustomPacketPayload message) {
        PacketDistributor.sendToAllPlayers(message);
    }
}