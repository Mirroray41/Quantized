package net.zapp.quantized.content.blocks.quantum_destabilizer;

import net.zapp.quantized.core.utils.DataFluxPair;

public class ProcessingCurves {
    private static final int T0 = 60;   // base ticks
    private static final int AT = 2;    // quad weight on data bucket
    private static final int BT = 3;    // linear weight on data bucket
    private static final int T_MIN = 10;  // floor so it never goes 0

    private static final int P0 = 300;  // base power in the high hundreds (late-game feel)
    private static final int AP = 80;   // quad weight on flux bucket (aggressive early)
    private static final int BP = 120;  // linear weight on flux bucket



    private static int bucket(int x) {
        return (x <= 0) ? 0 : 1 + (31 - Integer.numberOfLeadingZeros(x));
    }

    public static int timeTicks(int dataValue) {
        int b = bucket(dataValue);
        int t = T0 + AT * b * b + BT * b;
        return Math.max(T_MIN, t);
    }

    public static int powerPerTick(int fluxValue) {
        int b = bucket(fluxValue);

        return P0 + AP * b * b + BP * b;
    }

    public static int timeTicks(DataFluxPair dataFlux) {
        int b = bucket(dataFlux.data());
        int t = T0 + AT * b * b + BT * b;
        return Math.max(T_MIN, t);
    }

    public static int powerPerTick(DataFluxPair dataFlux) {
        int b = bucket(dataFlux.flux());

        return P0 + AP * b * b + BP * b;
    }
}
