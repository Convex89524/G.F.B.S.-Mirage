package org.mirage.Command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.mirage.Mirage_gfbs;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MirageGFBsEventCommand {
    private static final Map<String, Consumer<CommandContext>> handlers = new HashMap<>();

    // 注册事件处理器
    public static void registerHandler(String eventId, Consumer<CommandContext> handler) {
        handlers.put(eventId, handler);
    }

    // 执行事件处理器
    public static boolean executeHandler(String eventId, CommandContext context) {
        Consumer<CommandContext> handler = handlers.get(eventId);
        if (handler != null) {
            try {
                handler.accept(context);
                return true;
            } catch (Exception e) {
                Mirage_gfbs.LOGGER.error("执行事件处理器时发生异常，事件ID: " + eventId, e);
                context.sendFailure("执行事件时发生异常: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    // 命令上下文类
    public static class CommandContext {
        private final CommandSourceStack source;

        public CommandContext(CommandSourceStack source) {
            this.source = source;
        }

        public CommandSourceStack getSource() {
            return source;
        }

        public void sendSuccess(String message) {
            try {
                source.sendSuccess(() -> Component.literal(message), false);
            } catch (Exception e) {
                Mirage_gfbs.LOGGER.error("发送成功消息时发生异常", e);
            }
        }

        public void sendFailure(String message) {
            try {
                source.sendFailure(Component.literal(message));
            } catch (Exception e) {
                Mirage_gfbs.LOGGER.error("发送失败消息时发生异常", e);
            }
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        try {
            dispatcher.register(
                    Commands.literal("MirageGFBsEvent")
                            .requires(source -> source.hasPermission(2)) // 需要OP权限
                            .then(Commands.literal("exec")
                                    .then(Commands.argument("eventId", StringArgumentType.string())
                                            .executes(context -> {
                                                try {
                                                    String eventId = StringArgumentType.getString(context, "eventId");
                                                    CommandContext ctx =
                                                            new CommandContext(context.getSource());

                                                    if (executeHandler(eventId, ctx)) {
                                                        return 1;
                                                    } else {
                                                        ctx.sendFailure("未知事件ID: " + eventId);
                                                        return 0;
                                                    }
                                                } catch (Exception e) {
                                                    Mirage_gfbs.LOGGER.error("命令执行时发生异常", e);
                                                    try {
                                                        context.getSource().sendFailure(Component.literal("命令执行失败: " + e.getMessage()));
                                                    } catch (Exception ex) {
                                                        Mirage_gfbs.LOGGER.error("发送命令执行失败消息时发生异常", ex);
                                                    }
                                                    return 0;
                                                }
                                            })
                                    )
                            )
            );
        } catch (Exception e) {
            Mirage_gfbs.LOGGER.error("注册命令时发生异常", e);
            throw e;
        }
    }
}