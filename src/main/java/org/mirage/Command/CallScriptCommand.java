package org.mirage.Command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.mirage.Phenomenon.ScriptSystem.ScriptExecutor;

import java.util.function.Supplier;

public class CallScriptCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("MirageCallScript")
                .requires(source -> source.hasPermission(3)) // OP等级3+
                .then(Commands.argument("script_id", StringArgumentType.string())
                        .executes(context -> {
                            String scriptId = StringArgumentType.getString(context, "script_id");
                            CommandSourceStack source = context.getSource();

                            // 执行脚本
                            boolean success = ScriptExecutor.executeScript(scriptId, source);

                            if (success) {
                                source.sendSuccess(
                                        (Supplier<Component>) Component.literal("成功执行脚本: " + scriptId),
                                        true
                                );
                            } else {
                                source.sendFailure(
                                        Component.literal("执行脚本失败: " + scriptId)
                                );
                            }
                            return success ? 1 : 0;
                        })
                )
        );
    }
}