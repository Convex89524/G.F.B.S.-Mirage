package org.mirage.Phenomenon.BlackHole;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class BlackHole {
    private final double eventHorizonRadius;
    private final double lensingFactor;
    private Vec3 position;

    public BlackHole(double eventHorizonRadius, double lensingFactor, Vec3 position) {
        this.eventHorizonRadius = eventHorizonRadius;
        this.lensingFactor = Math.max(1.0, lensingFactor); // 确保最小为1
        this.position = position;
    }

    // 更新位置
    public void updatePosition(Player player) {
        this.position = player.position().add(0, 5, 0);
    }

    // 计算引力影响（用于实体运动）
    public void applyGravity(Entity entity) {
        Vec3 toBlackHole = position.subtract(entity.position());

        double distanceSqr = toBlackHole.lengthSqr();
        double influenceRadius = eventHorizonRadius * 10;
        double distance = Math.sqrt(distanceSqr);

        if (distanceSqr < influenceRadius * influenceRadius) {
            double forceMagnitude = 0.1 * eventHorizonRadius / (distance * distance + 0.01);
            Vec3 force = toBlackHole.normalize().scale(forceMagnitude);
            entity.setDeltaMovement(entity.getDeltaMovement().add(force));
        }
    }

    // 渲染相关参数获取
    public double getRenderRadius(float partialTicks) {
        return eventHorizonRadius;
    }

    public double getLensingFactor() {
        return lensingFactor;
    }

    public Vec3 getPosition() {
        return position;
    }

    public void setPosition(Vec3 position) {
        this.position = position;
    }
}