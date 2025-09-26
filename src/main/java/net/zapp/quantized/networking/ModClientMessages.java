package net.zapp.quantized.networking;


import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.zapp.quantized.networking.messages.EnergyS2C;
import net.zapp.quantized.networking.messages.FluidSyncS2C;

public final class ModClientMessages {
    private ModClientMessages() {}

    public static void register(final RegisterClientPayloadHandlersEvent event) {
        //Server -> Client
        event.register(EnergyS2C.ID, EnergyS2C::handle);
        event.register(FluidSyncS2C.ID, FluidSyncS2C::handle);
    }
}