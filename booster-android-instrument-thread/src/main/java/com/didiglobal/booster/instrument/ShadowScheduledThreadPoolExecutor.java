package com.didiglobal.booster.instrument;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author johnsonlee
 */
public class ShadowScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {

    /**
     * Initialize {@code ScheduledThreadPoolExecutor} with new thread name, this constructor is used by {@code ThreadTransformer} for thread renaming
     *
     * @param corePoolSize    the number of threads to keep in the pool, even if they are idle,
     *                        unless {@code allowCoreThreadTimeOut} is set
     * @param prefix          the prefix of new thread
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     */
    public ShadowScheduledThreadPoolExecutor(
            final int corePoolSize,
            final String prefix
    ) {
        this(corePoolSize, prefix, false);
    }

    /**
     * Initialize {@code ScheduledThreadPoolExecutor} with new thread name, this constructor is used by {@code ThreadTransformer} for thread renaming
     *
     * @param corePoolSize    the number of threads to keep in the pool, even if they are idle,
     *                        unless {@code allowCoreThreadTimeOut} is set
     * @param prefix          the prefix of new thread
     * @param optimize        the value indicates that the thread pool optimization should be applied
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     */
    public ShadowScheduledThreadPoolExecutor(
            final int corePoolSize,
            final String prefix,
            final boolean optimize
    ) {
        super(corePoolSize, new NamedThreadFactory(prefix));
        if (optimize) {
            if(getKeepAliveTime(TimeUnit.NANOSECONDS) <= 0L) {
                setKeepAliveTime(10L, TimeUnit.MILLISECONDS);
            }
            allowCoreThreadTimeOut(true);
        }
    }

    /**
     * Initialize {@code ScheduledThreadPoolExecutor} with new thread name, this constructor is used by {@code ThreadTransformer} for thread renaming
     *
     * @param corePoolSize    the number of threads to keep in the pool, even if they are idle,
     *                        unless {@code allowCoreThreadTimeOut} is set
     * @param threadFactory   the factory to use when the executor creates a new thread
     * @param prefix          the prefix of new thread
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     */
    public ShadowScheduledThreadPoolExecutor(
            final int corePoolSize,
            final ThreadFactory threadFactory,
            final String prefix
    ) {
        this(corePoolSize, threadFactory, prefix, false);
    }

    /**
     * Initialize {@code ScheduledThreadPoolExecutor} with new thread name, this constructor is used by {@code ThreadTransformer} for thread renaming
     *
     * @param corePoolSize    the number of threads to keep in the pool, even if they are idle,
     *                        unless {@code allowCoreThreadTimeOut} is set
     * @param threadFactory   the factory to use when the executor creates a new thread
     * @param prefix          the prefix of new thread
     * @param optimize        the value indicates that the thread pool optimization should be applied
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     */
    public ShadowScheduledThreadPoolExecutor(
            final int corePoolSize,
            final ThreadFactory threadFactory,
            final String prefix,
            final boolean optimize
    ) {
        super(corePoolSize, new NamedThreadFactory(threadFactory, prefix));
        if (optimize) {
            if(getKeepAliveTime(TimeUnit.NANOSECONDS) <= 0L) {
                setKeepAliveTime(10L, TimeUnit.MILLISECONDS);
            }
            allowCoreThreadTimeOut(true);
        }
    }

    /**
     * Initialize {@code ScheduledThreadPoolExecutor} with new thread name, this constructor is used by {@code ThreadTransformer} for thread renaming
     *
     * @param corePoolSize    the number of threads to keep in the pool, even if they are idle,
     *                        unless {@code allowCoreThreadTimeOut} is set
     * @param handler         the handler to use when execution is blocked because the thread bounds and queue capacities are reached
     * @param prefix          the prefix of new thread
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     */
    public ShadowScheduledThreadPoolExecutor(
            final int corePoolSize,
            final RejectedExecutionHandler handler,
            final String prefix
    ) {
        this(corePoolSize, handler, prefix, false);
    }

    /**
     * Initialize {@code ScheduledThreadPoolExecutor} with new thread name, this constructor is used by {@code ThreadTransformer} for thread renaming
     *
     * @param corePoolSize    the number of threads to keep in the pool, even if they are idle,
     *                        unless {@code allowCoreThreadTimeOut} is set
     * @param handler         the handler to use when execution is blocked because the thread bounds and queue capacities are reached
     * @param prefix          the prefix of new thread
     * @param optimize        the value indicates that the thread pool optimization should be applied
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     */
    public ShadowScheduledThreadPoolExecutor(
            final int corePoolSize,
            final RejectedExecutionHandler handler,
            final String prefix,
            final boolean optimize
    ) {
        super(corePoolSize, new NamedThreadFactory(prefix), handler);
        if (optimize) {
            if(getKeepAliveTime(TimeUnit.NANOSECONDS) <= 0L) {
                setKeepAliveTime(10L, TimeUnit.MILLISECONDS);
            }
            allowCoreThreadTimeOut(true);
        }
    }

    /**
     * Initialize {@code ScheduledThreadPoolExecutor} with new thread name, this constructor is used by {@code ThreadTransformer} for thread renaming
     *
     * @param corePoolSize    the number of threads to keep in the pool, even if they are idle,
     *                        unless {@code allowCoreThreadTimeOut} is set
     * @param threadFactory   the factory to use when the executor creates a new thread
     * @param handler         the handler to use when execution is blocked because the thread bounds and queue capacities are reached
     * @param prefix          the prefix of new thread
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     */
    public ShadowScheduledThreadPoolExecutor(
            final int corePoolSize,
            final ThreadFactory threadFactory,
            final RejectedExecutionHandler handler,
            final String prefix
    ) {
        this(corePoolSize, threadFactory, handler, prefix, false);
    }

    /**
     * Initialize {@code ScheduledThreadPoolExecutor} with new thread name, this constructor is used by {@code ThreadTransformer} for thread renaming
     *
     * @param corePoolSize    the number of threads to keep in the pool, even if they are idle,
     *                        unless {@code allowCoreThreadTimeOut} is set
     * @param threadFactory   the factory to use when the executor creates a new thread
     * @param handler         the handler to use when execution is blocked because the thread bounds and queue capacities are reached
     * @param prefix          the prefix of new thread
     * @param optimize        the value indicates that the thread pool optimization should be applied
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     */
    public ShadowScheduledThreadPoolExecutor(
            final int corePoolSize,
            final ThreadFactory threadFactory,
            final RejectedExecutionHandler handler,
            final String prefix,
            final boolean optimize
    ) {
        super(corePoolSize, new NamedThreadFactory(threadFactory, prefix), handler);
        if (optimize) {
            if(getKeepAliveTime(TimeUnit.NANOSECONDS) <= 0L) {
                setKeepAliveTime(10L, TimeUnit.MILLISECONDS);
            }
            allowCoreThreadTimeOut(true);
        }
    }

}
