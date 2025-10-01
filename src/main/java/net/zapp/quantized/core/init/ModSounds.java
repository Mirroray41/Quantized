package net.zapp.quantized.core.init;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zapp.quantized.Quantized;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Quantized.MOD_ID);

    public static final Holder<SoundEvent> QUANTUM_DESTABILIZER_WORK = SOUND_EVENTS.register(
            "quantum_destabilizer_work",
            // Takes in the registry name
            SoundEvent::createVariableRangeEvent
    );

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}