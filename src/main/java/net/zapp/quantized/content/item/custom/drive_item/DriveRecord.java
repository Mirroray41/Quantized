package net.zapp.quantized.content.item.custom.drive_item;

import net.zapp.quantized.core.utils.DataFluxPair;

public record DriveRecord(int capacity, int maxSizePerItem, int dataUsed, String[] items, int count) {

    public static DriveRecord blank() {
        return new DriveRecord(8, 2, 0, new String[0], 0);
    }

    public boolean canInsert(DataFluxPair df) {
        if (dataUsed + df.data() >= capacity) return false;
        if (df.data() > maxSizePerItem) return false;
        return true;
    }
}
