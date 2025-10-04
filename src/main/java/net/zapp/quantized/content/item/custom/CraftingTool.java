package net.zapp.quantized.content.item.custom;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CraftingTool extends Item {
    public CraftingTool(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull ItemStack getCraftingRemainder(ItemStack itemStack) {
        ItemStack stack = itemStack.copy();
        if (stack.getDamageValue() + 1 >= stack.getMaxDamage()) {
            return ItemStack.EMPTY;
        }
        stack.setDamageValue(stack.getDamageValue() + 1);
        return stack;
    }
}
