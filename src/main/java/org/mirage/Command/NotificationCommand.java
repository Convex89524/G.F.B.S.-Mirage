package org.mirage.Command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import org.mirage.Mirage_gfbs;
import org.mirage.Phenomenon.network.Notification.NotificationPacket;
import org.mirage.Phenomenon.network.Notification.PacketHandler;

import java.util.Collection;

public class NotificationCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("Notification")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.argument("tick", IntegerArgumentType.integer(1))
                                .then(Commands.argument("title", StringArgumentType.word())
                                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                                .executes(context -> {
                                                    Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
                                                    String title = StringArgumentType.getString(context, "title");
                                                    String message = StringArgumentType.getString(context, "message");

                                                    // 从服务端获取游戏规则值
                                                    int displayTime = IntegerArgumentType.getInteger(context, "tick");

                                                    for (ServerPlayer player : targets) {
                                                        PacketHandler.sendToPlayer(
                                                                new NotificationPacket(title, message, displayTime),
                                                                player
                                                        );
                                                    }

                                                    return targets.size();
                                                })
                                        )
                                )
                        )
                )
        );
    }
}