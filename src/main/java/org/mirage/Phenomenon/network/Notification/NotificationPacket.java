package org.mirage.Phenomenon.network.Notification;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.mirage.Tools.NotifucationGUI.NotificationGUI;

import java.util.function.Supplier;

public class NotificationPacket {
    private final String title;
    private final String message;
    private final int displayTime;

    public NotificationPacket(String title, String message, int displayTime) {
        this.title = title;
        this.message = message;
        this.displayTime = displayTime;
    }

    public NotificationPacket(FriendlyByteBuf buf) {
        this.title = buf.readUtf();
        this.message = buf.readUtf();
        this.displayTime = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(title);
        buf.writeUtf(message);
        buf.writeInt(displayTime);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            try {
                NotificationGUI.showNotification(title, message, displayTime);
            } catch (Exception e) {
                System.err.println("Error in showNotification: " + e.getMessage());
                e.printStackTrace();
            }
        });
        return true;
    }
}