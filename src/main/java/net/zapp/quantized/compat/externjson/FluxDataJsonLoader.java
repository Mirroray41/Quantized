package net.zapp.quantized.compat.externjson;

import com.google.gson.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.neoforged.fml.loading.FMLPaths;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.core.configs.FluxDataConfig;
import net.zapp.quantized.core.utils.DataFluxPair;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

public final class FluxDataJsonLoader implements PreparableReloadListener {
    private static final Gson GSON = new GsonBuilder().setStrictness(Strictness.LENIENT).create();
    private static final String JSON_DIR = "flux_data";
    private static final String ITEMS_KEY = "items";
    private static final String TAGS_KEY  = "tags";
    private static final String PRIORITY  = "priority";

    private static final int DEFAULT_PACK_PRIORITY = 0;
    private static final int DEFAULT_CONFIG_FILE_PRIORITY = 10_000;

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager rm, Executor bg, Executor game) {
        CompletableFuture<List<Pack>> prepared = CompletableFuture.supplyAsync(() -> {
            List<Pack> packs = new ArrayList<>();
            loadDatapackJson(rm, packs);
            loadExternalConfigJson(packs);
            sortPacks(packs);
            return packs;
        }, bg);

        return prepared.thenCompose(barrier::wait)
                .thenAcceptAsync(this::applyPacks, game);
    }

    private void loadDatapackJson(ResourceManager rm, List<Pack> out) {
        rm.listResources(JSON_DIR, rl -> rl.getPath().endsWith(".json")).forEach((id, res) -> {
            try (Reader r = res.openAsReader()) {
                JsonObject root = GSON.fromJson(r, JsonObject.class);
                int prio = getPriority(root, DEFAULT_PACK_PRIORITY);
                out.add(new Pack(prio, id, root));
            } catch (Exception ex) {
                warn("Failed datapack json {}: {}", id, ex.toString());
            }
        });
    }

    private void loadExternalConfigJson(List<Pack> out) {
        Path cfgRoot = FMLPaths.CONFIGDIR.get().resolve("quantized").resolve(JSON_DIR);
        if (!Files.isDirectory(cfgRoot)) return;

        try (Stream<Path> stream = Files.walk(cfgRoot)) {
            stream.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                try (Reader r = Files.newBufferedReader(p)) {
                    JsonObject root = GSON.fromJson(r, JsonObject.class);
                    int prio = getPriority(root, DEFAULT_CONFIG_FILE_PRIORITY);
                    ResourceLocation id = ResourceLocation.fromNamespaceAndPath("config", p.getFileName().toString());
                    out.add(new Pack(prio, id, root));
                } catch (Exception ex) {
                    warn("Failed config json {}: {}", p, ex.toString());
                }
            });
        } catch (Exception ignored) {
        }
    }

    private int getPriority(JsonObject root, int defaultValue) {
        return root.has(PRIORITY) ? root.get(PRIORITY).getAsInt() : defaultValue;
    }

    private void sortPacks(List<Pack> packs) {
        packs.sort(Comparator.comparingInt(Pack::prio)
                .thenComparing(p -> p.id().toString()));
    }

    public static void ensureExternJsonDirectoryExists() {
        Path cfgRoot = FMLPaths.CONFIGDIR.get().resolve("quantized").resolve(JSON_DIR);
        if (Files.notExists(cfgRoot)) {
            try {
                Files.createDirectory(cfgRoot);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void applyPacks(List<Pack> packs) {
        for (Pack p : packs) {
            applyOnePack(p);
        }
        info("Applied {} pack(s).", packs.size());
    }

    private void applyOnePack(Pack p) {
        JsonObject root = p.root();
        applyItems(root, p);
        applyTags(root, p);
    }

    private void applyItems(JsonObject root, Pack p) {
        if (!root.has(ITEMS_KEY) || !root.get(ITEMS_KEY).isJsonObject()) return;

        root.getAsJsonObject(ITEMS_KEY).entrySet().forEach(e -> {
            try {
                ResourceLocation rl = ResourceLocation.parse(e.getKey());
                DataFluxPair pair = parsePair(e.getValue());
                if (pair != null) {
                    FluxDataConfig.overrideItem(rl, pair);
                }
            } catch (Exception ex) {
                warn("Bad item key '{}' in {}", e.getKey(), p.id());
            }
        });
    }

    private void applyTags(JsonObject root, Pack p) {
        if (!root.has(TAGS_KEY) || !root.get(TAGS_KEY).isJsonObject()) return;

        root.getAsJsonObject(TAGS_KEY).entrySet().forEach(e -> {
            String normalized = normalizeTagKey(e.getKey());
            try {
                ResourceLocation rl = ResourceLocation.parse(normalized);
                DataFluxPair pair = parsePair(e.getValue());
                if (pair != null) {
                    FluxDataConfig.overrideTag(TagKey.create(Registries.ITEM, rl), pair);
                }
            } catch (Exception ex) {
                warn("Bad tag key '{}' in {}", normalized, p.id());
            }
        });
    }

    private static DataFluxPair parsePair(JsonElement el) {
        try {
            if (el.isJsonArray()) {
                var arr = el.getAsJsonArray();
                if (arr.size() >= 2) {
                    return new DataFluxPair(arr.get(0).getAsInt(), arr.get(1).getAsInt());
                }
            } else if (el.isJsonObject()) {
                var o = el.getAsJsonObject();
                if (o.has("data") && o.has("flux")) {
                    return new DataFluxPair(o.get("data").getAsInt(), o.get("flux").getAsInt());
                }
            } else if (el.isJsonPrimitive()) {
                String s = el.getAsString();
                String[] parts = s.replace("(", "").replace(")", "").split(",");
                if (parts.length >= 2) {
                    return new DataFluxPair(
                            Integer.parseInt(parts[0].trim()),
                            Integer.parseInt(parts[1].trim())
                    );
                }
            }
        } catch (Exception ignored) { }
        return null;
    }

    private static String normalizeTagKey(String key) {
        String k = key.startsWith("#") ? key.substring(1) : key;
        int colon = k.indexOf(':');
        if (colon > 0) {
            String ns = k.substring(0, colon);
            String path = k.substring(colon + 1).replace('_', '/');
            return ns + ":" + path;
        }
        return k;
    }

    private static void info(String msg, Object... args) {
        Quantized.LOGGER.info("[FluxJSON] {} {}", msg, args);
    }

    private static void warn(String msg, Object... args) {
        Quantized.LOGGER.warn("[FluxJSON] {} {}", msg, args);
    }
}
