package android.content;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;

import java.io.File;

public class ContextWrapper extends Context {

    public ContextWrapper(final Context base) {
        throw new RuntimeException("Stub!");
    }

    protected void attachBaseContext(final Context base) {
        throw new RuntimeException("Stub!");
    }

    @Override
    public AssetManager getAssets() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public Resources getResources() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public Looper getMainLooper() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public Intent registerReceiver(final BroadcastReceiver receiver, final IntentFilter filter) {
        throw new RuntimeException("Stub!");
    }

    @Override
    public Intent registerReceiver(final BroadcastReceiver receiver, final IntentFilter filter, final int flags) {
        throw new RuntimeException("Stub!");
    }

    @Override
    public Intent registerReceiver(final BroadcastReceiver receiver, final IntentFilter filter, final String perm, final Handler handler) {
        throw new RuntimeException("Stub!");
    }

    @Override
    public Intent registerReceiver(final BroadcastReceiver receiver, final IntentFilter filter, final String perm, final Handler handler, final int flags) {
        throw new RuntimeException("Stub!");
    }

    @Override
    public void unregisterReceiver(final BroadcastReceiver receiver) {
        throw new RuntimeException("Stub!");
    }

    @Override
    public Object getSystemService(final String name) {
        throw new RuntimeException("Stub!");
    }

    @Override
    public Context getApplicationContext() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public File getFilesDir() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public String getPackageName() {
        throw new RuntimeException("Stub!");
    }

}