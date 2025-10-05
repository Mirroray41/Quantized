package net.zapp.quantized.core.fluxdata;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.zapp.quantized.core.utils.DataFluxPair;

import java.util.Map;

public record PackValues(int priority, Map<ResourceLocation, DataFluxPair> items, Map<TagKey<Item>, DataFluxPair> tags, ResourceLocation source) {}
