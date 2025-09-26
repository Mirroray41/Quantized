package net.zapp.quantized;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.zapp.quantized.init.ModBlockEntities;
import net.zapp.quantized.init.ModBlocks;
import net.zapp.quantized.init.ModMenuTypes;
import net.zapp.quantized.init.ModRecipes;
import net.zapp.quantized.blocks.machine_block.MachineBlockScreen;
import net.zapp.quantized.init.ModFluidTypes;
import net.zapp.quantized.init.ModFluids;
import net.zapp.quantized.init.ModCreativeModeTabs;
import net.zapp.quantized.init.ModItems;
import net.zapp.quantized.networking.ModClientMessages;
import net.zapp.quantized.networking.ModMessages;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import javax.annotation.Nullable;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Quantized.MOD_ID)
public class Quantized {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "quantized";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Quantized(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        ModCreativeModeTabs.register(modEventBus);

        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModFluids.register(modEventBus);
        ModFluidTypes.register(modEventBus);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);
        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        /*if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.QUANTUM_MATTER);
        }*/
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MOD_ID)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.MACHINE_BLOCK_MENU.get(), MachineBlockScreen::new);
        }

        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.MACHINE_BLOCK_TILE.get(), (tile, side) -> tile.getEnergyStorage());
        }

        @SubscribeEvent
        public static void registerPayloadHandlersEvent(RegisterPayloadHandlersEvent event) {
            ModMessages.register(event);
        }

        @SubscribeEvent
        public static void registerClientPayloadHandlersEvent(RegisterClientPayloadHandlersEvent event) {
            ModClientMessages.register(event);
        }

        @SubscribeEvent
        static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
            event.registerFluidType(new IClientFluidTypeExtensions() {
                @Override
                public int getTintColor() {
                    return ModFluidTypes.QUANTUM_FLUX_FLUID_TYPE.get().getTintColor();
                }

                @Override
                public ResourceLocation getStillTexture() {
                    return ModFluidTypes.QUANTUM_FLUX_FLUID_TYPE.get().getStillTexture();
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return ModFluidTypes.QUANTUM_FLUX_FLUID_TYPE.get().getFlowingTexture();
                }

                @Override
                public @Nullable ResourceLocation getOverlayTexture() {
                    return ModFluidTypes.QUANTUM_FLUX_FLUID_TYPE.get().getOverlayTexture();
                }

                @Override
                public Vector4f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector4f fluidFogColor) {
                    Vector3f fogColor = ModFluidTypes.QUANTUM_FLUX_FLUID_TYPE.get().getFogColor();
                    return new Vector4f(
                            fogColor.x,
                            fogColor.y,
                            fogColor.z,
                            fluidFogColor.w
                    );
                }

                @Override
                public void modifyFogRender(Camera camera, @Nullable FogEnvironment environment, float renderDistance, float partialTick, FogData fogData) {
                    //TODO FIX
                }
            }, ModFluidTypes.QUANTUM_FLUX_FLUID_TYPE.get());
        }
    }
}
