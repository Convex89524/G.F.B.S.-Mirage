/**
 * G.F.B.S. Mirage (mirage_gfbs) - A Minecraft Mod
 * Copyright (C) 2025-2029 Convex89524

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.mirage;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import org.mirage.Command.*;
import org.mirage.Objects.Structure.Registrar;
import org.mirage.Objects.blocks.BlockRegistration;
import org.mirage.Objects.items.ItemRegistration;
import org.mirage.Phenomenon.CameraShake.CameraShakeModule;
import org.mirage.Phenomenon.network.Notification.PacketHandler;
import org.mirage.Phenomenon.network.packets.GlobalSoundPlayer;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Mod(Mirage_gfbs.MODID)
public class Mirage_gfbs {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "mirage_gfbs";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final Path SCRIPTS_DIR = FMLPaths.CONFIGDIR.get().resolve("Mirage_gfbs/scripts");

    // Create a Deferred Register to hold Blocks which will all be registered under the "mirage_gfbs" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "mirage_gfbs" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final DeferredRegister<SoundEvent> SOUND = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "mirage_gfbs" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public Mirage_gfbs() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        LOGGER.info("MOD "+MODID+" INIT...");

        BlockRegistration.init();
        ItemRegistration.init();
        Registrar.init();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        SOUND.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        GlobalSoundPlayer.registerNetworkMessages();
        GlobalSoundPlayCommand.registerNetworkMessages();

        createscriptdir();
    }

    public static GameRules.Key<GameRules.IntegerValue> RULE_MIRAGE_NOTIFICATION_SHOW_TIME;

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        if (Config.logDirtBlock) {
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
        }

        LOGGER.info("{} {}", Config.magicNumberIntroduction, Config.magicNumber);

        Config.items.forEach(item -> LOGGER.info("ITEM >> {}", item));

        event.enqueueWork(() -> {
            PacketHandler.register();
            LOGGER.info("Registered notification network channel");
        });

        Registrar.onSetup(event);

        event.enqueueWork(() -> {
            RULE_MIRAGE_NOTIFICATION_SHOW_TIME = GameRules.register(
                    "mirageNotificationShowTime",
                    GameRules.Category.MISC,
                    GameRules.IntegerValue.create(12) // 默认值10秒
            );
            LOGGER.info("Registered Mirage notification game rule");
        });

        CameraShakeModule.registerNetwork(event);

        event.enqueueWork(() -> {
            org.mirage.Phenomenon.network.ScriptSystem.NetworkHandler.register();
            LOGGER.info("Registered ScriptSystem network channel");
        });
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("server starting");
    }

    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event){
        NotificationCommand.register(event.getDispatcher());
        CameraShakeCommand.register(event.getDispatcher());

        UploadScriptCommand.register(event.getDispatcher());
        CallScriptCommand.register(event.getDispatcher());
        DeleteScriptCommand.register(event.getDispatcher());
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }

    @Mod.EventBusSubscriber(
            modid = Mirage_gfbs.MODID,
            bus = Mod.EventBusSubscriber.Bus.MOD,
            value = Dist.CLIENT
    )
    public class ShaderRegistry {
        public static ShaderInstance LENSING_SHADER_INSTANCE;

        @SubscribeEvent
        public static void registerShaders(RegisterShadersEvent event) {
            try {
                event.registerShader(
                        new ShaderInstance(
                                event.getResourceProvider(),
                                new ResourceLocation(Mirage_gfbs.MODID, "lensing"),
                                DefaultVertexFormat.POSITION_TEX),
                        shader -> {
                            LENSING_SHADER_INSTANCE = shader;
                        }
                );
            } catch (IOException e) {
                throw new RuntimeException("Failed to load lensing shader", e);
            }
        }
    }

    private void createscriptdir() {
        File dir = SCRIPTS_DIR.toFile();
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                LOGGER.info("Created scripts directory: {}", SCRIPTS_DIR);
            } else {
                LOGGER.error("Failed to create scripts directory: {}", SCRIPTS_DIR);
            }
        }
    }
}
