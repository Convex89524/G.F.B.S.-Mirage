package org.mirage.Tools;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Task {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(
            Runtime.getRuntime().availableProcessors(),
            new TaskThreadFactory()
    );

    // 延迟执行任务
    public static Future<?> delay(Runnable task, long delay, TimeUnit unit) {
        return scheduler.schedule(task, delay, unit);
    }

    // 在后台线程执行任务
    public static Future<?> spawn(Runnable task) {
        return scheduler.submit(task);
    }

    // 周期性执行任务
    public static ScheduledFuture<?> repeat(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return scheduler.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    // 关闭任务调度器
    public static void shutdown() {
        scheduler.shutdownNow();
    }

    // 自定义线程工厂（命名线程）
    private static class TaskThreadFactory implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "TaskThread-" + counter.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    }
}