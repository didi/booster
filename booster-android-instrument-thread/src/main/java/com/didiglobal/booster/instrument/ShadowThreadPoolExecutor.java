package com.didiglobal.booster.instrument;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ShadowThreadPoolExecutor {

    // <editor-fold desc="- named thread pool executor">

    public static ThreadPoolExecutor newThreadPoolExecutor(final int corePoolSize, final int maxPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final String name) {
        return new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue, new NamedThreadFactory(name));
    }

    public static ThreadPoolExecutor newThreadPoolExecutor(final int corePoolSize, final int maxPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final ThreadFactory factory, final String name) {
        return new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue, new NamedThreadFactory(factory, name));
    }

    public static ThreadPoolExecutor newThreadPoolExecutor(final int corePoolSize, final int maxPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final RejectedExecutionHandler handler, final String name) {
        return new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue, new NamedThreadFactory(name), handler);
    }

    public static ThreadPoolExecutor newThreadPoolExecutor(final int corePoolSize, final int maxPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final ThreadFactory factory, final RejectedExecutionHandler handler, final String name) {
        return new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue, new NamedThreadFactory(factory, name), handler);
    }

    // </editor-fold>

    //<editor-fold desc="* optimized thread pool executor">

    public static ThreadPoolExecutor newOptimizedThreadPoolExecutor(final int corePoolSize, final int maxPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue) {
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue);
        executor.allowCoreThreadTimeOut(keepAliveTime > 0);
        return executor;
    }

    public static ThreadPoolExecutor newOptimizedThreadPoolExecutor(final int corePoolSize, final int maxPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final ThreadFactory factory) {
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue, factory);
        executor.allowCoreThreadTimeOut(keepAliveTime > 0);
        return executor;
    }

    public static ThreadPoolExecutor newOptimizedThreadPoolExecutor(final int corePoolSize, final int maxPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final RejectedExecutionHandler handler) {
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue, handler);
        executor.allowCoreThreadTimeOut(keepAliveTime > 0);
        return executor;
    }

    public static ThreadPoolExecutor newOptimizedThreadPoolExecutor(final int corePoolSize, final int maxPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final ThreadFactory factory, final RejectedExecutionHandler handler) {
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue, factory, handler);
        executor.allowCoreThreadTimeOut(keepAliveTime > 0);
        return executor;
    }

    //</editor-fold>

    // <editor-fold desc="* optimized named thread pool executor">

    public static ThreadPoolExecutor newOptimizedThreadPoolExecutor(final int corePoolSize, final int maxPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final String name) {
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue, new NamedThreadFactory(name));
        executor.allowCoreThreadTimeOut(keepAliveTime > 0);
        return executor;
    }

    public static ThreadPoolExecutor newOptimizedThreadPoolExecutor(final int corePoolSize, final int maxPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final ThreadFactory factory, final String name) {
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue, new NamedThreadFactory(factory, name));
        executor.allowCoreThreadTimeOut(keepAliveTime > 0);
        return executor;
    }

    public static ThreadPoolExecutor newOptimizedThreadPoolExecutor(final int corePoolSize, final int maxPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final RejectedExecutionHandler handler, final String name) {
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue, new NamedThreadFactory(name), handler);
        executor.allowCoreThreadTimeOut(keepAliveTime > 0);
        return executor;
    }

    public static ThreadPoolExecutor newOptimizedThreadPoolExecutor(final int corePoolSize, final int maxPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final ThreadFactory factory, final RejectedExecutionHandler handler, final String name) {
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue, new NamedThreadFactory(factory, name), handler);
        executor.allowCoreThreadTimeOut(keepAliveTime > 0);
        return executor;
    }

    // </editor-fold>

}
