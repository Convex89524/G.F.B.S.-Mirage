package org.mirage.Command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.mirage.Phenomenon.network.packets.GlobalSoundPlayer;

import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GlobalSoundPlayCommand {

    // 创建一个静态的建议提供器，用于动态过滤声音ID
    private static final SuggestionProvider<CommandSourceStack> SOUND_SUGGESTIONS =
            (context, builder) -> {
                String input = builder.getInput();
                String lastPart = input.substring(input.lastIndexOf(' ') + 1);

                // 获取所有声音ID并根据输入过滤
                return SharedSuggestionProvider.suggestResource(
                        ForgeRegistries.SOUND_EVENTS.getKeys().stream()
                                .filter(id -> id.toString().contains(lastPart))
                                .collect(Collectors.toList()),
                        builder
                );
            };

    // 注册网络消息
    public static void registerNetworkMessages() {
        int packetId = 1;
        GlobalSoundPlayer.CHANNEL.registerMessage(
                packetId++,
                SoundCommandPacket.class,
                SoundCommandPacket::encode,
                SoundCommandPacket::decode,
                GlobalSoundPlayCommand::handleSoundCommand
        );
    }

    // 处理声音命令包（服务端侧）
    private static void handleSoundCommand(SoundCommandPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                GlobalSoundPlayer.playToAllClients(player, packet.soundId, packet.volume);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    // 服务端命令注册
    @Mod.EventBusSubscriber
    public static class ServerCommandHandler {
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            event.getDispatcher().register(Commands.literal("playsoundglobal")
                    .requires(source -> source.hasPermission(3))
                    .then(Commands.argument("sound_id", ResourceLocationArgument.id())
                            .suggests(SOUND_SUGGESTIONS) // 使用动态过滤的建议提供器
                            .executes(context -> {
                                ResourceLocation soundId = ResourceLocationArgument.getId(context, "sound_id");
                                ServerPlayer player = context.getSource().getPlayer();

                                if (player != null) {
                                    GlobalSoundPlayer.playToAllClients(player, soundId, 1.0f);
                                }
                                return 1;
                            })
                            .then(Commands.argument("volume", FloatArgumentType.floatArg(0.0f, 10.0f))
                                    .executes(context -> {
                                        ResourceLocation soundId = ResourceLocationArgument.getId(context, "sound_id");
                                        float volume = FloatArgumentType.getFloat(context, "volume");
                                        ServerPlayer player = context.getSource().getPlayer();

                                        if (player != null) {
                                            GlobalSoundPlayer.playToAllClients(player, soundId, volume);
                                        }
                                        return 1;
                                    })
                            )
                    )
            );
        }
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT)
    public static class ClientCommandHandler {
        @SubscribeEvent
        public static void registerClientCommands(RegisterClientCommandsEvent event) {
            event.getDispatcher().register(Commands.literal("playsoundglobal")
                    .then(Commands.argument("sound_id", ResourceLocationArgument.id())
                            .executes(context -> {
                                ResourceLocation soundId = ResourceLocationArgument.getId(context, "sound_id");

                                GlobalSoundPlayer.CHANNEL.sendToServer(
                                        new SoundCommandPacket(soundId, 1.0f)
                                );
                                return 1;
                            })
                            .then(Commands.argument("volume", FloatArgumentType.floatArg(0.0f, 10.0f))
                                    .executes(context -> {
                                        ResourceLocation soundId = ResourceLocationArgument.getId(context, "sound_id");
                                        float volume = FloatArgumentType.getFloat(context, "volume");

                                        GlobalSoundPlayer.CHANNEL.sendToServer(
                                                new SoundCommandPacket(soundId, volume)
                                        );
                                        return 1;
                                    })
                            )
                    )
            );
        }
    }

    // 声音命令网络包数据类
    public static class SoundCommandPacket {
        public final ResourceLocation soundId;
        public final float volume;

        public SoundCommandPacket(ResourceLocation soundId, float volume) {
            this.soundId = soundId;
            this.volume = volume;
        }

        public void encode(FriendlyByteBuf buffer) {
            buffer.writeResourceLocation(soundId);
            buffer.writeFloat(volume);
        }

        public static SoundCommandPacket decode(FriendlyByteBuf buffer) {
            return new SoundCommandPacket(buffer.readResourceLocation(), buffer.readFloat());
        }
    }
}