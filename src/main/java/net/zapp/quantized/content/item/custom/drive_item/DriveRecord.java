package net.zapp.quantized.content.item.custom.drive_item;

public record DriveRecord(int capacity, int maxSizePerItem, int dataUsed, String[] items, int count) {
}
