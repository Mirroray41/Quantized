package net.zapp.quantized.core.fluxdata;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public record Pack(int prio, ResourceLocation id, JsonObject root) {}
