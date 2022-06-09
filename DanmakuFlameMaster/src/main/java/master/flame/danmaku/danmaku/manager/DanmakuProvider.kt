package master.flame.danmaku.danmaku.manager

import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuParam

/**
 * 提供弹幕功能实现
 */
interface DanmakuProvider<T> {
    var initialized: Boolean

    val danmakuView: T

    var speed: Int

    fun getDanmakuView(style: Int): T

    fun init()

    fun addDanmaku(param: DanmakuParam)

    fun removeDanmaku(danmaku: BaseDanmaku)

    fun release()

    fun setScreenMode(mode :Int)
}