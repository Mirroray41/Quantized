package net.zapp.quantized.core.init;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.content.item.custom.CraftingTool;
import net.zapp.quantized.content.item.custom.drive_item.DriveItem;
import net.zapp.quantized.content.item.custom.drive_item.DriveRecord;

import java.lang.reflect.Array;
import java.util.List;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Quantized.MOD_ID);

    public static final DeferredItem<Item> Q_BIT = ITEMS.registerItem("q_bit",
            Item::new, new Item.Properties());

    public static final DeferredItem<Item> Q_BYTE = ITEMS.registerItem("q_byte",
            Item::new, new Item.Properties());

    public static final DeferredItem<Item> Q_BYTES_8 = ITEMS.registerItem("8_q_bytes",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> Q_BYTES_64 = ITEMS.registerItem("64_q_bytes",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> Q_BYTES_512 = ITEMS.registerItem("512_q_bytes",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> Q_BYTES_4K = ITEMS.registerItem("4_kq_bytes",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> Q_BYTES_32K = ITEMS.registerItem("32_kq_bytes",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> Q_BYTES_256K = ITEMS.registerItem("256_kq_bytes",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> Q_BYTES_2M = ITEMS.registerItem("2_mq_bytes",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> Q_BYTES_16M = ITEMS.registerItem("16_mq_bytes",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> Q_BYTES_128M = ITEMS.registerItem("128_mq_bytes",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> Q_BYTES_1G = ITEMS.registerItem("1_gq_bytes",
            Item::new, new Item.Properties());

    public static final DeferredItem<Item> DRIVE_8 = ITEMS.registerItem("8_qb_drive",
            properties -> new DriveItem(properties, 8, 2), new Item.Properties());
    public static final DeferredItem<Item> DRIVE_64 = ITEMS.registerItem("64_qb_drive",
            properties -> new DriveItem(properties, 64, 4), new Item.Properties());
    public static final DeferredItem<Item> DRIVE_512 = ITEMS.registerItem("512_qb_drive",
            properties -> new DriveItem(properties, 512, 8), new Item.Properties());
    public static final DeferredItem<Item> DRIVE_4K = ITEMS.registerItem("4_kqb_drive",
            properties -> new DriveItem(properties, 4096, 16), new Item.Properties());
    public static final DeferredItem<Item> DRIVE_32K = ITEMS.registerItem("32_kqb_drive",
            properties -> new DriveItem(properties, 32768, 32), new Item.Properties());
    public static final DeferredItem<Item> DRIVE_256K = ITEMS.registerItem("256_kqb_drive",
            properties -> new DriveItem(properties, 262144, 64), new Item.Properties());
    public static final DeferredItem<Item> DRIVE_2M = ITEMS.registerItem("2_mqb_drive",
            properties -> new DriveItem(properties, 2097152, 128), new Item.Properties());
    public static final DeferredItem<Item> DRIVE_16M = ITEMS.registerItem("16_mqb_drive",
            properties -> new DriveItem(properties, 16777216, 256), new Item.Properties());
    public static final DeferredItem<Item> DRIVE_128M = ITEMS.registerItem("128_mqb_drive",
            properties -> new DriveItem(properties, 134217728, 512), new Item.Properties());
    public static final DeferredItem<Item> DRIVE_1G = ITEMS.registerItem("1_gqb_drive",
            properties -> new DriveItem(properties, 1073741824, 1024), new Item.Properties());

    public static final DeferredItem<Item> DRIVE_CASING = ITEMS.registerItem("drive_casing",
            Item::new, new Item.Properties());

    public static final DeferredItem<Item> STEEL_INGOT = ITEMS.registerItem("steel_ingot",
            Item::new, new Item.Properties());

    public static final DeferredItem<Item> STEEL_NUGGET = ITEMS.registerItem("steel_nugget",
            Item::new, new Item.Properties());

    public static final DeferredItem<Item> STEEL_GEAR = ITEMS.registerItem("steel_gear",
            Item::new, new Item.Properties());

    public static final DeferredItem<Item> STEEL_PLATE = ITEMS.registerItem("steel_plate",
            Item::new, new Item.Properties());

    public static final DeferredItem<Item> STEEL_ROD = ITEMS.registerItem("steel_rod",
            Item::new, new Item.Properties());


    public static final DeferredItem<Item> MALLET = ITEMS.registerItem("mallet",
            CraftingTool::new, new  Item.Properties().durability(64));

    public static final DeferredItem<Item> WIRE_CUTTERS = ITEMS.registerItem("wire_cutters",
            CraftingTool::new, new Item.Properties().durability(64));

    public static final DeferredItem<Item> COPPER_WIRE = ITEMS.registerItem("copper_wire",
            Item::new, new Item.Properties());

    public static final DeferredItem<Item> INDUCTOR = ITEMS.registerItem("inductor",
            Item::new, new Item.Properties());

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
