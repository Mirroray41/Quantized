package net.zapp.quantized.content.item.custom.drive_item;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.zapp.quantized.core.init.ModDataComponents;
import net.zapp.quantized.core.init.ModItems;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

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

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);

        initializeDriveData(stack);
        DriveRecord data = stack.get(ModDataComponents.DRIVE_DATA);
        tooltipAdder.accept(Component.translatable("tooltip.quantized.disk.data",data.dataUsed(), data.capacity()));
        tooltipAdder.accept(Component.translatable("tooltip.quantized.disk.max_size",data.maxSizePerItem()));
        tooltipAdder.accept(Component.translatable("tooltip.quantized.disk.count",data.count()));
        if (data.count() > 0) {
            if (!Screen.hasShiftDown()) {
                tooltipAdder.accept(Component.translatable("tooltip.quantized.disk.items"));
            } else {
                tooltipAdder.accept(Component.translatable("tooltip.quantized.disk.items_shift"));
                if (data.count() < 8) {
                    for (int i = 0; i < data.count(); i++) {
                        ResourceLocation location = ResourceLocation.parse(data.items()[i]);
                        Item item = BuiltInRegistries.ITEM.get(location).get().value();
                        tooltipAdder.accept(Component.translatable("tooltip.quantized.disk.item",item.getName().getString()));
                    }
                } else {
                    for (int i = 0; i < 8; i++) {
                        ResourceLocation location = ResourceLocation.parse(data.items()[i]);
                        Item item = BuiltInRegistries.ITEM.get(location).get().value();
                        tooltipAdder.accept(Component.translatable("tooltip.quantized.disk.item",item.getName().getString()));
                    }
                    tooltipAdder.accept(Component.translatable("tooltip.quantized.disk.item_more", data.count() - 8));
                }
            }
        }
    }
}
