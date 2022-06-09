package master.flame.danmaku.danmaku.util;

/**
 * Created by ch on 15-12-9.
 */
public class SystemClock {

    /**
     * 倍速播放导致的时间偏移
     */
    public static long timeOffset = 0;

    public static final long uptimeMillis() {
        return android.os.SystemClock.elapsedRealtime() + timeOffset;
    }

    public static final void sleep(long mills) {
        android.os.SystemClock.sleep(mills);
    }
}
