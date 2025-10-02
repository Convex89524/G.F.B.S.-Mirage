/**
 * G.F.B.S. Mirage (mirage_gfbs) - A Minecraft Mod
 * Copyright (C) 2025-2029 Convex89524

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.mirage.Phenomenon.ScriptSystem;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.mirage.Mirage_gfbs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptExecutor {
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("^\\.\\s*(\\w+)\\s*$");
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    private static final Pattern SET_COMMAND_PATTERN = Pattern.compile("^set\\s+(\\w+)\\s+(.+)$");
    private static final Pattern IF_COMMAND_PATTERN = Pattern.compile("^if\\s+(.+)$");
    private static final Pattern LOOP_COMMAND_PATTERN = Pattern.compile("^loop\\s+(\\d+)\\s*$");
    private static final Pattern DELAY_COMMAND_PATTERN = Pattern.compile("^delay\\s+(\\d+)\\s*$");

    private static final Map<String, String> variables = new HashMap<>();
    private static final Stack<Boolean> conditionStack = new Stack<>();
    private static final Stack<Integer> loopStack = new Stack<>();
    private static final Stack<List<String>> loopLineStack = new Stack<>();
    private static final Stack<Integer> delayStack = new Stack<>();

    public static boolean executeScript(String scriptId, CommandSourceStack source) {
        variables.clear();
        conditionStack.clear();
        loopStack.clear();
        loopLineStack.clear();
        delayStack.clear();

        Path scriptPath = Mirage_gfbs.SCRIPTS_DIR.resolve(scriptId + ".txt");

        if (!Files.exists(scriptPath)) {
            source.sendFailure(Component.literal("脚本不存在: " + scriptId));
            return false;
        }

        try {
            // 指定UTF-8编码读取
            List<String> lines = Files.readAllLines(scriptPath, StandardCharsets.UTF_8);

            // 处理可能的BOM字符
            if (!lines.isEmpty() && lines.get(0).startsWith("\uFEFF")) {
                lines.set(0, lines.get(0).substring(1));
            }

            return executeMainFunction(lines, source);
        } catch (IOException e) {
            source.sendFailure(Component.literal("读取脚本失败: " + e.getMessage()));
            return false;
        }
    }

    private static boolean executeMainFunction(List<String> lines, CommandSourceStack source) {
        boolean inMainFunction = false;
        boolean foundMainFunction = false;
        int commandsExecuted = 0;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String trimmed = line.trim();

            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }

            // 处理延迟执行
            if (!delayStack.isEmpty()) {
                int delay = delayStack.pop();
                if (delay > 0) {
                    delayStack.push(delay - 1);
                    i--; // 重新处理当前行
                    try {
                        Thread.sleep(50); // 50ms tick
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }
            }

            // 处理循环
            if (!loopStack.isEmpty()) {
                int loopCount = loopStack.pop();
                List<String> loopLines = loopLineStack.pop();

                if (loopCount > 0) {
                    loopStack.push(loopCount - 1);
                    loopLineStack.push(loopLines);
                    // 执行循环体内的命令
                    for (String loopLine : loopLines) {
                        if (!processLine(loopLine, source)) {
                            source.sendFailure(Component.literal("循环内执行命令失败: " + loopLine));
                        }
                    }
                    i--; // 重新处理当前行
                    continue;
                } else {
                    continue; // 循环结束，继续下一行
                }
            }

            // 处理条件判断
            if (!conditionStack.isEmpty() && !conditionStack.peek()) {
                // 当前处于条件判断为假的分支，跳过直到遇到else或endif
                if (trimmed.equals("else")) {
                    conditionStack.pop();
                    conditionStack.push(true); // 进入else分支
                } else if (trimmed.equals("endif")) {
                    conditionStack.pop();
                }
                continue;
            }

            Matcher matcher = FUNCTION_PATTERN.matcher(trimmed);
            if (matcher.matches()) {
                String functionName = matcher.group(1).trim();
                inMainFunction = "main".equalsIgnoreCase(functionName);

                if (inMainFunction) {
                    foundMainFunction = true;
                }
                continue;
            }

            if (inMainFunction) {
                if (processLine(trimmed, source)) {
                    commandsExecuted++;
                } else {
                    source.sendFailure(Component.literal("执行命令失败: " + trimmed));
                }
            }
        }

        if (!foundMainFunction) {
            source.sendFailure(Component.literal("脚本中缺少 .main 函数"));
            return false;
        }

        return true;
    }

    private static boolean processLine(String line, CommandSourceStack source) {
        String processedLine = replaceVariables(line);

        // 检查是否是set命令（变量赋值）
        Matcher setMatcher = SET_COMMAND_PATTERN.matcher(processedLine);
        if (setMatcher.matches()) {
            String varName = setMatcher.group(1);
            String varValue = setMatcher.group(2);
            variables.put(varName, varValue);
            return true;
        }

        // 检查是否是if条件判断
        Matcher ifMatcher = IF_COMMAND_PATTERN.matcher(processedLine);
        if (ifMatcher.matches()) {
            String condition = ifMatcher.group(1);
            boolean result = evaluateCondition(condition, source);
            conditionStack.push(result);
            return true;
        }

        // 检查是否是else命令
        if (processedLine.equals("else")) {
            if (!conditionStack.isEmpty()) {
                boolean previousCondition = conditionStack.pop();
                conditionStack.push(!previousCondition);
            }
            return true;
        }

        // 检查是否是endif命令
        if (processedLine.equals("endif")) {
            if (!conditionStack.isEmpty()) {
                conditionStack.pop();
            }
            return true;
        }

        // 检查是否是loop命令
        Matcher loopMatcher = LOOP_COMMAND_PATTERN.matcher(processedLine);
        if (loopMatcher.matches()) {
            int count = Integer.parseInt(loopMatcher.group(1));
            loopStack.push(count);
            loopLineStack.push(new ArrayList<>());
            return true;
        }

        // 检查是否是endloop命令
        if (processedLine.equals("endloop")) {
            // 不需要做任何事情，循环已经在主逻辑中处理
            return true;
        }

        // 检查是否是delay命令
        Matcher delayMatcher = DELAY_COMMAND_PATTERN.matcher(processedLine);
        if (delayMatcher.matches()) {
            int delay = Integer.parseInt(delayMatcher.group(1));
            delayStack.push(delay);
            return true;
        }

        // 如果是循环体内的命令，添加到当前循环
        if (!loopStack.isEmpty() && loopLineStack.size() > 0) {
            loopLineStack.peek().add(processedLine);
            return true;
        }

        // 普通命令执行
        return executeCommand(processedLine, source);
    }

    private static String replaceVariables(String line) {
        Matcher matcher = VARIABLE_PATTERN.matcher(line);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String varName = matcher.group(1);
            String replacement = variables.getOrDefault(varName, "");
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private static boolean evaluateCondition(String condition, CommandSourceStack source) {
        if (condition.contains("==")) {
            String[] parts = condition.split("==");
            if (parts.length == 2) {
                String left = replaceVariables(parts[0].trim());
                String right = replaceVariables(parts[1].trim());
                return left.equals(right);
            }
        } else if (condition.contains("!=")) {
            String[] parts = condition.split("!=");
            if (parts.length == 2) {
                String left = replaceVariables(parts[0].trim());
                String right = replaceVariables(parts[1].trim());
                return !left.equals(right);
            }
        }

        return false;
    }

    private static boolean executeCommand(String command, CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        try {
            int result = server.getCommands().performPrefixedCommand(source, command);
            return result > 0;
        } catch (Exception e) {
            source.sendFailure(Component.literal("执行命令时出错: " + command + " - " + e.getMessage()));
            return false;
        }
    }
}