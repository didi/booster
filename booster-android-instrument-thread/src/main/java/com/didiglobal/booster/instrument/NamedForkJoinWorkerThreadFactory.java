package com.didiglobal.booster.instrument;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

import static com.didiglobal.booster.instrument.ShadowThread.makeThreadName;

/**
 * @author johnsonlee
 */
public class NamedForkJoinWorkerThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {

    private final String name;
    private final ForkJoinPool.ForkJoinWorkerThreadFactory factory;

    public NamedForkJoinWorkerThreadFactory(final ForkJoinPool.ForkJoinWorkerThreadFactory factory, final String name) {
        this.factory = factory;
        this.name = name;
    }

    public NamedForkJoinWorkerThreadFactory(final String name) {
        this(null, name);
    }

    @Override
    public ForkJoinWorkerThread newThread(final ForkJoinPool pool) {
        final ForkJoinWorkerThread thread = (null != this.factory)
                ? factory.newThread(pool)
                : new ForkJoinWorkerThread(pool) {};
        thread.setName(makeThreadName(thread.getName(), this.name));
        return thread;
    }

}
