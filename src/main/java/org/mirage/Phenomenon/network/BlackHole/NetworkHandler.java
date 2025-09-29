package org.mirage.Phenomenon.network.BlackHole;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.mirage.Mirage_gfbs;
import org.mirage.Phenomenon.network.packets.BlackHole.BlackHoleCreatePacket;
import org.mirage.Phenomenon.network.packets.BlackHole.BlackHoleRemovePacket;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Mirage_gfbs.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++, BlackHoleCreatePacket.class, BlackHoleCreatePacket::toBytes, BlackHoleCreatePacket::new, BlackHoleCreatePacket::handle);
        INSTANCE.registerMessage(id++, BlackHoleRemovePacket.class, BlackHoleRemovePacket::toBytes, BlackHoleRemovePacket::new, BlackHoleRemovePacket::handle);
    }
}