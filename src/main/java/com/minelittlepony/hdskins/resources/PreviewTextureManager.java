package com.minelittlepony.hdskins.resources;

import com.google.common.collect.Maps;
import com.minelittlepony.hdskins.resources.texture.ISkinAvailableCallback;
import com.minelittlepony.hdskins.resources.texture.ImageBufferDownloadHD;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;

import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

import javax.annotation.Nullable;

/**
 * Manager for fetching preview textures. This ensures that multiple calls
 * to the skin server aren't done when fetching preview textures.
 */
public class PreviewTextureManager {

    private final Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures;

    public PreviewTextureManager(MinecraftTexturesPayload payload) {
        this.textures = payload.getTextures();
    }

    @Nullable
    public PreviewTexture getPreviewTexture(ResourceLocation location, MinecraftProfileTexture.Type type, ResourceLocation def, @Nullable SkinManager.SkinAvailableCallback callback) {
        if (!textures.containsKey(type)) {
            return null;
        }

        MinecraftProfileTexture texture = textures.get(type);
        ISkinAvailableCallback buff = new ImageBufferDownloadHD(type, () -> {
            if (callback != null) {
                callback.skinAvailable(type, location, new MinecraftProfileTexture(texture.getUrl(), Maps.newHashMap()));
            }
        });

        PreviewTexture skinTexture = new PreviewTexture(texture.getMetadata("model"), texture.getUrl(), def, buff);

        TextureLoader.loadTexture(location, skinTexture);

        return skinTexture;
    }
}
