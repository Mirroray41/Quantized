package net.zapp.quantized.item.custom;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zapp.quantized.init.ModItems;
import org.jetbrains.annotations.NotNull;

public class DriveItem extends Item {
    public DriveItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull ItemStack getCraftingRemainder(ItemStack itemStack) {
        return new ItemStack(ModItems.DRIVE_CASING.get());
    }
}
