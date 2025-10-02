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

package org.mirage.Tools.NotifucationGUI;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameRules;
import org.mirage.Mirage_gfbs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotificationGUI {
    private static final List<Notification> notifications = new ArrayList<>();
    private static final int ANIMATION_DURATION = 20; // 动画时长（ticks）
    private static final float WIDTH_HEIGHT_RATIO = 2.85f;
    private static final int MARGIN = 10; // 屏幕边缘间距
    private static final int PADDING = 6; // 内容内边距 (从8减小到6，缩小25%)
    private static final int NOTIFICATION_SPACING = 4; // 弹窗之间的间距 (从5减小到4，缩小20%)
    private static final long DUPLICATE_CHECK_TIME_WINDOW = 1000; // 重复检查时间窗口（毫秒）
    private static final float SCALE = 0.75f; // 缩放因子

    public static void showNotification(String title, String message, int displayTime) {
        Component titleComponent = Component.literal(title);
        Component messageComponent = Component.literal(message);

        long currentTime = System.currentTimeMillis();
        for (Notification notification : notifications) {
            if (notification.isDuplicate(titleComponent, messageComponent, DUPLICATE_CHECK_TIME_WINDOW, currentTime)) {
                return;
            }
        }

        Notification newNotification = new Notification(titleComponent, messageComponent, currentTime, displayTime);
        notifications.add(newNotification);
        updateNotificationPositions();
    }

    public static void render(GuiGraphics guiGraphics, float partialTicks) {
        for (int i = notifications.size() - 1; i >= 0; i--) {
            Notification notification = notifications.get(i);
            if (!notification.isFinished()) {
                notification.render(guiGraphics, partialTicks);
            }
        }
    }

    public static void tick() {
        Iterator<Notification> iterator = notifications.iterator();
        while (iterator.hasNext()) {
            Notification notification = iterator.next();
            notification.tick();

            if (notification.isFinished()) {
                iterator.remove();
                updateNotificationPositions();
            }
        }
    }

    private static void updateNotificationPositions() {
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int notificationHeight = (int) ((screenHeight / 5) * SCALE); // 应用缩放因子

        for (int i = 0; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);
            int targetY = screenHeight - notificationHeight - MARGIN -
                    i * (notificationHeight + NOTIFICATION_SPACING);
            notification.setTargetY(targetY);
        }
    }

    private static class Notification {
        private final Component title;
        private final Component message;
        private final int width;
        private final int height;
        private final int targetX;
        private int targetY;
        private int animationTimer;
        private int displayTimer;
        private final int displayTime;
        private boolean isShowing = true;
        private boolean isHiding = false;
        private float prevProgress = 0f;
        private float currentY; // 当前Y坐标，用于平滑移动
        private final long creationTime; // 通知创建时间戳
        private float alpha = 0f; // 透明度

        public Notification(Component title, Component message, long creationTime, int displayTime) {
            this.title = title;
            this.message = message;
            this.creationTime = creationTime;
            this.displayTime = displayTime;

            int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            this.height = (int) ((screenHeight / 5) * SCALE);
            this.width = (int) (height * WIDTH_HEIGHT_RATIO);

            int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            this.targetX = screenWidth - width - MARGIN;
            this.targetY = screenHeight - height - MARGIN;
            this.currentY = screenHeight;

            this.animationTimer = 0;
            this.displayTimer = 0;
        }

        public void setTargetY(int targetY) {
            this.targetY = targetY;
        }

        public void tick() {
            float moveSpeed = 0.2f;
            currentY = Mth.lerp(moveSpeed, currentY, targetY);

            int currentDisplayDuration = this.displayTime;
            if (isShowing && animationTimer < ANIMATION_DURATION) {
                animationTimer++;
                alpha = (float) animationTimer / ANIMATION_DURATION;
            } else if (isShowing) {
                displayTimer++;
                alpha = 1.0f;
                if (displayTimer >= currentDisplayDuration) {
                    isShowing = false;
                    isHiding = true;
                    animationTimer = ANIMATION_DURATION;
                }
            } else if (isHiding && animationTimer > 0) {
                animationTimer--;
                alpha = (float) animationTimer / ANIMATION_DURATION;
            }
        }

        public void render(GuiGraphics guiGraphics, float partialTicks) {
            float progress = getAnimationProgress(partialTicks);

            float smoothedProgress = Mth.lerp(0.5f, prevProgress, progress);
            prevProgress = progress;

            int currentX = calculateCurrentX(smoothedProgress);
            int renderY = (int) currentY;

            int bgAlpha = (int) (0xAA * alpha);
            int titleAlpha = (int) (0xFF * alpha);
            int messageAlpha = (int) (0xCC * alpha);

            guiGraphics.fill(currentX, renderY,
                    currentX + width, renderY + height,
                    (bgAlpha << 24) | 0x000000);

            int titleHeight = height / 7;
            int messageHeight = height - titleHeight;

            guiGraphics.drawCenteredString(
                    Minecraft.getInstance().font,
                    title,
                    currentX + width / 2,
                    renderY + (titleHeight - Minecraft.getInstance().font.lineHeight) / 2,
                    (titleAlpha << 24) | 0xFFFFFF
            );

            List<FormattedCharSequence> lines = Minecraft.getInstance().font.split(
                    message,
                    width - 2 * PADDING
            );

            int lineHeight = Minecraft.getInstance().font.lineHeight;
            int maxLines = messageHeight / lineHeight;
            int linesToDraw = Math.min(lines.size(), maxLines);

            for (int i = 0; i < linesToDraw; i++) {
                guiGraphics.drawString(
                        Minecraft.getInstance().font,
                        lines.get(i),
                        currentX + PADDING,
                        renderY + titleHeight + PADDING + i * lineHeight,
                        (messageAlpha << 24) | 0xCCCCCC
                );
            }
        }

        private float getAnimationProgress(float partialTicks) {
            float totalTime = ANIMATION_DURATION;
            float currentTime;

            if (isShowing) {
                currentTime = animationTimer + partialTicks;
            } else if (isHiding) {
                currentTime = totalTime - (animationTimer + partialTicks);
            } else {
                return 1.0f;
            }

            float t = Mth.clamp(currentTime / totalTime, 0, 1);
            return t * t * t * (t * (t * 6 - 15) + 10);
        }

        private int calculateCurrentX(float progress) {
            int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();

            if (isShowing) {
                return (int) (screenWidth + (targetX - screenWidth) * progress);
            } else if (isHiding) {
                return targetX;
            }
            return targetX;
        }

        public boolean isFinished() {
            return isHiding && animationTimer <= 0;
        }

        public boolean isDuplicate(Component otherTitle, Component otherMessage, long timeWindow, long currentTime) {
            return this.title.equals(otherTitle) &&
                    this.message.equals(otherMessage) &&
                    !this.isFinished() &&
                    (currentTime - this.creationTime) < timeWindow;
        }
    }
}