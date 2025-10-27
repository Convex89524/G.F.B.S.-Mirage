package org.mirage;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static org.mirage.Mirage_gfbs.LOGGER;

import java.util.stream.IntStream;

public class SoundEventRegister {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Mirage_gfbs.MODID);

    public static Holder<SoundEvent> register(ResourceLocation p_263323_, ResourceLocation p_263411_, float p_263385_) {
        return Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, p_263323_, SoundEvent.createFixedRangeEvent(p_263411_, p_263385_));
    }

    public static SoundEvent register(String p_12657_) {
        return register(new ResourceLocation(p_12657_), 16.0f);
    }

    public static SoundEvent register(ResourceLocation location, float range) {
        LOGGER.info("register sound: {}", location.getNamespace());
        return register(location, location, range).value();
    }

    public static Holder.Reference<SoundEvent> registerForHolder(String p_263391_) {
        return registerForHolder(new ResourceLocation(p_263391_));
    }

    public static Holder.Reference<SoundEvent> registerForHolder(ResourceLocation p_263361_) {
        return registerForHolder(p_263361_, p_263361_);
    }

    public static SoundEvent register(ResourceLocation p_263388_, ResourceLocation p_263340_) {
        return Registry.register(BuiltInRegistries.SOUND_EVENT, p_263388_, SoundEvent.createFixedRangeEvent(p_263340_, 16.0f));
    }

    public static Holder.Reference<SoundEvent> registerForHolder(ResourceLocation p_263362_, ResourceLocation p_263424_) {
        return Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, p_263362_, SoundEvent.createFixedRangeEvent(p_263424_, 16.0f));
    }
}