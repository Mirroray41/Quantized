package net.zapp.quantized.core.init;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.content.item.custom.CraftingTool;
import net.zapp.quantized.content.item.custom.drive_item.DriveItem;
import net.zapp.quantized.content.item.custom.drive_item.DriveRecord;
import net.zapp.quantized.content.item.custom.drive_item.SingularityDriveItem;
import net.zapp.quantized.content.item.custom.upgrade.UpgradeCardItem;
import net.zapp.quantized.content.item.custom.upgrade.UpgradeType;

import java.lang.reflect.Array;
import java.util.ArrayList;
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
            properties -> new DriveItem(properties, 8, 4), new Item.Properties());
    public static final DeferredItem<Item> DRIVE_64 = ITEMS.registerItem("64_qb_drive",
            properties -> new DriveItem(properties, 64, 16), new Item.Properties());
    public static final DeferredItem<Item> DRIVE_512 = ITEMS.registerItem("512_qb_drive",
            properties -> new DriveItem(properties, 512, 64), new Item.Properties());
    public static final DeferredItem<Item> DRIVE_4K = ITEMS.registerItem("4_kqb_drive",
            properties -> new DriveItem(properties, 4096, 256), new Item.Properties());
    public static final DeferredItem<Item> DRIVE_32K = ITEMS.registerItem("32_kqb_drive",
            properties -> new DriveItem(properties, 32768, 1024), new Item.Properties());
    public static final DeferredItem<Item> DRIVE_256K = ITEMS.registerItem("256_kqb_drive",
            properties -> new DriveItem(properties, 262144, 4096), new Item.Properties());
    public static final DeferredItem<Item> DRIVE_2M = ITEMS.registerItem("2_mqb_drive",
            properties -> new DriveItem(properties, 2097152, 16384), new Item.Properties());
    public static final DeferredItem<Item> DRIVE_16M = ITEMS.registerItem("16_mqb_drive",
            properties -> new DriveItem(properties, 16777216, 65536), new Item.Properties());
    public static final DeferredItem<Item> DRIVE_128M = ITEMS.registerItem("128_mqb_drive",
            properties -> new DriveItem(properties, 134217728, 262144), new Item.Properties());
    public static final DeferredItem<Item> DRIVE_1G = ITEMS.registerItem("1_gqb_drive",
            properties -> new DriveItem(properties, 1073741824, 1048576), new Item.Properties());

    public static final DeferredItem<Item> SINGULARITY_DRIVE = ITEMS.registerItem("singularity_drive",
            SingularityDriveItem::new, new Item.Properties());

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

    public static final DeferredItem<Item> REPULSION_CORE = ITEMS.registerItem("repulsion_core",
            Item::new, new Item.Properties());

    public static final DeferredItem<Item> ATTRACTION_CORE = ITEMS.registerItem("attraction_core",
            Item::new, new Item.Properties());

    public static final DeferredItem<Item> FLUX_TANK = ITEMS.registerItem("flux_tank",
            Item::new, new Item.Properties());

    public static final DeferredItem<Item> CARBON_SILICON_BATTERY = ITEMS.registerItem("carbon_silicon_battery",
            Item::new, new Item.Properties());

    // ---- Upgrade cards: 5 tiers per type. value = stat multiplier. ----
    public static final double[] SPEED_VALUES = {1.5, 2.0, 3.0, 4.0, 6.0};
    public static final double[] EFFICIENCY_VALUES = {1.5, 2.0, 3.0, 4.0, 6.0};
    public static final double[] OUTPUT_VALUES = {2.0, 3.0, 4.0, 6.0, 8.0};

    /** The q-bit tier consumed to craft each upgrade tier (index 0 = tier 1). */
    public static final List<DeferredItem<Item>> UPGRADE_TIER_QBIT = List.of(
            Q_BYTE, Q_BYTES_8, Q_BYTES_512, Q_BYTES_32K, Q_BYTES_2M);

    public static final List<DeferredItem<Item>> SPEED_UPGRADES =
            registerUpgradeCards("speed_upgrade", UpgradeType.SPEED, SPEED_VALUES);
    public static final List<DeferredItem<Item>> EFFICIENCY_UPGRADES =
            registerUpgradeCards("efficiency_upgrade", UpgradeType.EFFICIENCY, EFFICIENCY_VALUES);
    public static final List<DeferredItem<Item>> OUTPUT_UPGRADES =
            registerUpgradeCards("output_upgrade", UpgradeType.OUTPUT, OUTPUT_VALUES);

    private static List<DeferredItem<Item>> registerUpgradeCards(String baseName, UpgradeType type, double[] values) {
        List<DeferredItem<Item>> list = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            final int tier = i + 1;
            final double value = values[i];
            list.add(ITEMS.registerItem(baseName + "_" + tier,
                    props -> new UpgradeCardItem(props, type, tier, value), new Item.Properties()));
        }
        return list;
    }


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
