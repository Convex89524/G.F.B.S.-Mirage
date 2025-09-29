package org.mirage.Phenomenon.event;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.mirage.Phenomenon.BlackHole.BlackHoleManager;
import org.mirage.Phenomenon.network.BlackHole.NetworkHandler;
import org.mirage.Phenomenon.network.packets.BlackHole.BlackHoleCreatePacket;
import org.mirage.Phenomenon.network.packets.BlackHole.BlackHoleRemovePacket;

public class BlackHoleCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("CreateBlackHole")
                .requires(source -> source.hasPermission(3))
                .then(Commands.argument("name", StringArgumentType.string())
                        .then(Commands.argument("position", Vec3Argument.vec3())
                                .then(Commands.argument("radius", DoubleArgumentType.doubleArg(0.1, 10.0))
                                        .then(Commands.argument("lensing", DoubleArgumentType.doubleArg(1.0, 20.0))
                                                .executes(context -> createBlackHole(
                                                        context,
                                                        StringArgumentType.getString(context, "name"),
                                                        Vec3Argument.getVec3(context, "position"),
                                                        DoubleArgumentType.getDouble(context, "radius"),
                                                        DoubleArgumentType.getDouble(context, "lensing")
                                                ))
                                        )
                                )
                        )
                )
        );

        dispatcher.register(Commands.literal("DeleteBlackHole")
                .requires(source -> source.hasPermission(3))
                .then(Commands.argument("name", StringArgumentType.string())
                        .executes(context -> deleteBlackHole(
                                context,
                                StringArgumentType.getString(context, "name")
                        ))
                )
        );
    }

    private static int createBlackHole(CommandContext<CommandSourceStack> context, String name, Vec3 position, double radius, double lensing) {
        // 在服务端创建黑洞
        boolean success = BlackHoleManager.createBlackHole(name, radius, lensing, position);

        if (success) {
            BlackHoleCreatePacket packet = new BlackHoleCreatePacket(name, position, radius, lensing);
            NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), packet);

            context.getSource().sendSuccess(() ->
                            Component.translatable("command.blackhole.create.success", name, position.x, position.y, position.z),
                    true
            );
            return 1;
        } else {
            context.getSource().sendFailure(
                    Component.translatable("command.blackhole.create.failure", name)
            );
            return 0;
        }
    }

    private static int deleteBlackHole(CommandContext<CommandSourceStack> context, String name) {
        // 在服务端删除黑洞
        boolean success = BlackHoleManager.removeBlackHole(name);

        if (success) {
            BlackHoleRemovePacket packet = new BlackHoleRemovePacket(name);
            NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), packet);

            context.getSource().sendSuccess(() ->
                            Component.translatable("command.blackhole.delete.success", name),
                    true
            );
            return 1;
        } else {
            context.getSource().sendFailure(
                    Component.translatable("command.blackhole.delete.failure", name)
            );
            return 0;
        }
    }
}