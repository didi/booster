package android.app;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;

public class Application extends ContextWrapper {

    public interface ActivityLifecycleCallbacks {
        void onActivityCreated(Activity activity, Bundle savedInstanceState);
        void onActivityDestroyed(Activity activity);
        void onActivityPaused(Activity activity);
        void onActivityResumed(Activity activity);
        void onActivitySaveInstanceState(Activity activity, Bundle outState);
        void onActivityStarted(Activity activity);
        void onActivityStopped(Activity activity);
    }

    public Application() {
        super(null);
    }

    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callbacks) {
        throw new RuntimeException("Stub!");
    }

    final void attach(final Context context) {
        attachBaseContext(context);
    }

    /**
     * Returns the name of the current process.
     * Added in API level 28
     */
    public static String getProcessName() {
        throw new RuntimeException("Stub!");
    }

}
