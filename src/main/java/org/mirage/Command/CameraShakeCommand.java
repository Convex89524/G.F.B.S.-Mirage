package org.mirage.Command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import org.mirage.Mirage_gfbs;
import org.mirage.Phenomenon.CameraShake.CameraShakeModule;

import java.util.Collection;

public class CameraShakeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // 创建基础命令
        LiteralArgumentBuilder<CommandSourceStack> cameraShakeCommand = Commands.literal("CameraShake")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.argument("speed", FloatArgumentType.floatArg(0.01f, 100.0f))
                                .then(Commands.argument("maxAmplitude", FloatArgumentType.floatArg(0.01f, 10.0f))
                                        .then(Commands.argument("duration", IntegerArgumentType.integer(1, 1200000))
                                                .then(Commands.argument("riseTime", IntegerArgumentType.integer(0, 50000))
                                                        .then(Commands.argument("fallTime", IntegerArgumentType.integer(0, 50000))
                                                                .executes(CameraShakeCommand::executeShakeCommand)
                                                        )
                                                )
                                        )
                                )
                        )
                );

        dispatcher.register(cameraShakeCommand);

        LiteralArgumentBuilder<CommandSourceStack> cameraShakeStopCommand = Commands.literal("CameraShakeStop")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("targets", EntityArgument.players())
                        .executes(CameraShakeCommand::executeStopCommand)
                );

        dispatcher.register(cameraShakeStopCommand);
    }

    private static int executeShakeCommand(CommandContext<CommandSourceStack> context) {
        try {
            // 获取命令参数
            Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
            float speed = FloatArgumentType.getFloat(context, "speed");
            float maxAmplitude = FloatArgumentType.getFloat(context, "maxAmplitude");
            int duration = IntegerArgumentType.getInteger(context, "duration");
            int riseTime = IntegerArgumentType.getInteger(context, "riseTime");
            int fallTime = IntegerArgumentType.getInteger(context, "fallTime");

            // 验证参数有效性
            if (riseTime + fallTime > duration) {
                context.getSource().sendFailure(
                        net.minecraft.network.chat.Component.literal("上升时间和下降时间之和不能大于总持续时间")
                );
                return 0;
            }

            // 向每个目标玩家发送震动指令
            for (ServerPlayer player : targets) {
                CameraShakeModule.sendShakeCommand(player, speed, maxAmplitude, duration, riseTime, fallTime);
            }

            // 发送成功反馈
            if (targets.size() == 1) {
                Mirage_gfbs.LOGGER.debug("已向 " + targets.iterator().next().getDisplayName().getString() + " 发送相机震动指令");
            } else {
                Mirage_gfbs.LOGGER.debug("已向 " + targets.size() + " 名玩家发送相机震动指令");
            }

            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            Mirage_gfbs.LOGGER.debug("执行命令时发生错误: " + e.getMessage());
            return 0;
        }
    }

    private static int executeStopCommand(CommandContext<CommandSourceStack> context) {
        try {
            Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
            for (ServerPlayer player : targets) {
                CameraShakeModule.sendShakeStopCommand(player);
            }

            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            context.getSource().sendFailure(
                    net.minecraft.network.chat.Component.literal("停止命令执行错误: " + e.getMessage())
            );
            return 0;
        }
    }
}