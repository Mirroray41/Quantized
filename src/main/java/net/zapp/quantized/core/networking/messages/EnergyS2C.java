package net.zapp.quantized.core.networking.messages;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.core.utils.energy.CustomEnergyStorage;
import net.zapp.quantized.core.utils.module.identifiers.HasEnergyModule;
import org.jetbrains.annotations.NotNull;

public record EnergyS2C(int energy, int capacity, BlockPos pos) implements CustomPacketPayload {
    public static final Type<EnergyS2C> ID =
            new Type<>(Quantized.id("energy_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EnergyS2C> STREAM_CODEC =
            StreamCodec.ofMember(EnergyS2C::write, EnergyS2C::new);

    public EnergyS2C(RegistryFriendlyByteBuf buffer) {
        this(buffer.readInt(), buffer.readInt(), buffer.readBlockPos());
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(energy);
        buffer.writeInt(capacity);
        buffer.writeBlockPos(pos);
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static void handle(EnergyS2C data, IPayloadContext context) {
        context.enqueueWork(() -> {
            BlockEntity blockEntity = context.player().level().getBlockEntity(data.pos);

            if(blockEntity instanceof HasEnergyModule energyModule) {
                CustomEnergyStorage energy = energyModule.getEnergyHandler();
                energy.setEnergy(data.energy);
                energy.setCapacity(data.capacity);
            }
        });
    }
}