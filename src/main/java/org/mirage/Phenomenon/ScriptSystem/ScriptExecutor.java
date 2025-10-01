package org.mirage.Phenomenon.ScriptSystem;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.mirage.Mirage_gfbs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptExecutor {
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("^\\.(\\w+)$");

    public static boolean executeScript(String scriptId, CommandSourceStack source) {
        Path scriptPath = Mirage_gfbs.SCRIPTS_DIR.resolve(scriptId + ".txt");

        if (!Files.exists(scriptPath)) {
            source.sendFailure(Component.literal("脚本不存在: " + scriptId));
            return false;
        }

        try {
            List<String> lines = Files.readAllLines(scriptPath);
            return executeMainFunction(lines, source);
        } catch (IOException e) {
            source.sendFailure(Component.literal("读取脚本失败: " + e.getMessage()));
            return false;
        }
    }

    private static boolean executeMainFunction(List<String> lines, CommandSourceStack source) {
        boolean inMainFunction = false;
        int commandsExecuted = 0;

        for (String line : lines) {
            String trimmed = line.trim();

            // 跳过空行和注释
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }

            // 检查函数定义
            Matcher matcher = FUNCTION_PATTERN.matcher(trimmed);
            if (matcher.matches()) {
                String functionName = matcher.group(1);
                inMainFunction = "main".equals(functionName);
                continue;
            }

            // 如果在main函数中，执行命令
            if (inMainFunction) {
                if (executeCommand(trimmed, source)) {
                    commandsExecuted++;
                } else {
                    source.sendFailure(Component.literal("执行命令失败: " + trimmed));
                }
            }
        }

        if (!inMainFunction) {
            source.sendFailure(Component.literal("脚本中缺少 .main 函数"));
            return false;
        }

        return commandsExecuted > 0;
    }

    private static boolean executeCommand(String command, CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        int result = server.getCommands().performPrefixedCommand(source, command);
        return result > 0;
    }
}