package net.zapp.quantized.content.item.custom.drive_item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Arrays;

public class DriveCodec {
    public static final StreamCodec<ByteBuf, String[]> STRING_ARRAY =
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).map(
                    list -> list.toArray(new String[0]),
                    Arrays::asList
            );

    public static final Codec<DriveRecord> DRIVE_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("capacity").forGetter(DriveRecord::capacity),
                    Codec.INT.fieldOf("maxSizePerItem").forGetter(DriveRecord::maxSizePerItem),
                    Codec.INT.fieldOf("dataUsed").forGetter(DriveRecord::dataUsed),
                    Codec.STRING.listOf().xmap(
                            list -> list.toArray(new String[0]),
                            java.util.Arrays::asList
                    ).fieldOf("items").forGetter(DriveRecord::items),
                    Codec.INT.fieldOf("count").forGetter(DriveRecord::count)
            ).apply(instance, DriveRecord::new)
    );
    public static final StreamCodec<ByteBuf, DriveRecord> DRIVE_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, DriveRecord::capacity,
            ByteBufCodecs.INT, DriveRecord::maxSizePerItem,
            ByteBufCodecs.INT, DriveRecord::dataUsed,
            STRING_ARRAY, DriveRecord::items,
            ByteBufCodecs.INT, DriveRecord::count,

            DriveRecord::new
    );

}
