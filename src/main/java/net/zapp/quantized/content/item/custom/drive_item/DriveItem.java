package net.zapp.quantized.content.item.custom.drive_item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zapp.quantized.core.init.ModDataComponents;
import net.zapp.quantized.core.init.ModItems;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DriveItem extends Item {
    private final int capacity;
    private final int maxPatternSize;

    public DriveItem(Properties properties, int capacity, int maxPatternSize) {
        super(properties);
        this.capacity = capacity;
        this.maxPatternSize = maxPatternSize;
    }

    @Override
    public @NotNull ItemStack getCraftingRemainder(ItemStack itemStack) {
        return new ItemStack(ModItems.DRIVE_CASING.get());
    }

    private void initializeDriveData(ItemStack stack) {
        if (!stack.has(ModDataComponents.DRIVE_DATA.get())) {
            stack.set(ModDataComponents.DRIVE_DATA.get(),
                    new DriveRecord(capacity, maxPatternSize, 0, new String[0], 0));
        }
    }

    // TODO: Remove, testing statement
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        initializeDriveData(player.getItemInHand(hand));
        DriveRecord data = player.getItemInHand(hand).get(ModDataComponents.DRIVE_DATA);
        List<String> items = new ArrayList<>(Arrays.stream(data.items()).toList());
        items.add("quantized:q_bit");
        DriveRecord newData = new DriveRecord(data.capacity(), data.maxSizePerItem(), data.dataUsed() + 1, items.toArray(new String[data.count() + 1]), data.count() + 1);
        player.getItemInHand(hand).set(ModDataComponents.DRIVE_DATA, newData);
        return InteractionResult.PASS;
    }
}
