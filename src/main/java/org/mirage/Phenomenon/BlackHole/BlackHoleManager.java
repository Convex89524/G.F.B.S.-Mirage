package org.mirage.Phenomenon.BlackHole;

import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

// 黑洞管理
public class BlackHoleManager {
    private static final Map<String, BlackHole> blackHoles = new HashMap<>();

    // 创建带名称的黑洞
    public static boolean createBlackHole(String name, double radius, double lensing, Vec3 pos) {
        synchronized (blackHoles) {
            if (blackHoles.containsKey(name)) {
                return false; // 名称已存在
            }
            blackHoles.put(name, new BlackHole(radius, lensing, pos));
            return true;
        }
    }

    // 原有的创建方法（保持兼容性）
    public static void createBlackHole(double radius, double lensing, Vec3 pos) {
        createBlackHole("BlackHole_" + System.currentTimeMillis(), radius, lensing, pos);
    }

    // 获取所有黑洞
    public static List<BlackHole> getBlackHoles() {
        synchronized (blackHoles) {
            return Collections.unmodifiableList(new ArrayList<>(blackHoles.values()));
        }
    }

    // 按名称获取黑洞
    public static BlackHole getBlackHole(String name) {
        synchronized (blackHoles) {
            return blackHoles.get(name);
        }
    }

    // 按名称删除黑洞
    public static boolean removeBlackHole(String name) {
        synchronized (blackHoles) {
            return blackHoles.remove(name) != null;
        }
    }

    // 移动黑洞到新位置
    public static boolean moveBlackHole(String name, Vec3 newPosition) {
        synchronized (blackHoles) {
            BlackHole blackHole = blackHoles.get(name);
            if (blackHole != null) {
                blackHole.setPosition(newPosition);
                return true;
            }
            return false;
        }
    }

    // 获取所有黑洞名称
    public static List<String> getBlackHoleNames() {
        synchronized (blackHoles) {
            return Collections.unmodifiableList(new ArrayList<>(blackHoles.keySet()));
        }
    }
}