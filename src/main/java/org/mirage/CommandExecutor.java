package org.mirage;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.server.ServerLifecycleHooks;

public class CommandExecutor {

    // 默认执行指令（在主世界/权限等级3）
    public static int executeCommand(String command) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ServerLevel overworld = server.overworld();
        return executeCommand(command, overworld, 3);
    }

    // 自定义执行指令
    public static int executeCommand(String command, ServerLevel level, int permissionLevel) {
        MinecraftServer server = level.getServer();
        CommandSourceStack source = createCommandSource(server, level, permissionLevel);

        return server.getCommands().performPrefixedCommand(source, command);
    }

    // 创建命令源
    private static CommandSourceStack createCommandSource(MinecraftServer server, ServerLevel level, int permissionLevel) {
        return new CommandSourceStack(
                CommandSource.NULL, // 使用系统命令源
                Vec3.ZERO,          // 位置（世界原点）
                Vec2.ZERO,          // 朝向
                level,              // 所在维度
                permissionLevel,    // 权限等级
                "CommandExecutor",  // 名称
                Component.literal("CommandExecutor"), // 显示名称
                server,             // 服务器实例
                null                // 无关联实体
        );
    }

    // 异步执行指令（在服务器线程）
    public static void executeCommandAsync(String command) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.execute(() -> executeCommand(command));
    }
}