package android.os;

public final class MessageQueue {

    public static interface IdleHandler {
        boolean queueIdle();
    }

    public void addIdleHandler(final IdleHandler handler) {
        throw new RuntimeException("Stub!");
    }

    public void removeIdleHandler(final IdleHandler handler) {
        throw new RuntimeException("Stub!");
    }

}
