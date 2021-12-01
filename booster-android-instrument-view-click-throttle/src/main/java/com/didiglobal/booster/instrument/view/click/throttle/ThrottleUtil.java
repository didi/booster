package com.didiglobal.booster.instrument.view.click.throttle;

import android.os.SystemClock;
import android.view.View;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThrottleUtil {
    private static final int CACHE_SIZE = 64;
    private static final Map<String, Long> KEY_MILLIS_MAP = new ConcurrentHashMap<>(CACHE_SIZE);
    private static long GLOBAL_LAST_CLICK_TIME = 0L;
    private static long GLOBAL_THROTTLE_TIME = 200L;
    private static boolean GLOBAL = true;
    private static long TEMP_THROTTLE_TIME = -1;

    public static void setGlobalDuration(long time) {
        GLOBAL_THROTTLE_TIME = time;
    }

    public static void setGlobal(boolean global) {
        GLOBAL = global;
    }

    public static boolean check(View view) {
        long curTime = SystemClock.elapsedRealtime();
        long duration = getDuration();
        if (GLOBAL) {
            boolean canClick = curTime - GLOBAL_LAST_CLICK_TIME >= duration;
            if (canClick) GLOBAL_LAST_CLICK_TIME = curTime;
            return canClick;
        } else {
            String id = String.valueOf(view.hashCode());
            Long lastTimeWithDuration = KEY_MILLIS_MAP.get(id);
            if (lastTimeWithDuration == null) {
                lastTimeWithDuration = 0L;
            }
            boolean canClick = curTime >= lastTimeWithDuration;
            if (canClick) {
                KEY_MILLIS_MAP.put(id, curTime + duration);
            }
            clearMapIfNecessary(curTime);
            return canClick;
        }
    }

    private static void clearMapIfNecessary(long curTime) {
        if (KEY_MILLIS_MAP.size() < CACHE_SIZE) return;
        for (Iterator<Map.Entry<String, Long>> it = KEY_MILLIS_MAP.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Long> entry = it.next();
            Long validTime = entry.getValue();
            if (curTime >= validTime) {
                it.remove();
            }
        }
    }

    private static long getDuration() {
        long duration = GLOBAL_THROTTLE_TIME;
        if (TEMP_THROTTLE_TIME >= 0) {
            duration = TEMP_THROTTLE_TIME;
            TEMP_THROTTLE_TIME = -1;
        }
        return duration;
    }

    public static void setDuration(long tempThrottleTime) {
        TEMP_THROTTLE_TIME = tempThrottleTime;
    }
}
