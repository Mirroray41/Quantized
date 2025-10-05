package net.zapp.quantized;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.zapp.quantized.core.fluxdata.FluxDataRecipeComputer;
import net.zapp.quantized.core.fluxdata.FluxDataJsonLoader;
import net.zapp.quantized.core.configs.Config;
import net.zapp.quantized.core.configs.FluxDataConfig;
import net.zapp.quantized.core.init.*;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Quantized.MOD_ID)
public class Quantized {
    // Define mod id in data common place for everything to reference
    public static final String MOD_ID = "quantized";
    // Directly reference data slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Quantized(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.

        ModDataComponents.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModFluids.register(modEventBus);
        ModFluidTypes.register(modEventBus);
        ModComponents.register(modEventBus);
        ModSounds.register(modEventBus);

        FluxDataRecipeComputer.init();
        // Register the item to data creative tab
        modEventBus.addListener(this::addCreative);
        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        FluxDataJsonLoader.ensureExternJsonDirectoryExists();
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Quantized.MOD_ID, path);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(FluxDataConfig::load);
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        /*if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.QUANTUM_MATTER);
        }*/
    }
}
