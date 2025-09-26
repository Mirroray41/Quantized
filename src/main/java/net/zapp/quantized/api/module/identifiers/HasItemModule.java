package net.zapp.quantized.api.module.identifiers;

import net.neoforged.neoforge.items.IItemHandler;
import net.zapp.quantized.api.module.ItemModule;

import javax.annotation.Nonnull;

public interface HasItemModule {
    @Nonnull
    ItemModule getItemModule();
    @Nonnull
    default IItemHandler getItemHandler() {
        return getItemModule().getHandler();
    }
}
