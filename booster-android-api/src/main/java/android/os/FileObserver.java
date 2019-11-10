package android.os;

public abstract class FileObserver {
    public static final int CLOSE_WRITE = 8;
    public static final int DELETE = 512;

    public FileObserver(String path, int mask) {
        throw new RuntimeException("Stub!");
    }

    protected void finalize() {
        throw new RuntimeException("Stub!");
    }

    public void startWatching() {
        throw new RuntimeException("Stub!");
    }

    public void stopWatching() {
        throw new RuntimeException("Stub!");
    }

    public abstract void onEvent(int var1, String var2);
}
