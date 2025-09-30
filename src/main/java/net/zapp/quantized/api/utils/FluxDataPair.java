package net.zapp.quantized.api.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FluxDataPair(int data, int flux) {
    public static final Codec<FluxDataPair> CODEC = RecordCodecBuilder.create(i ->
            i.group(
                Codec.INT.fieldOf("data").forGetter(FluxDataPair::data),
                Codec.INT.fieldOf("flux").forGetter(FluxDataPair::flux)
    ).apply(i, FluxDataPair::new));

    public static final StreamCodec<FriendlyByteBuf, FluxDataPair> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, FluxDataPair::data,
            ByteBufCodecs.VAR_INT, FluxDataPair::flux,
            FluxDataPair::new
    );

    public FluxDataPair plus(FluxDataPair other) {
        return new FluxDataPair(data + other.data, flux + other.flux);
    }

    public FluxDataPair scaled(double s) {
        return new FluxDataPair((int)(data * s), (int)(flux * s));
    }

    public boolean isZero() {
        return data == 0 && flux == 0;
    }


}
