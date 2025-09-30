package org.mirage.Client.ClientShake;

public class ShakeManager {
    public static void startShake(float speed, float maxAmplitude, int duration, int riseTime, int fallTime) {
        ClientShakeHandler.speed = speed;
        ClientShakeHandler.maxAmplitude = maxAmplitude;
        ClientShakeHandler.duration = duration;
        ClientShakeHandler.riseTime = riseTime;
        ClientShakeHandler.fallTime = fallTime;
        ClientShakeHandler.startTime = System.currentTimeMillis();
    }
}