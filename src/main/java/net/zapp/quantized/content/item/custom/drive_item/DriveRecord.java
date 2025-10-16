package net.zapp.quantized.content.item.custom.drive_item;

import net.minecraft.world.item.Item;

import java.util.Arrays;

public record DriveRecord(int capacity, int maxSizePerItem, int dataUsed, String[] items, int count) {

    public static DriveRecord blank() { return new DriveRecord(8, 2, 0, new String[0], 0); }
    public static boolean isBlank(DriveRecord dr) { return dr.equals(blank()); }

    public boolean canInsert(net.zapp.quantized.core.utils.DataFluxPair df) {
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
