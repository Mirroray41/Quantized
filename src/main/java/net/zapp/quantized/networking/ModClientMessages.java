package net.zapp.quantized.networking;


import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.zapp.quantized.block.custom.EnergyS2C;

public final class ModClientMessages {
    private ModClientMessages() {}

    public static void register(final RegisterClientPayloadHandlersEvent event) {
        //Server -> Client
        event.register(EnergyS2C.ID, EnergyS2C::handle);
    }
}