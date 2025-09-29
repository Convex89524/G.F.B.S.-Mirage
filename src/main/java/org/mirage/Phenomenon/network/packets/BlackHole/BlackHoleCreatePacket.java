package org.mirage.Phenomenon.network.packets.BlackHole;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import org.mirage.Phenomenon.BlackHole.BlackHoleManager;

import java.util.function.Supplier;

public class BlackHoleCreatePacket {
    private final String name;
    private final Vec3 position;
    private final double radius;
    private final double lensing;

    public BlackHoleCreatePacket(String name, Vec3 position, double radius, double lensing) {
        this.name = name;
        this.position = position;
        this.radius = radius;
        this.lensing = lensing;
    }

    public BlackHoleCreatePacket(FriendlyByteBuf buf) {
        this.name = buf.readUtf();
        this.position = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.radius = buf.readDouble();
        this.lensing = buf.readDouble();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(name);
        buf.writeDouble(position.x);
        buf.writeDouble(position.y);
        buf.writeDouble(position.z);
        buf.writeDouble(radius);
        buf.writeDouble(lensing);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            BlackHoleManager.createBlackHole(name, radius, lensing, position);
        });
        return true;
    }
}