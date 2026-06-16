package net.zapp.quantized.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.core.init.ModItems;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Quantized.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.Q_BIT.get());
        basicItem(ModItems.Q_BYTE.get());
        basicItem(ModItems.STEEL_INGOT.get());
        basicItem(ModItems.STEEL_NUGGET.get());
        basicItem(ModItems.STEEL_GEAR.get());
        basicItem(ModItems.STEEL_PLATE.get());
        basicItem(ModItems.STEEL_ROD.get());
        basicItem(ModItems.MALLET.get());
        basicItem(ModItems.WIRE_CUTTERS.get());
        basicItem(ModItems.COPPER_WIRE.get());
        basicItem(ModItems.INDUCTOR.get());

        basicItem(ModItems.ATTRACTION_CORE.get());
        basicItem(ModItems.REPULSION_CORE.get());

        basicItem(ModItems.CARBON_SILICON_BATTERY.get());
        basicItem(ModItems.FLUX_TANK.get());

        basicItem(ModItems.Q_BYTES_8.get());
        basicItem(ModItems.Q_BYTES_64.get());
        basicItem(ModItems.Q_BYTES_512.get());

        basicItem(ModItems.Q_BYTES_4K.get());
        basicItem(ModItems.Q_BYTES_32K.get());
        basicItem(ModItems.Q_BYTES_256K.get());

        basicItem(ModItems.Q_BYTES_2M.get());
        basicItem(ModItems.Q_BYTES_16M.get());
        basicItem(ModItems.Q_BYTES_128M.get());

        basicItem(ModItems.Q_BYTES_1G.get());

        basicItem(ModItems.DRIVE_8.get());
        basicItem(ModItems.DRIVE_64.get());
        basicItem(ModItems.DRIVE_512.get());

        basicItem(ModItems.DRIVE_4K.get());
        basicItem(ModItems.DRIVE_32K.get());
        basicItem(ModItems.DRIVE_256K.get());

        basicItem(ModItems.DRIVE_2M.get());
        basicItem(ModItems.DRIVE_16M.get());
        basicItem(ModItems.DRIVE_128M.get());

        basicItem(ModItems.DRIVE_1G.get());

        basicItem(ModItems.DRIVE_CASING.get());
        // SINGULARITY_DRIVE model is hand-authored (placeholder texture not yet present, which
        // basicItem's ExistingFileHelper would reject).
    }
}