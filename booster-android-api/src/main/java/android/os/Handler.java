package android.os;

public class Handler {

    public Handler() {
        throw new RuntimeException("Stub!");
    }

    public Handler(final Looper looper) {
        throw new RuntimeException("Stub!");
    }

    public final void removeCallbacks(final Runnable runnable) {
        throw new RuntimeException("Stub!");
    }

    public final boolean post(final Runnable runnable) {
        throw new RuntimeException("Stub!");
    }

    public final boolean postDelayed(final Runnable runnable, final long delayInMillis) {
        throw new RuntimeException("Stub!");
    }

    public void handleMessage(final Message msg) {
        throw new RuntimeException("Stub!");
    }

    public final Looper getLooper() {
        throw new RuntimeException("Stub!");
    }

    public interface Callback {
        public boolean handleMessage(final Message msg);
    }

}
