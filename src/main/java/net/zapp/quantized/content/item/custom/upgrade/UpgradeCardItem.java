package net.zapp.quantized.content.item.custom.upgrade;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * An upgrade card that improves one {@link UpgradeType} by a fixed {@code value} multiplier.
 *
 * <p>Higher tiers carry a larger value and are crafted from higher q-bit tiers. A machine only
 * accepts a card in the slot matching the card's {@link #type()}.
 */
public class UpgradeCardItem extends Item {
    private final UpgradeType type;
    private final int tier;
    private final double value;

    public UpgradeCardItem(Properties properties, UpgradeType type, int tier, double value) {
        super(properties.stacksTo(16));
        this.type = type;
        this.tier = tier;
        this.value = value;
    }

    public UpgradeType type() {
        return type;
    }

    public int tier() {
        return tier;
    }

    /** The multiplier this card applies to its stat (e.g. 2.0 = double speed / quarter cost / 2x output). */
    public double value() {
        return value;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.quantized.upgrade." + type.lowerName(), formatValue()));
        tooltip.add(Component.translatable("tooltip.quantized.upgrade.tier", tier));
    }

    private String formatValue() {
        return value == Math.floor(value) ? Integer.toString((int) value) : Double.toString(value);
    }
}
