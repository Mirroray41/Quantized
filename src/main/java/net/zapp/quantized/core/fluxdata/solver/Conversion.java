package net.zapp.quantized.core.fluxdata.solver;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.List;

/**
 * A recipe abstracted for value solving, independent of Minecraft's recipe types.
 *
 * <p>This mirrors ProjectE's "conversion" concept: a single rule that produces {@code outnumber}
 * copies of {@code output} from a list of {@link Slot}s. Reading recipes (see
 * {@link RecipeConversionMapper}) is fully decoupled from solving values (see {@link ValueSolver}).
 *
 * @param output    the produced item
 * @param outnumber how many are produced (recipe result count); cost is divided by this
 * @param slots     the consumed inputs and any credits
 * @param source    the recipe id this conversion came from (for debugging/provenance); may be null
 */
public record Conversion(Item output, int outnumber, List<Slot> slots, ResourceLocation source) {

    /**
     * One input position.
     *
     * @param candidates items that can satisfy this slot (a choice/tag ingredient has several);
     *                   the solver charges the cheapest-by-flux candidate.
     * @param amount     how many are consumed. <b>Negative</b> means a credit — e.g. the empty
     *                   bucket returned when a recipe consumes a water bucket. Credits are only
     *                   applied if their item is already priced (see {@link ValueSolver}); positive
     *                   slots must be priced for the conversion to be costable.
     */
    public record Slot(List<Item> candidates, int amount) {
        public boolean isCredit() {
            return amount < 0;
        }
    }
}
