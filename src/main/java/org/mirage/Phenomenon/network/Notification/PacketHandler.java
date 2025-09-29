package org.mirage.Phenomenon.network.Notification;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.mirage.Mirage_gfbs;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("mirage", "notifications"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int packetId = 0;

    private static boolean registered = false;

    public static void register() {
        if (registered) return;
        registered = true;

        INSTANCE.registerMessage(packetId++, NotificationPacket.class,
                NotificationPacket::toBytes,
                NotificationPacket::new,
                NotificationPacket::handle);

        Mirage_gfbs.LOGGER.info("Registered notification network channel");
    }

    public static void sendToPlayer(NotificationPacket packet, ServerPlayer player) {
        Mirage_gfbs.LOGGER.info("Send client Popup: {}", packet);
        INSTANCE.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}