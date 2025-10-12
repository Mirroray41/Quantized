package net.zapp.quantized.core.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class DataFluxPair {
    public static DataFluxPair zero() {
        return new DataFluxPair(0, 0);
    }

    private int data = 0;
    private int flux = 0;

    public DataFluxPair(int data, int flux) {
        this.data = data;
        this.flux = flux;
    }

    public DataFluxPair add(DataFluxPair other) {
        data += other.data;
        flux += other.flux;
        return this;
    }

    public DataFluxPair setFlux(int flux) {
        this.flux = flux;
        return this;
    }

    public DataFluxPair setData(int data) {
        this.data = data;
        return this;
    }

    public DataFluxPair mul(int k) {
        data *= k;
        flux *= k;
        return this;
    }
    public DataFluxPair div(int k) {
        data = Math.ceilDiv(data, k);
        flux = Math.max(Math.floorDiv(flux, k), 1);
        return this;
    }

    public static boolean isValid(DataFluxPair pair) {
        return pair != null && !pair.isZero();
    }

    public boolean isZero() {
        return data == 0 && flux == 0;
    }

    public int data() {
        return data;
    }

    public int flux() {
        return flux;
    }

    @Override
    public String toString() {
        return "DataFluxPair{" +
                "data=" + data +
                ", flux=" + flux +
                '}';
    }

    public DataFluxPair copy() {
        return new DataFluxPair(data,flux);
    }
}
