package net.zapp.quantized.item;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zapp.quantized.Quantized;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Quantized.MOD_ID);

    public static final DeferredItem<Item> QUANTUM_MATTER = ITEMS.registerItem("quantum_matter",
            Item::new, new Item.Properties());

    public static final DeferredItem<Item> STEEL_INGOT = ITEMS.registerItem("steel_ingot",
            Item::new, new Item.Properties());

    public static final DeferredItem<Item> STEEL_NUGGET = ITEMS.registerItem("steel_nugget",
            Item::new, new Item.Properties());

    public static final DeferredItem<Item> STEEL_GEAR = ITEMS.registerItem("steel_gear",
            Item::new, new Item.Properties());


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
