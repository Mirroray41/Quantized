package net.zapp.quantized.content.item.custom.upgrade;

import java.util.Locale;

/**
 * The three stats an upgrade card can improve. A machine accepts a subset of these and has exactly
 * one slot per accepted type, so the number of unique upgrades it can hold equals its slot count.
 */
public enum UpgradeType {
    /** Reduces processing time (ticks per operation). */
    SPEED,
    /** Reduces resource cost (FE per tick and/or flux per operation). */
    EFFICIENCY,
    /** Multiplies what the machine produces (items / flux / FE). */
    OUTPUT;

    public String lowerName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
