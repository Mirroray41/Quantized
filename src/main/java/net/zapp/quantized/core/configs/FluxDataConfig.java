package net.zapp.quantized.core.configs;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.fml.loading.FMLPaths;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.core.fluxdata.FluxDataFixerUpper;
import net.zapp.quantized.core.utils.DataFluxPair;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class FluxDataConfig {
    private static boolean isLoaded = false;
    private static final Map<ResourceLocation, DataFluxPair> ITEMS = new ConcurrentHashMap<>();
    private static final Map<TagKey<Item>, DataFluxPair> TAGS = new LinkedHashMap<>();

    public static void load() {
        if (isLoaded) return;
        Path configDir = FMLPaths.CONFIGDIR.get();
        Path dir = configDir.resolve(Quantized.MOD_ID);
        try  {
            Files.createDirectories(dir);
        } catch (Exception ignored) {}
        ITEMS.clear();
        TAGS.clear();

        Path file = configDir.resolve("quantized").resolve("default_flux_data.toml");
        CommentedFileConfig config = CommentedFileConfig.builder(file)
                .autosave()
                .preserveInsertionOrder()
                .sync()
                .build();

        config.load();

        CommentedConfig items = config.get("items");
        if (items == null) {
            items = CommentedConfig.inMemory();
            config.set("items", items);
        }
        CommentedConfig tags = config.get("tags");
        if (tags == null) {
            tags = CommentedConfig.inMemory();
            config.set("tags", tags);
        }

        prepopulateConfigIfAbsent(config);

        ITEMS.clear();
        TAGS.clear();
        readItemsFromConfig(config);
        readTagsFromConfig(config);

        config.save();
        isLoaded = true;
    }

    public static Map<ResourceLocation, DataFluxPair> itemMapView() {
        return Collections.unmodifiableMap(ITEMS);
    }
    public static Map<TagKey<Item>, DataFluxPair> tagMapView() {
        return Collections.unmodifiableMap(TAGS);
    }

    private static void readItemsFromConfig(CommentedFileConfig config) {
        CommentedConfig items = config.get("items");
        for (var entry : items.entrySet()) {
            String key = entry.getKey().trim();
            DataFluxPair pair = asNumberPair(entry.getValue());
            var id = ResourceLocation.tryParse(key);
            if (id != null && BuiltInRegistries.ITEM.containsKey(id) && pair != null) {
                ITEMS.put(id, pair);
            }
        }
    }

    private static void readTagsFromConfig(CommentedFileConfig config) {
        CommentedConfig tags = config.get("tags");
        for (var entry : tags.entrySet()) {
            String rawKey = entry.getKey();
            String norm  = normalizeTagKey(rawKey);
            DataFluxPair pair = asNumberPair(entry.getValue());
            if (pair == null) continue;

            var tagId = ResourceLocation.parse(norm.startsWith("#") ? norm.substring(1) : norm);
            var tagKey = TagKey.create(Registries.ITEM, tagId);
            TAGS.put(tagKey, pair);
        }
    }

    private static void prepopulateConfigIfAbsent(CommentedFileConfig config) {
        CommentedConfig items = config.get("items");
        CommentedConfig tags  = config.get("tags");

        java.util.function.BiConsumer<String, DataFluxPair> itemDef = (id, pair) -> {
            if (!items.contains(id)) items.set(id, java.util.List.of(pair.data(), pair.flux()));
        };
        java.util.function.BiConsumer<String, DataFluxPair> tagDef = (tagKey, pair) -> {
            String norm = normalizeTagKey(tagKey);
            if (!tags.contains(norm)) tags.set(norm, java.util.List.of(pair.data(), pair.flux()));
        };

        addPredefinedItems(itemDef);
        addPredefinedTags(tagDef);
    }



    private static void addPredefinedItems(BiConsumer<String, DataFluxPair> itemDef) {
        itemDef.accept("minecraft:netherite_upgrade_smithing_template", new DataFluxPair(160, 57345));
        itemDef.accept("minecraft:basalt", new DataFluxPair(1, 1));
        itemDef.accept("minecraft:blackstone", new DataFluxPair(1, 1));
        itemDef.accept("minecraft:bamboo", new DataFluxPair(1, 2));
        itemDef.accept("minecraft:clay_ball", new DataFluxPair(4, 16));
        itemDef.accept("minecraft:prismarine_shard", new DataFluxPair(4, 16));
        itemDef.accept("minecraft:melon_slice", new DataFluxPair(1, 1));
        itemDef.accept("minecraft:apple", new DataFluxPair(2, 4));
        itemDef.accept("minecraft:chorus_fruit", new DataFluxPair(4, 16));
        itemDef.accept("minecraft:enchanted_golden_apple", new DataFluxPair(18, 73732));
        itemDef.accept("minecraft:gilded_blackstone", new DataFluxPair(4, 16));
        itemDef.accept("minecraft:exposed_copper", new DataFluxPair(4, 1152));
        itemDef.accept("minecraft:weathered_copper", new DataFluxPair(4, 1152));
        itemDef.accept("minecraft:oxidized_copper", new DataFluxPair(4, 1152));
        itemDef.accept("minecraft:honeycomb", new DataFluxPair(2, 16));
        itemDef.accept("minecraft:ice", new DataFluxPair(1, 4));
        itemDef.accept("minecraft:snow_block", new DataFluxPair(1, 1));
        itemDef.accept("minecraft:pointed_dripstone", new DataFluxPair(1, 1));
        itemDef.accept("minecraft:budding_amethyst", new DataFluxPair(4, 16));
        itemDef.accept("minecraft:small_amethyst_bud", new DataFluxPair(1, 4));
        itemDef.accept("minecraft:medium_amethyst_bud", new DataFluxPair(2, 8));
        itemDef.accept("minecraft:large_amethyst_bud", new DataFluxPair(3, 12));
        itemDef.accept("minecraft:amethyst_cluster", new DataFluxPair(4, 16));
        itemDef.accept("minecraft:mangrove_roots", new DataFluxPair(1, 1));
        itemDef.accept("minecraft:brown_mushroom_block", new DataFluxPair(4, 16));
        itemDef.accept("minecraft:red_mushroom_block", new DataFluxPair(4, 16));
        itemDef.accept("minecraft:kelp", new DataFluxPair(1, 1));
        itemDef.accept("minecraft:sponge", new DataFluxPair(4, 8));
        itemDef.accept("minecraft:wet_sponge", new DataFluxPair(4, 8));
        itemDef.accept("minecraft:slime_ball", new DataFluxPair(4, 8));
        itemDef.accept("minecraft:honey_block", new DataFluxPair(4, 16));
        itemDef.accept("minecraft:soul_sand", new DataFluxPair(2, 4));
        itemDef.accept("minecraft:soul_soil", new DataFluxPair(2, 4));
        itemDef.accept("minecraft:cobweb", new DataFluxPair(2, 4));
        itemDef.accept("minecraft:blaze_rod", new DataFluxPair(8, 64));
        itemDef.accept("minecraft:flint", new DataFluxPair(1, 3));
        itemDef.accept("minecraft:ghast_tear", new DataFluxPair(4, 16));
        itemDef.accept("minecraft:obsidian", new DataFluxPair(4, 64));
        itemDef.accept("minecraft:nether_star", new DataFluxPair(64, 65536));
        itemDef.accept("minecraft:glow_ink_sack", new DataFluxPair(2, 8));
        itemDef.accept("minecraft:ink_sack", new DataFluxPair(2, 8));
    }

    private static void addPredefinedTags(BiConsumer<String, DataFluxPair> tagDef) {
        tagDef.accept("#minecraft:logs", new DataFluxPair(4, 16));
        tagDef.accept("#minecraft:dirt", new DataFluxPair(1, 1));
        tagDef.accept("#minecraft:wool", new DataFluxPair(2, 8));
        tagDef.accept("#minecraft:terracotta", new DataFluxPair(2, 8));
        tagDef.accept("#minecraft:leaves", new DataFluxPair(1, 1));
        tagDef.accept("#minecraft:coral_blocks", new DataFluxPair(1, 1));
        tagDef.accept("#minecraft:corals", new DataFluxPair(1, 1));
        tagDef.accept("#c:sands", new DataFluxPair(1, 1));
        tagDef.accept("#c:end_stones", new DataFluxPair(2, 2));
        tagDef.accept("#c:dyes", new DataFluxPair(2, 4));
        tagDef.accept("#c:mushrooms", new DataFluxPair(2, 8));
        tagDef.accept("#c:flowers", new DataFluxPair(2, 4));

        tagDef.accept("#c:strings", new DataFluxPair(1, 2));
        tagDef.accept("#c:gravels", new DataFluxPair(1, 1));

        tagDef.accept("#c:ores/copper", new DataFluxPair(4, 32));
        tagDef.accept("#c:ores/iron", new DataFluxPair(8, 256));
        tagDef.accept("#c:ores/gold", new DataFluxPair(16, 1024));
        tagDef.accept("#c:ores/coal", new DataFluxPair(4, 128));
        tagDef.accept("#c:ores/quartz", new DataFluxPair(4, 64));
        tagDef.accept("#c:ores/diamond", new DataFluxPair(32, 8192));
        tagDef.accept("#c:ores/emerald", new DataFluxPair(32, 8192));
        tagDef.accept("#c:ores/netherite_scrap", new DataFluxPair(64, 16384));
        tagDef.accept("#c:ores/redstone", new DataFluxPair(4, 320));

        tagDef.accept("#c:dusts/redstone", new DataFluxPair(4, 64));
        tagDef.accept("#c:dusts/glowstone", new DataFluxPair(4, 32));


        tagDef.accept("#c:ingots/copper", new DataFluxPair(4, 128));

        tagDef.accept("#c:raw_materials/copper", new DataFluxPair(4, 32));
        tagDef.accept("#c:raw_materials/iron", new DataFluxPair(8, 256));
        tagDef.accept("#c:raw_materials/gold", new DataFluxPair(16, 1024));


        tagDef.accept("#c:gems/diamond", new DataFluxPair(32, 8192));
        tagDef.accept("#c:gems/prismarine", new DataFluxPair(4, 32));
        tagDef.accept("#c:gems/amethyst", new DataFluxPair(4, 32));

        tagDef.accept("#c:stones", new DataFluxPair(1, 1));
        tagDef.accept("#c:cobblestones", new DataFluxPair(1, 1));
        tagDef.accept("#c:netherracks", new DataFluxPair(1, 1));

        tagDef.accept("#c:buckets/water", new DataFluxPair(16, 1536));
        tagDef.accept("#c:buckets/lava", new DataFluxPair(16, 1536));
        tagDef.accept("#c:buckets/milk", new DataFluxPair(16, 1536));
        tagDef.accept("#c:buckets/powder_snow", new DataFluxPair(16, 1536));
        tagDef.accept("#c:buckets/entity_water", new DataFluxPair(16, 1536));

        tagDef.accept("#c:clumps/resin", new DataFluxPair(1, 4));

        tagDef.accept("#c:crops/wheat", new DataFluxPair(1, 4));
        tagDef.accept("#c:crops/beetroot", new DataFluxPair(1, 4));
        tagDef.accept("#c:crops/cactus", new DataFluxPair(1, 4));
        tagDef.accept("#c:crops/carrot", new DataFluxPair(1, 4));
        tagDef.accept("#c:crops/cocoa_bean", new DataFluxPair(1, 4));
        tagDef.accept("#c:crops/nether_wart", new DataFluxPair(1, 4));
        tagDef.accept("#c:crops/pumpkin", new DataFluxPair(1, 4));
        tagDef.accept("#c:crops/potato", new DataFluxPair(1, 4));
        tagDef.accept("#c:crops/sugar_cane", new DataFluxPair(1, 4));

    }

    private static DataFluxPair asNumberPair(Object val) {
        if (val instanceof java.util.List<?> list && list.size() >= 2
                && list.get(0) instanceof Number n0 && list.get(1) instanceof Number n1) {
            return new DataFluxPair(n0.intValue(), n1.intValue());
        }
        if (val instanceof String s) {
            String cleaned = s.replace("(", "").replace(")", "");
            String[] parts = cleaned.split(",");
            if (parts.length >= 2) {
                try { return new DataFluxPair(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim())); }
                catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }

    public static String normalizeTagKey(String key) {
        String s = key.trim();
        if (!s.startsWith("#")) s = "#" + s;
        int colon = s.indexOf(':');
        if (colon > 0 && colon + 1 < s.length()) {
            String ns = s.substring(1, colon);
            String path = s.substring(colon + 1);
            if (path.contains("_") && !path.contains("/")) path = path.replace('_', '/');
            s = "#" + ns + ":" + path;
        }
        return s;
    }

    public static void overrideItem(ResourceLocation id, DataFluxPair v) {
        ITEMS.put(id, v);
    }

    public static void overrideTag(TagKey<Item> tag, DataFluxPair v) {
        TAGS.put(tag, v);
    }
}
