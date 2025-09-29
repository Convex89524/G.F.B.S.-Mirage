package org.mirage;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;

@Mod.EventBusSubscriber(modid = Mirage_gfbs.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SoundLogger {
    private static final Logger LOGGER = LogManager.getLogger();

    // 指定要记录的 MOD ID
    private static final String TARGET_MOD_ID = Mirage_gfbs.MODID;

    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new ResourceManagerReloadListener() {
            @Override
            public void onResourceManagerReload(ResourceManager manager) {
                logSoundFiles(manager);
            }
        });
    }

    private static void logSoundFiles(ResourceManager manager) {
        String soundPath = "sounds";

        Collection<ResourceLocation> resources = manager.listResources(
                soundPath,
                location -> location.getNamespace().equals(TARGET_MOD_ID) &&
                        location.getPath().endsWith(".ogg")
        ).keySet();

        resources.forEach(loc -> {
            String path = loc.getPath(); // 完整路径如 "sounds/ambient/cave.ogg"
            LOGGER.info("Found .ogg file: {}:{}", loc.getNamespace(), path);
        });
    }
}