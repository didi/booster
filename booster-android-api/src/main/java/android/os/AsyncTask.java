package android.os;

import java.util.concurrent.Executor;

public abstract class AsyncTask<Params, Progress, Result> {

    private static final Executor STUB = new Executor() {
        @Override
        public void execute(Runnable command) {
            throw new RuntimeException("Stub!");
        }
    };

    public static final Executor THREAD_POOL_EXECUTOR = STUB;

    public static final Executor SERIAL_EXECUTOR = STUB;

    public static void setDefaultExecutor(final Executor executor) {
        throw new RuntimeException("Stub!");
    }

}
