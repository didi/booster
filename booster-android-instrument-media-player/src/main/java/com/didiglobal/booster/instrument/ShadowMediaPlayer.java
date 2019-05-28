package com.didiglobal.booster.instrument;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import com.didiglobal.booster.android.bugfix.CaughtCallback;
import com.didiglobal.booster.android.bugfix.Constants;

import static com.didiglobal.booster.android.bugfix.Reflection.getFieldValue;
import static com.didiglobal.booster.android.bugfix.Reflection.setFieldValue;

/**
 * Shadow of {@code android.media.MediaPlayer}
 *
 * @author johnsonlee
 */
public final class ShadowMediaPlayer implements Constants {

    public static MediaPlayer newMediaPlayer() {
        return workaround(new MediaPlayer());
    }

    public static MediaPlayer create(Context context, Uri uri) {
        return workaround(MediaPlayer.create(context, uri));
    }

    public static MediaPlayer create(final Context context, final Uri uri, final SurfaceHolder holder) {
        return workaround(MediaPlayer.create(context, uri, holder));
    }

    public static MediaPlayer create(final Context context, final Uri uri, final SurfaceHolder holder, final AudioAttributes audioAttributes, final int audioSessionId) {
        return workaround(MediaPlayer.create(context, uri, holder, audioAttributes, audioSessionId));
    }

    public static MediaPlayer create(final Context context, final int resid) {
        return workaround(MediaPlayer.create(context, resid));
    }

    public static MediaPlayer create(final Context context, final int resid, final AudioAttributes audioAttributes, final int audioSessionId) {
        return workaround(MediaPlayer.create(context, resid, audioAttributes, audioSessionId));
    }

    private static MediaPlayer workaround(final MediaPlayer player) {
        try {
            final Handler handler = getEventHandler(player);
            if (null == handler || !setFieldValue(handler, "mCallback", new CaughtCallback(handler))) {
                Log.i(TAG, "Hook MediaPlayer.mEventHandler.mCallback failed");
            }
        } catch (final Throwable t) {
            Log.e(TAG, "Hook MediaPlayer.mEventHandler.mCallback failed", t);
        }

        return player;
    }

    private static Handler getEventHandler(final MediaPlayer player) {
        Handler handler;

        if (null != (handler = getFieldValue(player, "mEventHandler"))) {
            return handler;
        }

        try {
            if (null != (handler = getFieldValue(player, Class.forName("android.media.MediaPlayer$EventHandler")))) {
                return handler;
            }
        } catch (final ClassNotFoundException e) {
            Log.w(TAG, e.getMessage(), e);
        }

        return null;
    }

    private ShadowMediaPlayer() {
    }

}
