package net.zapp.quantized.core.init;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.content.item.custom.drive_item.DriveCodec;
import net.zapp.quantized.content.item.custom.drive_item.DriveRecord;

import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Quantized.MOD_ID);

    public static final Supplier<DataComponentType<DriveRecord>> DRIVE_DATA =
            DATA_COMPONENT.register("drive_data", () ->
                    DataComponentType.<DriveRecord>builder()
                            .persistent(DriveCodec.DRIVE_CODEC)
                            .networkSynchronized(DriveCodec.DRIVE_STREAM_CODEC)
                            .build()
            );

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENT.register(modEventBus);
    }
}
