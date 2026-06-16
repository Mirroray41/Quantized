package net.zapp.quantized.content.item.custom.drive_item;

import net.minecraft.world.item.Item;

import java.util.Arrays;

public record DriveRecord(int capacity, int maxSizePerItem, int dataUsed, String[] items, int count) {

    /** Sentinel {@code capacity} marking a singular drive: holds exactly one item of any size. */
    public static final int SINGULAR_CAPACITY = -1;

    public static DriveRecord blank() { return new DriveRecord(8, 2, 0, new String[0], 0); }
    public static boolean isBlank(DriveRecord dr) { return dr.equals(blank()); }

    /** A blank singular drive: stores a single item pattern regardless of its size. */
    public static DriveRecord singular() {
        return new DriveRecord(SINGULAR_CAPACITY, Integer.MAX_VALUE, 0, new String[0], 0);
    }

    public boolean isSingular() { return capacity == SINGULAR_CAPACITY; }

    public boolean canInsert(net.zapp.quantized.core.utils.DataFluxPair df) {
        // Singular drives accept exactly one item of any size: room only while still empty.
        if (isSingular()) return count == 0;
        if (dataUsed + df.data() >= capacity) return false;
        if (df.data() > maxSizePerItem) return false;
        return true;
    }

    public static String keyOf(Item item) { return item.toString(); }

    public boolean containsItemString(String itemStr) {
        if (itemStr == null) return false;
        for (String s : items) if (itemStr.equals(s)) return true;
        return false;
    }

    public DriveRecord withItemRemoved(String itemStr, int dfPerItem) {
        if (!containsItemString(itemStr)) return this;

        String[] newItems = Arrays.stream(items)
                .filter(s -> s != null && !s.equals(itemStr))
                .toArray(String[]::new);

        int newCount = Math.max(0, count - 1);
        int newDataUsed = Math.max(0, dataUsed - Math.max(0, dfPerItem));
        return new DriveRecord(capacity, maxSizePerItem, newDataUsed, newItems, newCount);
    }
}
