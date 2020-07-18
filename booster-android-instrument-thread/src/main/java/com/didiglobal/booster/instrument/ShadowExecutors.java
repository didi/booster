package com.didiglobal.booster.instrument;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class ShadowExecutors {

    /**
     * The minimum pool size, {@link ScheduledThreadPoolExecutor} with core pool size 0 cause high processor load.
     * This is a bug of JDK that has been fixed in Java 9
     *
     * @see <a href="https://bugs.openjdk.java.net/browse/JDK-8129861">JDK-8129861</a>
     * @see <a href="https://bugs.openjdk.java.net/browse/JDK-8022642>JDK-8022642/a>
     */
    private static final int MIN_POOL_SIZE = 1;

    private static final int NCPU = Runtime.getRuntime().availableProcessors();

    /**
     * The maximum pool size
     */
    private static final int MAX_POOL_SIZE = (NCPU << 1) + 1;

    /**
     * The default keep alive time for idle threads
     */
    private static final long DEFAULT_KEEP_ALIVE = 30000L;

    public static ThreadFactory defaultThreadFactory(final String name) {
        return new NamedThreadFactory(name);
    }

    // <editor-fold desc="- named fixed thread pool">

    public static ExecutorService newFixedThreadPool(final int nThreads, final String name) {
        return Executors.newFixedThreadPool(nThreads, new NamedThreadFactory(name));
    }

    public static ExecutorService newFixedThreadPool(final int nThreads, final ThreadFactory factory, final String name) {
        return Executors.newFixedThreadPool(nThreads, new NamedThreadFactory(factory, name));
    }

    // </editor-fold>

    // <editor-fold desc="- named single thread executor">

    public static ExecutorService newSingleThreadExecutor(final String name) {
        return Executors.newSingleThreadExecutor(new NamedThreadFactory(name));
    }

    public static ExecutorService newSingleThreadExecutor(final ThreadFactory factory, final String name) {
        return Executors.newSingleThreadExecutor(new NamedThreadFactory(factory, name));
    }

    // </editor-fold>

    // <editor-fold desc="- named cached thread pool">

    public static ExecutorService newCachedThreadPool(final String name) {
        return Executors.newCachedThreadPool(new NamedThreadFactory(name));
    }

    public static ExecutorService newCachedThreadPool(final ThreadFactory factory, final String name) {
        return Executors.newCachedThreadPool(new NamedThreadFactory(factory, name));
    }

    // </editor-fold>

    // <editor-fold desc="- named single thread scheduled executor">

    public static ScheduledExecutorService newSingleThreadScheduledExecutor(final String name) {
        return Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(name));
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor(final ThreadFactory factory, final String name) {
        return Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(factory, name));
    }

    // </editor-fold>

    // <editor-fold desc="- named scheduled thread pool">

    public static ScheduledExecutorService newScheduledThreadPool(final int corePoolSize, final String name) {
        return Executors.newScheduledThreadPool(corePoolSize, new NamedThreadFactory(name));
    }

    public static ScheduledExecutorService newScheduledThreadPool(final int corePoolSize, final ThreadFactory factory, final String name) {
        return Executors.newScheduledThreadPool(corePoolSize, new NamedThreadFactory(factory, name));
    }

    // </editor-fold>

    // <editor-fold desc="- named work stealing pool">

    public static ExecutorService newWorkStealingPool(final String name) {
        return new ForkJoinPool(Runtime.getRuntime().availableProcessors(), new NamedForkJoinWorkerThreadFactory(name), null, true);
    }

    public static ExecutorService newWorkStealingPool(final int parallelism, final String name) {
        return new ForkJoinPool(parallelism, new NamedForkJoinWorkerThreadFactory(name), null, true);
    }

    // </editor-fold>

    // <editor-fold desc="- optimized fixed thread pool">

    public static ExecutorService newOptimizedFixedThreadPool(final int nThreads, final String name) {
        return Executors.newFixedThreadPool(nThreads, new NamedThreadFactory(name));
    }

    public static ExecutorService newOptimizedFixedThreadPool(final int nThreads, final ThreadFactory factory, final String name) {
        return Executors.newFixedThreadPool(nThreads, new NamedThreadFactory(factory, name));
    }

    // </editor-fold>

    // <editor-fold desc="* optimized single thread scheduled executor">

    public static ScheduledExecutorService newOptimizedSingleThreadScheduledExecutor(final String name) {
        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory(name));
        executor.setKeepAliveTime(DEFAULT_KEEP_ALIVE, TimeUnit.MILLISECONDS);
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    public static ScheduledExecutorService newOptimizedSingleThreadScheduledExecutor(final ThreadFactory factory, final String name) {
        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory(factory, name));
        executor.setKeepAliveTime(DEFAULT_KEEP_ALIVE, TimeUnit.MILLISECONDS);
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    // </editor-fold>

    // <editor-fold desc="* optimized scheduled thread pool">

    public static ScheduledExecutorService newOptimizedScheduledThreadPool(final int corePoolSize, final String name) {
        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(min(max(1, corePoolSize), MAX_POOL_SIZE), new NamedThreadFactory(name));
        executor.setKeepAliveTime(DEFAULT_KEEP_ALIVE, TimeUnit.MILLISECONDS);
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    public static ScheduledExecutorService newOptimizedScheduledThreadPool(final int corePoolSize, final ThreadFactory factory, final String name) {
        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(min(max(1, corePoolSize), MAX_POOL_SIZE), new NamedThreadFactory(factory, name));
        executor.setKeepAliveTime(DEFAULT_KEEP_ALIVE, TimeUnit.MILLISECONDS);
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    // </editor-fold>

    // <editor-fold desc="* optimized single thread executor">

    public static ExecutorService newOptimizedSingleThreadExecutor(final String name) {
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(0, 1, DEFAULT_KEEP_ALIVE, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory(name));
        executor.allowCoreThreadTimeOut(true);
        return new FinalizableDelegatedExecutorService(executor);
    }

    public static ExecutorService newOptimizedSingleThreadExecutor(final ThreadFactory factory, final String name) {
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(0, 1, DEFAULT_KEEP_ALIVE, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory(factory, name));
        executor.allowCoreThreadTimeOut(true);
        return new FinalizableDelegatedExecutorService(executor);
    }

    // </editor-fold>

    //<editor-fold desc="* optimized cached thread pool">

    public static ExecutorService newOptimizedCachedThreadPool(final String name) {
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new NamedThreadFactory(name));
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    public static ExecutorService newOptimizedCachedThreadPool(final ThreadFactory factory, final String name) {
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new NamedThreadFactory(factory, name));
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    //</editor-fold>

    // <editor-fold desc="* optimized work stealing pool">

    public static ExecutorService newOptimizedWorkStealingPool(final String name) {
        return new ForkJoinPool(Runtime.getRuntime().availableProcessors(), new NamedForkJoinWorkerThreadFactory(name), null, true);
    }

    public static ExecutorService newOptimizedWorkStealingPool(final int parallelism, final String name) {
        return new ForkJoinPool(parallelism, new NamedForkJoinWorkerThreadFactory(name), null, true);
    }

    // </editor-fold>

    private static class DelegatedExecutorService extends AbstractExecutorService {

        private final ExecutorService executor;

        DelegatedExecutorService(final ExecutorService executor) {
            this.executor = executor;
        }

        @Override
        public void execute(final Runnable command) {
            this.executor.execute(command);
        }

        @Override
        public void shutdown() {
            this.executor.shutdown();
        }

        @Override
        public List<Runnable> shutdownNow() {
            return this.executor.shutdownNow();
        }

        @Override
        public boolean isShutdown() {
            return this.executor.isShutdown();
        }

        @Override
        public boolean isTerminated() {
            return this.executor.isTerminated();
        }

        @Override
        public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
            return this.executor.awaitTermination(timeout, unit);
        }

        @Override
        public Future<?> submit(final Runnable task) {
            return this.executor.submit(task);
        }

        @Override
        public <T> Future<T> submit(final Callable<T> task) {
            return this.executor.submit(task);
        }

        @Override
        public <T> Future<T> submit(final Runnable task, final T result) {
            return this.executor.submit(task, result);
        }

        @Override
        public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks) throws InterruptedException {
            return this.executor.invokeAll(tasks);
        }

        @Override
        public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit) throws InterruptedException {
            return this.executor.invokeAll(tasks, timeout, unit);
        }

        @Override
        public <T> T invokeAny(final Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
            return this.executor.invokeAny(tasks);
        }

        @Override
        public <T> T invokeAny(final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return this.executor.invokeAny(tasks, timeout, unit);
        }

    }

    private static final class FinalizableDelegatedExecutorService extends DelegatedExecutorService {

        FinalizableDelegatedExecutorService(final ExecutorService executor) {
            super(executor);
        }

        @Override
        protected void finalize() {
            super.shutdown();
        }

    }

}
