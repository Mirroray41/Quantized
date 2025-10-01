package net.zapp.quantized.core.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record DataFluxPair(int data, int flux) {
    public static final Codec<DataFluxPair> CODEC = RecordCodecBuilder.create(i ->
            i.group(
                Codec.INT.fieldOf("data").forGetter(DataFluxPair::data),
                Codec.INT.fieldOf("flux").forGetter(DataFluxPair::flux)
    ).apply(i, DataFluxPair::new));

    public static final StreamCodec<FriendlyByteBuf, DataFluxPair> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DataFluxPair::data,
            ByteBufCodecs.VAR_INT, DataFluxPair::flux,
            DataFluxPair::new
    );

    public DataFluxPair plus(DataFluxPair other) {
        return new DataFluxPair(data + other.data, flux + other.flux);
    }

    public DataFluxPair scaled(double s) {
        return new DataFluxPair((int)(data * s), (int)(flux * s));
    }

    public boolean isZero() {
        return data == 0 && flux == 0;
    }

    public static DataFluxPair zero() {
        return new DataFluxPair(0, 0);
    }
}
