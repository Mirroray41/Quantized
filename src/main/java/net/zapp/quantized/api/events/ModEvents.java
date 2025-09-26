package net.zapp.quantized.api.events;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.blocks.machine_block.MachineBlockScreen;
import net.zapp.quantized.init.ModBlockEntities;
import net.zapp.quantized.init.ModFluidTypes;
import net.zapp.quantized.init.ModMenuTypes;
import net.zapp.quantized.networking.ModClientMessages;
import net.zapp.quantized.networking.ModMessages;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = Quantized.MOD_ID)
public class ModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {

    }
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.MACHINE_BLOCK_MENU.get(), MachineBlockScreen::new);
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MACHINE_BLOCK_TILE.get(),
                (be, side) -> {
                    ItemStackHandler handler = be.getItemHandler();
                    if (side == Direction.UP) {
                        return new RangedWrapper(handler, 0, 1);
                    } else if (side == Direction.DOWN) {
                        return new RangedWrapper(handler, 1, 2);
                    } else {
                        return new RangedWrapper(handler, 0, 2);
                    }
                });

        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.MACHINE_BLOCK_TILE.get(),
                (be, side) -> be.getEnergyHandler());

        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.MACHINE_BLOCK_TILE.get(),
                (be, side) -> be.getFluidHandler());
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
