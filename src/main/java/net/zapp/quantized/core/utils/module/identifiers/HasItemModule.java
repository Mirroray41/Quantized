package net.zapp.quantized.core.utils.module.identifiers;

import net.neoforged.neoforge.items.ItemStackHandler;
import net.zapp.quantized.core.utils.module.ItemModule;

import javax.annotation.Nonnull;

public interface HasItemModule {
    @Nonnull
    ItemModule getItemModule();
    @Nonnull
    default ItemStackHandler getItemHandler() {
        return getItemModule().getHandler();
    }
}
