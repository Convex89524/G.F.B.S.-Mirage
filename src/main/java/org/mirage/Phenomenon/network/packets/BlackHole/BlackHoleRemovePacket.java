package org.mirage.Phenomenon.network.packets.BlackHole;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.mirage.Phenomenon.BlackHole.BlackHoleManager;

import java.util.function.Supplier;

public class BlackHoleRemovePacket {
    private final String name;

    public BlackHoleRemovePacket(String name) {
        this.name = name;
    }

    public BlackHoleRemovePacket(FriendlyByteBuf buf) {
        this.name = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(name);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            BlackHoleManager.removeBlackHole(name);
        });
        return true;
    }
}