package net.zapp.quantized.core.utils.module.identifiers;

import net.zapp.quantized.core.utils.module.DriveInterfaceModule;
import javax.annotation.Nonnull;

public interface HasDriveInterfaceModule {
    @Nonnull
    DriveInterfaceModule getDriveInterfaceModule();

    default void setFilter(String newFilter) {
        getDriveInterfaceModule().filter(newFilter);
    }

    default void resetFilterAndScroll() {
        getDriveInterfaceModule().resetFilterAndScroll();
    }
}
