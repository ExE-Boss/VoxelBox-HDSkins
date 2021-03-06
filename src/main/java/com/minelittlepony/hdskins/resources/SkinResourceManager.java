package com.minelittlepony.hdskins.resources;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.minelittlepony.hdskins.HDSkins;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;

public class SkinResourceManager implements ISelectiveResourceReloadListener {

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private Map<UUID, Skin> uuidSkins = Maps.newHashMap();
    private Map<String, Skin> namedSkins = Maps.newHashMap();
    private Map<ResourceLocation, Future<ResourceLocation>> inProgress = Maps.newHashMap();
    private Map<ResourceLocation, ResourceLocation> converted = Maps.newHashMap();

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        if (resourcePredicate.test(VanillaResourceType.TEXTURES)) {
            uuidSkins.clear();
            namedSkins.clear();
            executor.shutdownNow();
            executor = Executors.newSingleThreadExecutor();
            inProgress.clear();
            converted.clear();
            for (String domain : resourceManager.getResourceDomains()) {
                try {
                    for (IResource res : resourceManager.getAllResources(new ResourceLocation(domain, "textures/skins/skins.json"))) {
                        try {
                            SkinData data = getSkinData(res.getInputStream());
                            for (Skin s : data.skins) {
                                if (s.uuid != null) {
                                    uuidSkins.put(s.uuid, s);
                                }
                                if (s.name != null) {
                                    namedSkins.put(s.name, s);
                                }
                            }
                        } catch (JsonParseException je) {
                            HDSkins.instance.logger.warn("Invalid skins.json in %s", res.getResourcePackName(), je);
                        }
                    }
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private SkinData getSkinData(InputStream stream) {
        try {
            return new Gson().fromJson(new InputStreamReader(stream), SkinData.class);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    @Nullable
    public ResourceLocation getPlayerTexture(GameProfile profile, Type type) {
        if (type != Type.SKIN)
            // not supported
            return null;

        Skin skin = getSkin(profile);
        if (skin != null) {
            final ResourceLocation res = skin.getTexture();
            return getConvertedResource(res);
        }
        return null;
    }

    /**
     * Convert older resources to a newer format.
     *
     * @param res The skin resource to convert
     * @return The converted resource
     */
    @Nullable
    public ResourceLocation getConvertedResource(@Nullable ResourceLocation res) {
        loadSkinResource(res);
        return converted.get(res);
    }

    private void loadSkinResource(@Nullable final ResourceLocation res) {
        if (res != null) {
            // read and convert in a new thread
            this.inProgress.computeIfAbsent(res, r -> CompletableFuture.supplyAsync(new ImageLoader(r), executor)
                    .whenComplete((loc, t) -> {
                        if (loc != null)
                            converted.put(res, loc);
                        else {
                            LogManager.getLogger().warn("Errored while processing {}. Using original.", res, t);
                            converted.put(res, res);
                        }
                    }));


        }

    }

    @Nullable
    private Skin getSkin(GameProfile profile) {
        Skin skin = this.uuidSkins.get(profile.getId());
        if (skin == null) {
            skin = this.namedSkins.get(profile.getName());
        }
        return skin;
    }

}
