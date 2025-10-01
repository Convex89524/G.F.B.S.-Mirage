package org.mirage.Phenomenon.network.ScriptSystem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.mirage.Client.ScriptSystem.ClientHandler;

import java.util.function.Supplier;

public class OpenFileChooserPacket {
    private final String scriptId;

    public OpenFileChooserPacket(String scriptId) {
        this.scriptId = scriptId;
    }

    public OpenFileChooserPacket(FriendlyByteBuf buf) {
        this.scriptId = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(scriptId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientHandler.openFileChooser(scriptId);
        });
        ctx.get().setPacketHandled(true);
    }
}