package android.view;

public interface WindowManager extends ViewManager {

    public static class BadTokenException extends RuntimeException {
        public BadTokenException() {
            throw new RuntimeException("Stub!");
        }

        public BadTokenException(String name) {
            throw new RuntimeException("Stub!");
        }
    }

}
