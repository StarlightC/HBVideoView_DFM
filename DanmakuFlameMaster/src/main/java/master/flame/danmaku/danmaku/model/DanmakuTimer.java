/*
 * Copyright (C) 2013 Chen Hui <calmer91@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package master.flame.danmaku.danmaku.model;

import master.flame.danmaku.danmaku.util.SystemClock;

public class DanmakuTimer {
    public long currMillisecond;

    private long lastInterval;

    /**
     * 倍速播放导致的时间偏移
     */
    private long offset = 0L;

    public DanmakuTimer() {
        SystemClock.timeOffset = offset;
    }

    public DanmakuTimer(long curr) {
        update(curr);
        SystemClock.timeOffset = offset;
    }

    public long update(long curr) {
        lastInterval = curr - currMillisecond;
        currMillisecond = curr;
        return lastInterval;
    }

    public long add(long mills) {
        return update(currMillisecond + mills);
    }

    public void addOffset(long timeOffset) {
        offset += timeOffset;
        SystemClock.timeOffset = offset;
    }

    public long lastInterval() {
        return lastInterval;
    }

}
