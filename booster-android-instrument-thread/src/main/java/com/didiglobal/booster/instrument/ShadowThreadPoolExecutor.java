package com.didiglobal.booster.instrument;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author johnsonlee
 */
public class ShadowThreadPoolExecutor extends ThreadPoolExecutor {

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

    /**
     * Initialize {@code ThreadPoolExecutor} with new thread name, this constructor is used by {@code ThreadTransformer} for thread renaming
     *
     * @param corePoolSize    the number of threads to keep in the pool, even if they are idle,
     *                        unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the pool
     * @param keepAliveTime   when the number of threads is greater than the core,
     *                        this is the maximum time that excess idle threads will wait for new tasks before terminating.
     * @param unit            the time unit for the {@code keepAliveTime} argument
     * @param workQueue       the queue to use for holding tasks before they are executed.
     *                        This queue will hold only the {@code Runnable} tasks submitted by the {@code execute} method.
     * @param prefix          the prefix of new thread
     * @throws IllegalArgumentException if one of the following holds:<br>
     *                                  {@code corePoolSize < 0}<br>
     *                                  {@code keepAliveTime < 0}<br>
     *                                  {@code maximumPoolSize <= 0}<br>
     *                                  {@code maximumPoolSize < corePoolSize}
     */
    public ShadowThreadPoolExecutor(
            final int corePoolSize,
            final int maximumPoolSize,
            final long keepAliveTime,
            final TimeUnit unit,
            final BlockingQueue<Runnable> workQueue,
            final String prefix
    ) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, prefix, false);
    }

    /**
     * Initialize {@code ThreadPoolExecutor} with new thread name, this constructor is used by {@code ThreadTransformer} for thread renaming
     *
     * @param corePoolSize    the number of threads to keep in the pool, even if they are idle,
     *                        unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the pool
     * @param keepAliveTime   when the number of threads is greater than the core,
     *                        this is the maximum time that excess idle threads will wait for new tasks before terminating.
     * @param unit            the time unit for the {@code keepAliveTime} argument
     * @param workQueue       the queue to use for holding tasks before they are executed.
     *                        This queue will hold only the {@code Runnable} tasks submitted by the {@code execute} method.
     * @param prefix          the prefix of new thread
     * @param optimize        the value indicates that the thread pool optimization should be applied
     * @throws IllegalArgumentException if one of the following holds:<br>
     *                                  {@code corePoolSize < 0}<br>
     *                                  {@code keepAliveTime < 0}<br>
     *                                  {@code maximumPoolSize <= 0}<br>
     *                                  {@code maximumPoolSize < corePoolSize}
     */
    public ShadowThreadPoolExecutor(
            final int corePoolSize,
            final int maximumPoolSize,
            final long keepAliveTime,
            final TimeUnit unit,
            final BlockingQueue<Runnable> workQueue,
            final String prefix,
            final boolean optimize
    ) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, new NamedThreadFactory(prefix));
        if (optimize) {
            allowCoreThreadTimeOut(getKeepAliveTime(unit) > 0);
        }
    }

    /**
     * Initialize {@code ThreadPoolExecutor} with new thread name, this constructor is used by {@code ThreadTransformer} for thread renaming
     *
     * @param corePoolSize    the number of threads to keep in the pool, even if they are idle,
     *                        unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the pool
     * @param keepAliveTime   when the number of threads is greater than the core,
     *                        this is the maximum time that excess idle threads will wait for new tasks before terminating.
     * @param unit            the time unit for the {@code keepAliveTime} argument
     * @param workQueue       the queue to use for holding tasks before they are executed.
     *                        This queue will hold only the {@code Runnable} tasks submitted by the {@code execute} method.
     * @param threadFactory   the factory to use when the executor creates a new thread
     * @param prefix          the prefix of new thread
     * @throws IllegalArgumentException if one of the following holds:<br>
     *                                  {@code corePoolSize < 0}<br>
     *                                  {@code keepAliveTime < 0}<br>
     *                                  {@code maximumPoolSize <= 0}<br>
     *                                  {@code maximumPoolSize < corePoolSize}
     */
    public ShadowThreadPoolExecutor(
            final int corePoolSize,
            final int maximumPoolSize,
            final long keepAliveTime,
            final TimeUnit unit,
            final BlockingQueue<Runnable> workQueue,
            final ThreadFactory threadFactory,
            final String prefix
    ) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, prefix, false);
    }

    /**
     * Initialize {@code ThreadPoolExecutor} with new thread name, this constructor is used by {@code ThreadTransformer} for thread renaming
     *
     * @param corePoolSize    the number of threads to keep in the pool, even if they are idle,
     *                        unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the pool
     * @param keepAliveTime   when the number of threads is greater than the core,
     *                        this is the maximum time that excess idle threads will wait for new tasks before terminating.
     * @param unit            the time unit for the {@code keepAliveTime} argument
     * @param workQueue       the queue to use for holding tasks before they are executed.
     *                        This queue will hold only the {@code Runnable} tasks submitted by the {@code execute} method.
     * @param threadFactory   the factory to use when the executor creates a new thread
     * @param prefix          the prefix of new thread
     * @param optimize        the value indicates that the thread pool optimization should be applied
     * @throws IllegalArgumentException if one of the following holds:<br>
     *                                  {@code corePoolSize < 0}<br>
     *                                  {@code keepAliveTime < 0}<br>
     *                                  {@code maximumPoolSize <= 0}<br>
     *                                  {@code maximumPoolSize < corePoolSize}
     */
    public ShadowThreadPoolExecutor(
            final int corePoolSize,
            final int maximumPoolSize,
            final long keepAliveTime,
            final TimeUnit unit,
            final BlockingQueue<Runnable> workQueue,
            final ThreadFactory threadFactory,
            final String prefix,
            final boolean optimize
    ) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, new NamedThreadFactory(threadFactory, prefix));
        if (optimize) {
            allowCoreThreadTimeOut(getKeepAliveTime(unit) > 0);
        }
    }

    /**
     * Initialize {@code ThreadPoolExecutor} with new thread name, this constructor is used by {@code ThreadTransformer} for thread renaming
     *
     * @param corePoolSize    the number of threads to keep in the pool, even if they are idle,
     *                        unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the pool
     * @param keepAliveTime   when the number of threads is greater than the core,
     *                        this is the maximum time that excess idle threads will wait for new tasks before terminating.
     * @param unit            the time unit for the {@code keepAliveTime} argument
     * @param workQueue       the queue to use for holding tasks before they are executed.
     *                        This queue will hold only the {@code Runnable} tasks submitted by the {@code execute} method.
     * @param handler         the handler to use when execution is blocked because the thread bounds and queue capacities are reached
     * @param prefix          the prefix of new thread
     * @throws IllegalArgumentException if one of the following holds:<br>
     *                                  {@code corePoolSize < 0}<br>
     *                                  {@code keepAliveTime < 0}<br>
     *                                  {@code maximumPoolSize <= 0}<br>
     *                                  {@code maximumPoolSize < corePoolSize}
     */
    public ShadowThreadPoolExecutor(
            final int corePoolSize,
            final int maximumPoolSize,
            final long keepAliveTime,
            final TimeUnit unit,
            final BlockingQueue<Runnable> workQueue,
            final RejectedExecutionHandler handler,
            final String prefix
    ) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler, prefix, false);
    }

    /**
     * Initialize {@code ThreadPoolExecutor} with new thread name, this constructor is used by {@code ThreadTransformer} for thread renaming
     *
     * @param corePoolSize    the number of threads to keep in the pool, even if they are idle,
     *                        unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the pool
     * @param keepAliveTime   when the number of threads is greater than the core,
     *                        this is the maximum time that excess idle threads will wait for new tasks before terminating.
     * @param unit            the time unit for the {@code keepAliveTime} argument
     * @param workQueue       the queue to use for holding tasks before they are executed.
     *                        This queue will hold only the {@code Runnable} tasks submitted by the {@code execute} method.
     * @param handler         the handler to use when execution is blocked because the thread bounds and queue capacities are reached
     * @param prefix          the prefix of new thread
     * @param optimize        the value indicates that the thread pool optimization should be applied
     * @throws IllegalArgumentException if one of the following holds:<br>
     *                                  {@code corePoolSize < 0}<br>
     *                                  {@code keepAliveTime < 0}<br>
     *                                  {@code maximumPoolSize <= 0}<br>
     *                                  {@code maximumPoolSize < corePoolSize}
     */
    public ShadowThreadPoolExecutor(
            final int corePoolSize,
            final int maximumPoolSize,
            final long keepAliveTime,
            final TimeUnit unit,
            final BlockingQueue<Runnable> workQueue,
            final RejectedExecutionHandler handler,
            final String prefix,
            final boolean optimize
    ) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, new NamedThreadFactory(prefix), handler);
        if (optimize) {
            allowCoreThreadTimeOut(getKeepAliveTime(unit) > 0);
        }
    }

    /**
     * Initialize {@code ThreadPoolExecutor} with new thread name, this constructor is used by {@code ThreadTransformer} for thread renaming
     *
     * @param corePoolSize    the number of threads to keep in the pool, even if they are idle,
     *                        unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the pool
     * @param keepAliveTime   when the number of threads is greater than the core,
     *                        this is the maximum time that excess idle threads will wait for new tasks before terminating.
     * @param unit            the time unit for the {@code keepAliveTime} argument
     * @param workQueue       the queue to use for holding tasks before they are executed.
     *                        This queue will hold only the {@code Runnable} tasks submitted by the {@code execute} method.
     * @param threadFactory   the factory to use when the executor creates a new thread
     * @param handler         the handler to use when execution is blocked because the thread bounds and queue capacities are reached
     * @param prefix          the prefix of new thread
     * @throws IllegalArgumentException if one of the following holds:<br>
     *                                  {@code corePoolSize < 0}<br>
     *                                  {@code keepAliveTime < 0}<br>
     *                                  {@code maximumPoolSize <= 0}<br>
     *                                  {@code maximumPoolSize < corePoolSize}
     */
    public ShadowThreadPoolExecutor(
            final int corePoolSize,
            final int maximumPoolSize,
            final long keepAliveTime,
            final TimeUnit unit,
            final BlockingQueue<Runnable> workQueue,
            final ThreadFactory threadFactory,
            final RejectedExecutionHandler handler,
            final String prefix
    ) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler, prefix, false);
    }

    /**
     * Initialize {@code ThreadPoolExecutor} with new thread name, this constructor is used by {@code ThreadTransformer} for thread renaming
     *
     * @param corePoolSize    the number of threads to keep in the pool, even if they are idle,
     *                        unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the pool
     * @param keepAliveTime   when the number of threads is greater than the core,
     *                        this is the maximum time that excess idle threads will wait for new tasks before terminating.
     * @param unit            the time unit for the {@code keepAliveTime} argument
     * @param workQueue       the queue to use for holding tasks before they are executed.
     *                        This queue will hold only the {@code Runnable} tasks submitted by the {@code execute} method.
     * @param threadFactory   the factory to use when the executor creates a new thread
     * @param handler         the handler to use when execution is blocked because the thread bounds and queue capacities are reached
     * @param prefix          the prefix of new thread
     * @param optimize        the value indicates that the thread pool optimization should be applied
     * @throws IllegalArgumentException if one of the following holds:<br>
     *                                  {@code corePoolSize < 0}<br>
     *                                  {@code keepAliveTime < 0}<br>
     *                                  {@code maximumPoolSize <= 0}<br>
     *                                  {@code maximumPoolSize < corePoolSize}
     */
    public ShadowThreadPoolExecutor(
            final int corePoolSize,
            final int maximumPoolSize,
            final long keepAliveTime,
            final TimeUnit unit,
            final BlockingQueue<Runnable> workQueue,
            final ThreadFactory threadFactory,
            final RejectedExecutionHandler handler,
            final String prefix,
            final boolean optimize
    ) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, new NamedThreadFactory(threadFactory, prefix), handler);
        if (optimize) {
            allowCoreThreadTimeOut(getKeepAliveTime(unit) > 0);
        }
    }

}
