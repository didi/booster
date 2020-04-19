package android.content;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;

import java.io.File;

public abstract class Context {

    public abstract AssetManager getAssets();

    public abstract Resources getResources();

    public abstract Looper getMainLooper();

    public abstract Intent registerReceiver(final BroadcastReceiver receiver, final IntentFilter filter);

    public abstract Intent registerReceiver(final BroadcastReceiver receiver, final IntentFilter filter, final int flags);

    public abstract Intent registerReceiver(final BroadcastReceiver receiver, final IntentFilter filter, final String perm, final Handler handler);

    public abstract Intent registerReceiver(final BroadcastReceiver receiver, final IntentFilter filter, final String perm, final Handler handler, final int flags);

    public abstract void unregisterReceiver(final BroadcastReceiver receiver);

    public abstract Object getSystemService(final String name);

    public abstract Context getApplicationContext();

    public abstract File getFilesDir();

    public abstract String getPackageName();

}