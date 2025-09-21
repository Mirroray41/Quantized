package net.zapp.quantized.item;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.item.custom.CraftingTool;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Quantized.MOD_ID);

    public static final DeferredItem<Item> QUANTUM_MATTER = ITEMS.registerItem("quantum_matter",
            Item::new, new Item.Properties());

    public static final DeferredItem<Item> Q_BIT = ITEMS.registerItem("q_bit",
            Item::new, new Item.Properties());

    public static final DeferredItem<Item> Q_BYTE = ITEMS.registerItem("q_byte",
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
