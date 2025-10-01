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

        readItemsFromConfig(config);
        readTagsFromConfig(config);

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
        itemDef.accept("minecraft:stick",      new DataFluxPair(1, 10));
        itemDef.accept("minecraft:oak_door",   new DataFluxPair(1, 30));
        itemDef.accept("minecraft:diamond",    new DataFluxPair(1, 200));
        itemDef.accept("minecraft:diamond_block", new DataFluxPair(9, 1800));
        itemDef.accept("minecraft:iron_ingot", new DataFluxPair(1, 100));
    }

    private static void addPredefinedTags(BiConsumer<String, DataFluxPair> tagDef) {
        tagDef.accept("#minecraft:planks",      new DataFluxPair(2, 50));
        tagDef.accept("#forge:ingots/iron",     new DataFluxPair(1, 100));
        tagDef.accept("#forge:ingots/steel",    new DataFluxPair(3, 1500));
        tagDef.accept("#c:ingots",              new DataFluxPair(1, 75));
        tagDef.accept("#c:stones",              new DataFluxPair(1, 1));
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
