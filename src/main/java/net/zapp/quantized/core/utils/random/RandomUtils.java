package net.zapp.quantized.core.utils.random;

import java.util.Random;

public class RandomUtils {
    private static final Random rng = new Random();

    public static boolean percentChance(double percent) {
        return percent >= rng.nextInt(1, 101);
    }
}
