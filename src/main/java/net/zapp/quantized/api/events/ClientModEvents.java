package net.zapp.quantized.api.events;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.zapp.quantized.Quantized;
import net.zapp.quantized.init.ModBlocks;

@EventBusSubscriber(modid = Quantized.MOD_ID, value = Dist.CLIENT)
public class ClientModEvents {

}