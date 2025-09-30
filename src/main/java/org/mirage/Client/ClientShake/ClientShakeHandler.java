package org.mirage.Client.ClientShake;

import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = "mirage_gfbs", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientShakeHandler {
    // 震动参数
    public static float currentAmplitude = 0;
    public static long startTime = 0;
    public static float speed;
    public static float maxAmplitude;
    public static int duration;
    public static int riseTime;
    public static int fallTime;

    // 用于生成随机震动方向的随机数生成器
    private static final Random random = new Random();
    private static Vec3 currentShakeDirection = Vec3.ZERO;
    private static Vec3 targetShakeDirection = Vec3.ZERO;
    private static long lastDirectionChangeTime = 0;
    private static final long DIRECTION_CHANGE_INTERVAL = 100;
    private static final double DIRECTION_SMOOTHING_FACTOR = 0.15; // 方向平滑过渡因子

    /**
     * 计算当前时间的震动偏移量
     */
    private static Vec3 calculateShakeOffset() {
        if (startTime == 0) return Vec3.ZERO;

        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed > duration) {
            resetShake();
            return Vec3.ZERO;
        }

        // 计算当前振幅
        currentAmplitude = calculateCurrentAmplitude(elapsed);
        if (currentAmplitude <= 0) return Vec3.ZERO;

        // 使用三个不同频率的正弦函数生成更自然的震动效果
        double xOffset = Math.sin(elapsed * speed / 1000.0) * currentAmplitude;
        double yOffset = Math.sin(elapsed * speed / 1000.0 * 1.17 + 0.5) * currentAmplitude; // 不同频率和相位
        double zOffset = Math.sin(elapsed * speed / 1000.0 * 0.83 + 1.2) * currentAmplitude; // 不同频率和相位

        return new Vec3(xOffset, yOffset, zOffset);
    }

    /**
     * 重置震动状态
     */
    public static void resetShake() {
        startTime = 0;
        currentAmplitude = 0;
        currentShakeDirection = Vec3.ZERO;
        targetShakeDirection = Vec3.ZERO;
    }

    /**
     * 生成随机震动方向
     */
    private static Vec3 generateRandomDirection() {
        // 使用球坐标生成更均匀的随机方向
        double theta = random.nextDouble() * Math.PI * 2;
        double phi = Math.acos(2 * random.nextDouble() - 1);
        double x = Math.sin(phi) * Math.cos(theta);
        double y = Math.sin(phi) * Math.sin(theta);
        double z = Math.cos(phi);

        return new Vec3(x, y, z).normalize();
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        Vec3 shakeOffset = calculateShakeOffset();
        if (shakeOffset.equals(Vec3.ZERO)) return;

        long currentTime = System.currentTimeMillis();

        // 定期更新目标震动方向
        if (currentTime - lastDirectionChangeTime > DIRECTION_CHANGE_INTERVAL) {
            targetShakeDirection = generateRandomDirection();
            lastDirectionChangeTime = currentTime;
        }

        // 平滑过渡到目标方向
        if (!currentShakeDirection.equals(targetShakeDirection)) {
            double dx = targetShakeDirection.x - currentShakeDirection.x;
            double dy = targetShakeDirection.y - currentShakeDirection.y;
            double dz = targetShakeDirection.z - currentShakeDirection.z;

            currentShakeDirection = new Vec3(
                    currentShakeDirection.x + dx * DIRECTION_SMOOTHING_FACTOR,
                    currentShakeDirection.y + dy * DIRECTION_SMOOTHING_FACTOR,
                    currentShakeDirection.z + dz * DIRECTION_SMOOTHING_FACTOR
            ).normalize();
        }

        // 应用震动到摄像机方向
        applyShakeToCamera(event, shakeOffset);
    }

    /**
     * 将震动效果应用到相机
     */
    private static void applyShakeToCamera(ViewportEvent.ComputeCameraAngles event, Vec3 shakeOffset) {
        // 将偏移向量投影到当前震动方向上
        Vec3 rotationOffset = currentShakeDirection.scale(shakeOffset.length());

        // 应用非线性响应曲线，使小震动更细腻，大震动更强烈
        double responseCurve = 1.0 + 0.3 * Math.pow(shakeOffset.length() / maxAmplitude, 2);

        event.setYaw((float) (event.getYaw() + rotationOffset.x * 10.0 * responseCurve));
        event.setPitch((float) (event.getPitch() + rotationOffset.y * 5.0 * responseCurve));
        event.setRoll((float) (event.getRoll() + rotationOffset.z * 3.0 * responseCurve));
    }

    /**
     * 根据经过时间计算当前振幅
     * 使用改进的包络函数和物理真实的衰减模型
     */
    private static float calculateCurrentAmplitude(long elapsed) {
        if (elapsed > duration) {
            return 0;
        }

        // 改进的包络函数
        float baseAmplitude;
        if (elapsed < riseTime) {
            // 使用S形曲线实现更自然的上升
            float progress = (float) elapsed / riseTime;
            baseAmplitude = maxAmplitude * (float) (0.5 - 0.5 * Math.cos(Math.PI * progress));
        } else if (elapsed < duration - fallTime) {
            baseAmplitude = maxAmplitude;
        } else {
            // 使用改进的衰减模型，模拟真实震动能量的消散
            int fallStart = duration - fallTime;
            float fallProgress = (float) (elapsed - fallStart) / fallTime;
            // 指数衰减结合S形曲线，使结束更自然
            baseAmplitude = maxAmplitude * (float) (Math.exp(-4 * fallProgress) * (0.5 + 0.5 * Math.cos(Math.PI * fallProgress)));
        }

        // 添加基于物理的随机扰动
        if (baseAmplitude > 0) {
            // 使用频率相关的噪声，模拟真实震动的高频成分
            float noiseAmplitude = 0.08f * baseAmplitude;
            // 多频率噪声叠加
            float noise1 = (float) improvedNoise(elapsed * 0.01) * noiseAmplitude;
            float noise2 = (float) improvedNoise(elapsed * 0.03 + 100) * noiseAmplitude * 0.6f;
            float noise3 = (float) improvedNoise(elapsed * 0.1 + 200) * noiseAmplitude * 0.3f;

            return Math.max(0, baseAmplitude + noise1 + noise2 + noise3);
        }

        return baseAmplitude;
    }

    /**
     * 改进的Perlin噪声函数
     */
    private static double improvedNoise(double x) {
        // 使用整数部分进行哈希
        int X = (int) Math.floor(x) & 255;
        x -= Math.floor(x);

        // 计算渐变函数
        double u = fade(x);

        // 使用更复杂的哈希函数生成更自然的梯度
        int h1 = hash(X);
        int h2 = hash(X+1);

        double grad1 = grad(h1, x);
        double grad2 = grad(h2, x-1);

        return lerp(u, grad1, grad2);
    }

    /**
     * 简单的哈希函数
     */
    private static int hash(int n) {
        n = (n << 13) ^ n;
        return (n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff;
    }

    private static double fade(double t) {
        // 改进的渐变曲线，更平滑
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(double t, double a, double b) {
        // 线性插值
        return a + t * (b - a);
    }

    private static double grad(int hash, double x) {
        // 使用哈希值的低位字节确定梯度
        int h = hash & 15;
        double grad = 1.0 + (h & 7); // 梯度值1-8
        if ((h & 8) != 0) grad = -grad; // 随机一半是负数
        return grad * x;
    }
}