package net.zapp.quantized.networking.messages;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.api.module.identifiers.HasTankModule;

/*
 * This file incorporates code from EnergizedPower by JDDev0,
 * licensed under the MIT License.
 */
public record FluidSyncS2C(int tank, FluidStack fluidStack, int capacity, BlockPos pos) implements CustomPacketPayload {
    public static final Type<FluidSyncS2C> ID = new Type<>(Quantized.id("fluid_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidSyncS2C> STREAM_CODEC = StreamCodec.ofMember(FluidSyncS2C::write, FluidSyncS2C::new);

    public FluidSyncS2C(RegistryFriendlyByteBuf buffer) {
        this(buffer.readInt(), FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer), buffer.readInt(), buffer.readBlockPos());
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(tank);
        FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, fluidStack);
        buffer.writeInt(capacity);
        buffer.writeBlockPos(pos);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static void handle(FluidSyncS2C data, IPayloadContext context) {
        context.enqueueWork(() -> {
            BlockEntity blockEntity = context.player().level().getBlockEntity(data.pos);

            if (blockEntity instanceof HasTankModule tankModule) {
                FluidTank tank = tankModule.getTankModule().getHandler();
                tank.setFluid(data.fluidStack);
                tank.setCapacity(data.capacity);
            }
        });
    }
}
