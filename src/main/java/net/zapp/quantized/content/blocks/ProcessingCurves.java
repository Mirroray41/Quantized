package net.zapp.quantized.content.blocks;

import net.zapp.quantized.core.utils.DataFluxPair;

public class ProcessingCurves {
    // Cubic
    private static final float AT = 0.1f;
    private static final int BT = 2;
    private static final int CT = 2;
    private static final int DT = 10;
    private static final int T_MIN = 60;

    // Cubic
    private static final float APwr = 0.1f;
    private static final int BPwr = 10;
    private static final int CPwr = 15;
    private static final int DPwr = 40;


    // Scales the "x" term in our quadratic equations, it becomes larger the larger our input integer is.
    private static int bucket(int x) {
        return (x <= 0) ? 0 : 1 + (31 - Integer.numberOfLeadingZeros(x));
    }

    public static int timeTicks(int dataValue) {
        int x = bucket(dataValue);
        int t = (int) (AT * Math.pow(x, 3) + BT * Math.pow(x, 2) + CT * x + DT);
        return Math.max(T_MIN, t);
    }

    public static int powerPerTick(int fluxValue) {
        int x = bucket(fluxValue);
        return (int) (APwr * Math.pow(x, 3) + BPwr * Math.pow(x, 2) + CPwr * x + DPwr);
    }


    public static int timeTicks(DataFluxPair dataFlux) {
        return timeTicks(dataFlux.data());
    }

    public static int powerPerTick(DataFluxPair dataFlux) {
        return powerPerTick(dataFlux.flux());
    }
}
