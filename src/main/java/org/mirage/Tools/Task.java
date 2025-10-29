package org.mirage.Tools;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Mod.EventBusSubscriber
public class Task {
    private static ScheduledExecutorService scheduler = null;
    private static final Object lock = new Object();

    // 获取调度器
    private static ScheduledExecutorService getScheduler() {
        if (scheduler == null || scheduler.isShutdown()) {
            synchronized (lock) {
                if (scheduler == null || scheduler.isShutdown()) {
                    scheduler = Executors.newScheduledThreadPool(
                            Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
                            new TaskThreadFactory()
                    );
                }
            }
        }
        return scheduler;
    }

    // 延迟执行任务
    public static Future<?> delay(Runnable task, long delay, TimeUnit unit) {
        return getScheduler().schedule(wrapTask(task), delay, unit);
    }

    // 在后台线程执行任务
    public static Future<?> spawn(Runnable task) {
        return getScheduler().submit(wrapTask(task));
    }

    // 周期性执行任务
    public static ScheduledFuture<?> repeat(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return getScheduler().scheduleAtFixedRate(wrapTask(task), initialDelay, period, unit);
    }

    /**
     * 等待指定的毫秒数（非阻塞方式）
     */
    public static CompletableFuture<Void> sleep(long milliseconds) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        getScheduler().schedule(() -> future.complete(null), milliseconds, TimeUnit.MILLISECONDS);
        return future;
    }

    /**
     * 等待指定的时间（非阻塞方式）
     */
    public static CompletableFuture<Void> sleep(long delay, TimeUnit unit) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        getScheduler().schedule(() -> future.complete(null), delay, unit);
        return future;
    }

    // 包装任务，添加异常处理
    private static Runnable wrapTask(Runnable original) {
        return () -> {
            try {
                original.run();
            } catch (Exception e) {
                System.err.println("Task execution failed: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }

    // 优雅关闭调度器
    public static void shutdown() {
        synchronized (lock) {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
                try {
                    if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                        scheduler.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    scheduler.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    // 监听服务器停止事件
    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        shutdown();
    }

    // 自定义线程工厂
    private static class TaskThreadFactory implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "Mirage-TaskThread-" + counter.getAndIncrement());
            t.setDaemon(false);
            return t;
        }
    }
}