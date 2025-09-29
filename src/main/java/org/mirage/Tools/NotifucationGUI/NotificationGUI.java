package org.mirage.Tools.NotifucationGUI;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotificationGUI {
    private static final List<Notification> notifications = new ArrayList<>();
    private static final int ANIMATION_DURATION = 20; // 动画时长（ticks）
    private static final int DISPLAY_DURATION = 200; // 显示持续时间（ticks）
    private static final float WIDTH_HEIGHT_RATIO = 2.85f;
    private static final int MARGIN = 10; // 屏幕边缘间距
    private static final int PADDING = 8; // 内容内边距
    private static final int NOTIFICATION_SPACING = 5; // 弹窗之间的间距
    private static final long DUPLICATE_CHECK_TIME_WINDOW = 1000; // 重复检查时间窗口（毫秒）

    public static void showNotification(String title, String message) {
        Component titleComponent = Component.literal(title);
        Component messageComponent = Component.literal(message);

        // 检查是否已经存在相同的通知（在时间窗口内）
        long currentTime = System.currentTimeMillis();
        for (Notification notification : notifications) {
            if (notification.isDuplicate(titleComponent, messageComponent, DUPLICATE_CHECK_TIME_WINDOW, currentTime)) {
                return;
            }
        }

        Notification newNotification = new Notification(titleComponent, messageComponent, currentTime);
        notifications.add(newNotification);
        updateNotificationPositions();
    }

    public static void render(GuiGraphics guiGraphics, float partialTicks) {
        // 从后向前渲染，确保新的弹窗在上面
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
                // 移除完成后更新剩余通知的位置
                updateNotificationPositions();
            }
        }
    }

    private static void updateNotificationPositions() {
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int notificationHeight = screenHeight / 5;

        // 计算每个通知的目标Y位置
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
        private boolean isShowing = true;
        private boolean isHiding = false;
        private float prevProgress = 0f;
        private float currentY; // 当前Y坐标，用于平滑移动
        private final long creationTime; // 通知创建时间戳
        private float alpha = 0f; // 透明度

        public Notification(Component title, Component message, long creationTime) {
            this.title = title;
            this.message = message;
            this.creationTime = creationTime;

            // 计算弹窗尺寸（基于屏幕高度）
            int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            this.height = screenHeight / 5; // 高度为屏幕高度的1/5
            this.width = (int) (height * WIDTH_HEIGHT_RATIO);

            // 计算目标位置（右下角）
            int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            this.targetX = screenWidth - width - MARGIN;
            this.targetY = screenHeight - height - MARGIN;
            this.currentY = screenHeight; // 初始位置在屏幕下方

            this.animationTimer = 0;
            this.displayTimer = 0;
        }

        public void setTargetY(int targetY) {
            this.targetY = targetY;
        }

        public void tick() {
            // 平滑移动到目标Y位置
            float moveSpeed = 0.2f;
            currentY = Mth.lerp(moveSpeed, currentY, targetY);

            if (isShowing && animationTimer < ANIMATION_DURATION) {
                animationTimer++;
                // 更新透明度 - 淡入
                alpha = (float) animationTimer / ANIMATION_DURATION;
            } else if (isShowing) {
                // 显示动画完成，开始计算显示时间
                displayTimer++;
                alpha = 1.0f; // 完全显示
                if (displayTimer >= DISPLAY_DURATION) {
                    isShowing = false;
                    isHiding = true;
                    animationTimer = ANIMATION_DURATION; // 确保从正确的位置开始隐藏
                }
            } else if (isHiding && animationTimer > 0) {
                animationTimer--;
                // 更新透明度 - 淡出
                alpha = (float) animationTimer / ANIMATION_DURATION;
            }
        }

        public void render(GuiGraphics guiGraphics, float partialTicks) {
            float progress = getAnimationProgress(partialTicks);

            // 添加平滑处理：使用线性插值减少跳跃
            float smoothedProgress = Mth.lerp(0.5f, prevProgress, progress);
            prevProgress = progress;

            int currentX = calculateCurrentX(smoothedProgress);
            int renderY = (int) currentY; // 使用平滑后的Y坐标

            // 计算实际透明度（考虑alpha值）
            int bgAlpha = (int) (0xAA * alpha);
            int titleAlpha = (int) (0xFF * alpha);
            int messageAlpha = (int) (0xCC * alpha);

            // 绘制背景
            guiGraphics.fill(currentX, renderY,
                    currentX + width, renderY + height,
                    (bgAlpha << 24) | 0x000000); // 带透明度的黑色背景

            // 计算标题和信息区域
            int titleHeight = height / 7;
            int messageHeight = height - titleHeight;

            // 绘制标题
            guiGraphics.drawCenteredString(
                    Minecraft.getInstance().font,
                    title,
                    currentX + width / 2,
                    renderY + (titleHeight - Minecraft.getInstance().font.lineHeight) / 2,
                    (titleAlpha << 24) | 0xFFFFFF // 带透明度的白色
            );

            // 绘制信息（自动换行）
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
                        (messageAlpha << 24) | 0xCCCCCC // 带透明度的浅灰色
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

            // 使用更平滑的缓动函数
            float t = Mth.clamp(currentTime / totalTime, 0, 1);
            // 使用更平滑的五次方缓动函数
            return t * t * t * (t * (t * 6 - 15) + 10); // Perlin平滑步进函数
        }

        private int calculateCurrentX(float progress) {
            int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();

            // 统一使用进度值计算位置，避免整数计算导致的跳跃
            if (isShowing) {
                // 从右侧不可见区域滑入
                return (int) (screenWidth + (targetX - screenWidth) * progress);
            } else if (isHiding) {
                // 隐藏时不再移动，只改变透明度
                return targetX;
            }
            return targetX;
        }

        public boolean isFinished() {
            return isHiding && animationTimer <= 0;
        }

        // 检查是否是重复通知
        public boolean isDuplicate(Component otherTitle, Component otherMessage, long timeWindow, long currentTime) {
            return this.title.equals(otherTitle) &&
                    this.message.equals(otherMessage) &&
                    !this.isFinished() &&
                    (currentTime - this.creationTime) < timeWindow;
        }
    }
}