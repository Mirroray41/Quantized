package net.zapp.quantized;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.fml.loading.FMLPaths;
import net.zapp.quantized.api.utils.FluxDataPair;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FluxDataConfig {
    private static boolean isLoaded = false;
    // Keep ITEMS concurrent; preserve TAGS insertion order to define priority
    private static final Map<ResourceLocation, FluxDataPair> ITEMS = new ConcurrentHashMap<>();
    private static final Map<TagKey<Item>, FluxDataPair> TAGS = new LinkedHashMap<>();

    public static void load() {
        if (isLoaded) return;
        Path configDir = FMLPaths.CONFIGDIR.get();
        Path dir = configDir.resolve(Quantized.MOD_ID);
        try  {
            Files.createDirectories(dir);
        } catch (Exception ignored) {}

        ITEMS.clear();
        TAGS.clear();

        Path file = configDir.resolve("quantized").resolve("flux_data.toml");
        CommentedFileConfig config = CommentedFileConfig.builder(file)
                .autosave()
                .preserveInsertionOrder()
                .sync()
                .build();

        config.load();
        if (config.isNull("items"))
            config.set("items", CommentedConfig.inMemory());
        if (config.isNull("tags"))
            config.set("tags", CommentedConfig.inMemory());

        // ----- items -----
        CommentedConfig items = config.get("items");
        for (var entry : items.entrySet()) {
            String key = entry.getKey().trim();
            FluxDataPair pair = asNumberPair(entry.getValue());
            var id = ResourceLocation.tryParse(key);
            if (id != null && BuiltInRegistries.ITEM.containsKey(id) && pair != null) {
                ITEMS.put(id, pair);
            }
        }

        // ----- tags -----
        CommentedConfig tags = config.get("tags");
        for (var entry : tags.entrySet()) {
            String rawKey = entry.getKey();                 // <-- use KEY, not value
            String norm = normalizeTagKey(rawKey);
            FluxDataPair pair = asNumberPair(entry.getValue());
            if (pair == null) continue;

            ResourceLocation tagId = ResourceLocation.parse(norm.startsWith("#") ? norm.substring(1) : norm);
            TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagId);

            // LinkedHashMap preserves the file order -> defines tag priority
            TAGS.put(tagKey, pair);
        }

        config.save();
        isLoaded = true;
    }

    /** Direct item entry only (no fallback). */
    public static FluxDataPair getValuesFor(ResourceLocation id) {
        return ITEMS.get(id);
    }

    /** First tag value that matches (by config order). */
    public static FluxDataPair getForTags(Set<TagKey<Item>> itemTags) {
        for (var k : TAGS.keySet()) {
            if (itemTags.contains(k)) {
                return TAGS.get(k);
            }
        }
        return null;
    }

    /** On-the-fly fallback: item id -> else first matching tag (by config order) -> default. */
    public static FluxDataPair getWithFallback(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        if (id != null) {
            FluxDataPair direct = ITEMS.get(id);
            if (direct != null) return direct;
        }
        // Check tags in config order
        var holder = item.builtInRegistryHolder();
        for (var e : TAGS.entrySet()) {
            if (holder.is(e.getKey())) {
                return e.getValue();
            }
        }
        return new FluxDataPair(0, 0);
    }

    public static Map<ResourceLocation, FluxDataPair> itemMapView() {
        return Collections.unmodifiableMap(ITEMS);
    }

    public static Map<TagKey<Item>, FluxDataPair> tagMapView() {
        return Collections.unmodifiableMap(TAGS);
    }

    @SuppressWarnings("unchecked")
    private static FluxDataPair asNumberPair(Object val) {
        // TOML list: [1, 30]
        if (val instanceof List<?> list && list.size() >= 2 && list.get(0) instanceof Number n0 && list.get(1) instanceof Number n1) {
            return new FluxDataPair(n0.intValue(), n1.intValue());
        }
        // Already an IntPair (unlikely from config, but harmless)
        if (val instanceof FluxDataPair pair) {
            return pair;
        }
        // String forms: "1, 30" or "(1, 30)"
        if (val instanceof String s) {
            String cleaned = s.replace("(", "").replace(")", "");
            String[] parts = cleaned.split(",");
            if (parts.length >= 2) {
                try {
                    return new FluxDataPair(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
                } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }

    /**
     * Accepts:
     *  "#forge:ingots/steel" (preferred)
     *  "forge:ingots/steel"
     *  "forge:ingots_steel"  -> normalized to "forge:ingots/steel"
     */
    private static String normalizeTagKey(String key) {
        String s = key.trim();
        if (!s.startsWith("#")) s = "#" + s;
        int colon = s.indexOf(':');
        if (colon > 0 && colon + 1 < s.length()) {
            String ns = s.substring(1, colon); // without '#'
            String path = s.substring(colon + 1);
            if (path.contains("_") && !path.contains("/")) {
                path = path.replace('_', '/');
            }
            s = "#" + ns + ":" + path;
        }
        return s;
    }
}
