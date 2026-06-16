package net.zapp.quantized.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zapp.quantized.core.fluxdata.FluxDataFixerUpper;
import net.zapp.quantized.core.fluxdata.FluxDataRecipeComputer;
import net.zapp.quantized.core.utils.DataFluxPair;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code /quantized} debugging command — makes the value system inspectable in-game.
 *
 * <ul>
 *   <li>{@code /quantized value} – value of the item in your main hand, with its source.</li>
 *   <li>{@code /quantized value <item>} – value of a specified item, with its source.</li>
 *   <li>{@code /quantized unpriced [limit]} – how many items have no value, plus a sample.</li>
 *   <li>{@code /quantized recompute} – force a re-derivation (op only).</li>
 * </ul>
 */
public final class QuantizedCommand {
    private QuantizedCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("quantized")
                .then(Commands.literal("value")
                        .executes(c -> valueOfHeld(c.getSource()))
                        .then(Commands.argument("item", ItemArgument.item(ctx))
                                .executes(c -> valueOf(c.getSource(),
                                        ItemArgument.getItem(c, "item").getItem()))))
                .then(Commands.literal("unpriced")
                        .executes(c -> unpriced(c.getSource(), 20))
                        .then(Commands.argument("limit", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 1000))
                                .executes(c -> unpriced(c.getSource(),
                                        com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "limit")))))
                .then(Commands.literal("recompute")
                        .requires(src -> src.hasPermission(2))
                        .executes(c -> recompute(c.getSource())));
        dispatcher.register(root);
    }

    private static int valueOfHeld(CommandSourceStack source) {
        var player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Specify an item: /quantized value <item>"));
            return 0;
        }
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            source.sendFailure(Component.literal("You are not holding anything."));
            return 0;
        }
        return valueOf(source, held.getItem());
    }

    private static int valueOf(CommandSourceStack source, Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        DataFluxPair pair = FluxDataFixerUpper.getDataFlux(item);
        if (!DataFluxPair.isValid(pair)) {
            source.sendSuccess(() -> Component.literal(id + ": no value (unpriced)"), false);
            return 0;
        }
        String src;
        if (FluxDataFixerUpper.isAnchored(item)) {
            src = "config anchor";
        } else {
            ResourceLocation recipe = FluxDataFixerUpper.getDerivedSource(item);
            src = recipe != null ? "derived from recipe " + recipe : "derived";
        }
        source.sendSuccess(() -> Component.literal(
                id + " -> data " + pair.data() + ", flux " + pair.flux() + "  (" + src + ")"), false);
        return 1;
    }

    private static int unpriced(CommandSourceStack source, int limit) {
        List<ResourceLocation> missing = new ArrayList<>();
        int total = 0;
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == net.minecraft.world.item.Items.AIR) continue;
            if (!DataFluxPair.isValid(FluxDataFixerUpper.getDataFlux(item))) {
                total++;
                if (missing.size() < limit) missing.add(BuiltInRegistries.ITEM.getKey(item));
            }
        }
        final int count = total;
        source.sendSuccess(() -> Component.literal(
                count + " item(s) have no value. Showing " + missing.size() + ":"), false);
        missing.forEach(rl -> source.sendSuccess(() -> Component.literal("  " + rl), false));
        return count;
    }

    private static int recompute(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Recomputing Quantized values..."), true);
        FluxDataRecipeComputer.recomputeDerived();
        source.sendSuccess(() -> Component.literal("Done. See log for details."), true);
        return 1;
    }
}
