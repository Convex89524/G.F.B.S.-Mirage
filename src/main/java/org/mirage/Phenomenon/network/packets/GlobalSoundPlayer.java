package org.mirage.Phenomenon.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.mirage.Mirage_gfbs;

import java.util.function.Supplier;

public class GlobalSoundPlayer {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Mirage_gfbs.MODID, "global_sound"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    // 注册网络包
    public static void registerNetworkMessages() {
        int packetId = 0;
        CHANNEL.registerMessage(packetId++, SoundPacket.class,
                SoundPacket::encode,
                SoundPacket::decode,
                GlobalSoundPlayer::handleSoundPacket
        );
    }

    // 处理网络包（客户端侧）
    private static void handleSoundPacket(SoundPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(packet.soundId);
            if (sound != null) {
                // 确保在客户端执行
                if (ctx.get().getDirection().getReceptionSide().isClient()) {
                    LocalPlayer player = Minecraft.getInstance().player;
                    if (player != null) {
                        Vec3 pos = player.position();
                        // 在客户端玩家位置播放声音
                        player.level().playLocalSound(
                                pos.x, pos.y, pos.z,
                                sound, SoundSource.MASTER,
                                packet.volume, 1.0f, false
                        );
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    // 向所有玩家发送声音
    public static void playToAllClients(ServerPlayer sender, ResourceLocation soundId, float volume) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), new SoundPacket(soundId, volume));
    }

    // 网络包数据类
    public static class SoundPacket {
        public final ResourceLocation soundId;
        public final float volume;

        public SoundPacket(ResourceLocation soundId, float volume) {
            this.soundId = soundId;
            this.volume = volume;
        }

        public void encode(FriendlyByteBuf buffer) {
            buffer.writeResourceLocation(soundId);
            buffer.writeFloat(volume);
        }

        public static SoundPacket decode(FriendlyByteBuf buffer) {
            return new SoundPacket(buffer.readResourceLocation(), buffer.readFloat());
        }
    }
}