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

package org.mirage.Phenomenon.network.Network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.mirage.Mirage_gfbs;

import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Mirage_gfbs.MODID, "network_system_miragev"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int packetId = 0;

    public void register() {
        try {
            CHANNEL.registerMessage(packetId++, EventPacket.class,
                    EventPacket::encode,
                    this::decode,
                    EventPacket::handle,
                    Optional.of(NetworkDirection.PLAY_TO_CLIENT));
            Mirage_gfbs.LOGGER.info("Successfully registered network channel");
        } catch (Exception e) {
            Mirage_gfbs.LOGGER.error("Error occurred while registering network channel", e);
        }
    }

    private EventPacket decode(FriendlyByteBuf buf) {
        try {
            return new EventPacket(buf);
        } catch (Exception e) {
            Mirage_gfbs.LOGGER.error("Error occurred while decoding network packet", e);
            throw new RuntimeException("Failed to decode network packet", e);
        }
    }

    public static void sendToPlayer(ServerPlayer player, String eventId, CompoundTag data) {
        try {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new EventPacket(eventId, data));
            Mirage_gfbs.LOGGER.debug("Successfully sent event to player {}: {}", player.getName().getString(), eventId);
        } catch (Exception e) {
            Mirage_gfbs.LOGGER.error("Error occurred while sending event to player: {}", eventId, e);
        }
    }

    public static void sendToAll(String eventId, CompoundTag data) {
        try {
            CHANNEL.send(PacketDistributor.ALL.noArg(), new EventPacket(eventId, data));
            Mirage_gfbs.LOGGER.debug("Successfully sent event to all players: {}", eventId);
        } catch (Exception e) {
            Mirage_gfbs.LOGGER.error("Error occurred while sending event to all players: {}", eventId, e);
        }
    }
}