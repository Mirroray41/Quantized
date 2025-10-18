package net.zapp.quantized.client.event;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.content.blocks.flux_generator.FluxGeneratorScreen;
import net.zapp.quantized.content.blocks.quantum_analyzer.QuantumAnalyzerScreen;
import net.zapp.quantized.content.blocks.quantum_analyzer.renderer.QuantumAnalyzerRenderer;
import net.zapp.quantized.content.blocks.quantum_destabilizer.QuantumDestabilizerScreen;
import net.zapp.quantized.content.blocks.quantum_destabilizer.renderer.QuantumDestabilizerRenderer;
import net.zapp.quantized.content.blocks.quantum_fabricator.QuantumFabricatorScreen;
import net.zapp.quantized.content.blocks.quantum_fabricator.renderer.QuantumFabricatorRenderer;
import net.zapp.quantized.content.blocks.quantum_stabilizer.QuantumStabilizerScreen;
import net.zapp.quantized.core.init.ModBlockEntities;
import net.zapp.quantized.core.init.ModFluidTypes;
import net.zapp.quantized.core.init.ModMenuTypes;
import net.zapp.quantized.core.networking.ModClientMessages;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = Quantized.MOD_ID, value = Dist.CLIENT)
public class ClientModEvents {
    public ClientModEvents(ModContainer container) {
        // Allows NeoForge to create data config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {

    }

    @SubscribeEvent
    public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.QUANTUM_DESTABILIZER_TILE.get(), QuantumDestabilizerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.QUANTUM_ANALYZER_TILE.get(), QuantumAnalyzerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.QUANTUM_FABRICATOR_TILE.get(), QuantumFabricatorRenderer::new);


    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.QUANTUM_DESTABILIZER_MENU.get(), QuantumDestabilizerScreen::new);
        event.register(ModMenuTypes.QUANTUM_ANALYZER_MENU.get(), QuantumAnalyzerScreen::new);
        event.register(ModMenuTypes.QUANTUM_FABRICATOR_MENU.get(), QuantumFabricatorScreen::new);
        event.register(ModMenuTypes.QUANTUM_STABILIZER_MENU.get(), QuantumStabilizerScreen::new);
        event.register(ModMenuTypes.FLUX_GENERATOR.get(), FluxGeneratorScreen::new);
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