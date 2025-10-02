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