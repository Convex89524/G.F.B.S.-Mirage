package org.mirage.Command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.command.EnumArgument;
import org.mirage.Phenomenon.network.ScriptSystem.NetworkHandler;
import org.mirage.Phenomenon.network.ScriptSystem.OpenFileChooserPacket;

import java.util.function.Supplier;

public class UploadScriptCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("MirageUploadedScript")
                .requires(source -> source.hasPermission(3)) // OP等级3+
                .then(Commands.argument("script_id", StringArgumentType.string())
                        .executes(context -> {
                            String scriptId = StringArgumentType.getString(context, "script_id");
                            ServerPlayer player = context.getSource().getPlayerOrException();

                            // 打开文件选择器
                            NetworkHandler.sendToClient(new OpenFileChooserPacket(scriptId), player);

                            context.getSource().sendSuccess(
                                    (Supplier<Component>) Component.literal("请选择要上传的脚本文件..."),
                                    true
                            );
                            return 1;
                        })
                )
        );
    }
}