package net.zapp.quantized.core.fluxdata;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.core.configs.FluxDataConfig;
import net.zapp.quantized.core.fluxdata.solver.Conversion;
import net.zapp.quantized.core.fluxdata.solver.FluxValue;
import net.zapp.quantized.core.fluxdata.solver.RecipeConversionMapper;
import net.zapp.quantized.core.fluxdata.solver.ValueSolver;
import net.zapp.quantized.core.utils.DataFluxPair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Drives value derivation: read recipes into {@link Conversion}s, seed the config anchors, run the
 * {@link ValueSolver}, and push the result into {@link FluxDataFixerUpper}'s derived layer.
 *
 * <p>Runs on server start and again after any {@code /reload} (triggered by
 * {@link FluxDataJsonLoader} once it has rebuilt the BASE layer), so derived values are never left
 * stale or wiped — the bug in the old single-pass, server-start-only design.
 */
public final class FluxDataRecipeComputer {
    private FluxDataRecipeComputer() {}

    private static volatile MinecraftServer server;

    public static void init() {
        NeoForge.EVENT_BUS.addListener(FluxDataRecipeComputer::onServerStarted);
        NeoForge.EVENT_BUS.addListener(FluxDataRecipeComputer::onServerStopped);
    }

    private static void onServerStarted(ServerStartedEvent e) {
        server = e.getServer();
        recomputeDerived();
    }

    private static void onServerStopped(ServerStoppedEvent e) {
        server = null;
    }

    /**
     * Rebuild the derived value layer from the current recipes + config anchors.
     * No-op if no server is running yet (initial datapack load seeds BASE; the subsequent
     * {@link ServerStartedEvent} performs the first derivation).
     */
    public static synchronized void recomputeDerived() {
        MinecraftServer srv = server;
        if (srv == null) return;

        Level level = srv.overworld();
        RecipeManager recipeManager = srv.getRecipeManager();

        try {
            List<Conversion> conversions = RecipeConversionMapper.build(recipeManager, level);
            Map<Item, FluxValue> anchors = buildAnchors();

            ValueSolver.Result result = ValueSolver.solve(conversions, anchors);
            FluxDataFixerUpper.setDerived(result.derived(), result.sources());
            reportExploits(result.exploits());
        } catch (Throwable t) {
            // Never let a derivation failure crash server start or wipe the existing derived layer.
            Quantized.LOGGER.error("[Quantized:Solver] Value derivation failed; keeping previous derived values.", t);
        }
    }

    /** Seed every item that has a config item value or a tag value as a fixed anchor. */
    private static Map<Item, FluxValue> buildAnchors() {
        Map<Item, FluxValue> anchors = new HashMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (!FluxDataFixerUpper.hasBaseValue(item)) continue;
            DataFluxPair base = FluxDataFixerUpper.getDataFlux(item);
            if (DataFluxPair.isValid(base)) anchors.put(item, FluxValue.of(base));
        }
        return anchors;
    }

    private static void reportExploits(Map<Item, ValueSolver.ExploitInfo> exploits) {
        if (exploits.isEmpty()) return;
        Quantized.LOGGER.warn("[Quantized:Solver] {} anchored value(s) can be crafted for less flux "
                + "than their configured value (possible duplication exploit):", exploits.size());
        exploits.forEach((item, info) -> Quantized.LOGGER.warn(
                "  - {}: configured flux {} but a recipe yields it for {}",
                BuiltInRegistries.ITEM.getKey(item), info.anchor().flux(), info.cheapest().flux()));
    }
}
