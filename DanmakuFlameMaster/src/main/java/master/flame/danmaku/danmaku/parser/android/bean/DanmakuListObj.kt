package master.flame.danmaku.danmaku.parser.android.bean

import java.io.Serializable

class DanmakuListObj(
    var index:Int = -1,
    var danmakus: List<DanmakuObj>?
): Serializable
