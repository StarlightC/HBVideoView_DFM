package master.flame.danmaku.danmaku.parser.android.bean

import java.io.Serializable

data class DanmakuObj(
    var video_time: Long?,
    var type: Int?,
    var size: Float?,
    var color: String?,
    var userid: String?,
    var dm_id: Long?,
    var release_time: String?,
    var text: String?
): Serializable