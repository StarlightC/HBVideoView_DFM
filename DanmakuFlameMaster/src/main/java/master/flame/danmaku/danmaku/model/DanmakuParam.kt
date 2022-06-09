package master.flame.danmaku.danmaku.model

import android.graphics.Color
import java.io.Serializable

class DanmakuParam: Serializable {
    /**
     * 类型
     */
    var type: Int = BaseDanmaku.TYPE_SCROLL_RL
    /**
     * 显示时间(ms)
     */
    var time: Long = 5000L

    /**
     * 偏移时间
     */
    var timeOffset: Long = 0L

    /**
     * 弹幕文本内容
     */
    var text: CharSequence? = null

    /**
     * 文本颜色
     */
    var textColor = Color.WHITE

    /**
     * 文本字号
     */
    var textSize = 15f

    /**
     * 文本阴影颜色
     */
    var textShadowColor = 0x4D000000

    /**
     * 下划线颜色
     */
    var underlineColor = Color.WHITE

    /**
     * 边框颜色
     */
    var borderColor = Color.RED

    /**
     * 弹幕发布者id, 0表示游客
     */
    var userId: Int = 0

    /**
     * 弹幕发布者id
     */
    var userHash: String? = null

    /**
     * 水平内边距(像素)
     */
    var paddingHor = 0

    /**
     * 纵向内边距(px)
     */
    var paddingVer = 0

    /**
     * 弹幕优先级,0为低优先级,>0为高优先级不会被过滤器过滤
     */
    var priority: Byte = 0

    var isLive: Boolean = false
    /**
     * 透明度
     */
    var alpha: Int = 255

    /**
     *是否为当前用户发送的弹幕
     */
    var isSelf: Boolean = false


    /**
     * 弹幕ID dm_id
     */
    var danmakuID: String? = null

    /**
     * 可保存一些自定义数据的引用(外部使用).
     * 除非主动set null,否则不会自动释放引用.
     * 确定你会主动set null, 否则不要使用这个字段引用大内存的对象实例.
     */
    var tag: Any? = null
}