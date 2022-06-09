package master.flame.danmaku.danmaku.manager

import androidx.lifecycle.MutableLiveData
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuParam
import master.flame.danmaku.danmaku.model.IDanmakus

/**
 * 管理弹幕数据状态，暴露相关操作方法
 */
interface DanmakuManager {
    var isReadyLD: MutableLiveData<Boolean>

    var provider: DanmakuProvider<*>?

    var onReportDanmakuAction: (danmaku: BaseDanmaku) -> Unit

    fun showFloatDanmaku(danmaku: BaseDanmaku, marginTop: Int)

    fun hideFloatDanmaku()

    /**
     * 开始状态同步
     */
    fun sync()

    /**
     * 清空全部弹幕数据
     */
    fun clear()

    fun replay()

    fun sendDanmaku(param: DanmakuParam, linkID: String, text: String, isFullscreen: Boolean)

    fun refreshCurrentPosition()

    fun release()
}