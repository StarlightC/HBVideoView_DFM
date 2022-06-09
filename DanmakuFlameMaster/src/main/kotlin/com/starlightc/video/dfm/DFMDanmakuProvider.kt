package com.starlightc.video.dfm

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import com.starlightc.video.core.SimpleLogger
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.controller.IDanmakuView
import master.flame.danmaku.danmaku.manager.DanmakuProvider
import master.flame.danmaku.danmaku.model.*
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import master.flame.danmaku.danmaku.model.android.Danmakus
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.danmaku.parser.HBDanmakuObjParser
import master.flame.danmaku.danmaku.parser.IDataSource
import master.flame.danmaku.danmaku.parser.android.DanmakuObjectSource
import master.flame.danmaku.danmaku.parser.android.bean.DanmakuListObj
import master.flame.danmaku.ui.widget.DanmakuView

/**
 * @author StarlightC
 * @since 2022/6/9
 *
 * 对 烈焰弹幕使 的简单封装
 */
class DFMDanmakuProvider(private val context: Context) : DanmakuProvider<DanmakuView> {
    override val danmakuView: DanmakuView = DanmakuView(context)

    override var initialized: Boolean = false
        get() {
            if (!field) {
                SimpleLogger.instance.debugE("Error: Provider haven't be initialized")
            }
            return field
        }
    override var speed: Int = 100

    override fun init() {
        init(0)
    }

    override fun getDanmakuView(style: Int): DanmakuView {
        if (!initialized) {
            init(style)
        }
        return danmakuView
    }

    fun getDanmakuView(style: Int, danmakuListObj: DanmakuListObj?): DanmakuView {
        if (!initialized) {
            init(style, danmakuListObj)
        }
        return danmakuView
    }

    override fun addDanmaku(param: DanmakuParam) {
        val danmaku: BaseDanmaku =
            danmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL)
        danmaku.text = param.text
        danmaku.paddingHor = param.paddingHor
        danmaku.paddingVer = param.paddingVer
        danmaku.priority = 1
        danmaku.isLive = param.isLive
        danmaku.time = danmakuView.currentTime + 1000  //1s后显示自己发布的弹幕
        danmaku.textSize = param.textSize * context.resources.displayMetrics.density
        danmaku.sizeGap = 2f * context.resources.displayMetrics.density
        danmaku.shadowSize = 0.5f * context.resources.displayMetrics.density
        danmaku.textColor = param.textColor
        danmaku.textShadowColor = param.textShadowColor
        //danmaku.underlineColor = param.underlineColor
        //danmaku.borderColor = param.borderColor
        danmaku.danmakuID = param.danmakuID
        danmaku.userHash = param.userHash
        danmaku.tag = param.tag
        danmaku.isSelf = true
        danmaku.paddingVer = dp2px(context, 4f)
        danmaku.paddingHor = dp2px(context, 10f)
        danmakuView.addDanmaku(danmaku)
    }

    override fun removeDanmaku(danmaku: BaseDanmaku) {
        danmakuView.removeDanmaku(danmaku)
        danmakuView.invalidateDanmaku(danmaku, false)
    }

    override fun release() {
        danmakuView.release()
    }

    /**
     * 0 = 小屏
     * 1 = 全屏
     */
    override fun setScreenMode(mode: Int) {
        danmakuScreenMode = mode
        val isFullscreen = mode == 1
        danmakuContext.displayer.displayerConfig.isFullscreen = isFullscreen
        danmakuContext.displayer.cacheStuffer.isFullscreen = isFullscreen
        //for (danmaku in danmakuView.preAndCurrentDanmakus.collection) {
        //    danmakuView.invalidateDanmaku(danmaku, true)
        //}
    }

    var danmakuScreenMode:Int = 0
    var existPartMap = HashMap<Int, Boolean>()
    var currentDanmakuIndex: Int = 0     //当前弹幕列表序号
        get() {
            return if (segmentLength != 0L) {
                (currentPosition / segmentLength).toInt()
            } else {
                0
            }
        }
    var lastTime = 0L               //当前弹幕列表结束时间
    var duration = 0L               //视频长度
    var segmentLength = 180000L      //弹幕列表单页长度
    var difference = 0L
    var timerTime = 0L
    var currentPosition: Long = 0 // 当前播放时间
        set(value) {
            field = value
            difference = value - timerTime
            if (duration > 500 && field >= duration - 200) {
                danmakuView.pause()
            }
        }
    var onDanmakuClick: (danmakus: IDanmakus) -> Boolean = {
        val latest = it.last()
        if (null != latest) {
            latest.hideThisTime = true
            SimpleLogger.instance.debugD("onDanmakuClick: text of latest danmaku:" + latest.text)
            true
        } else {
            false
        }
    }
        set(value) {
            if (initialized) {
                field = value
            }
        }
    var onDanmakuLongClick: (danmakus: IDanmakus) -> Boolean = { false }
        set(value) {
            if (initialized) {
                field = value
            }
        }
    var onDanmakuViewClick: (v: IDanmakuView) -> Boolean = {
        SimpleLogger.instance.debugD("DanmakuView Click")
        false
    }
        set(value) {
            if (initialized) {
                field = value
            }
        }

    val danmakuContext: DanmakuContext = DanmakuContext.create()

    lateinit var danmakuParser: BaseDanmakuParser

    private val maxLinePair = HashMap<Int, Int>()
    private val overlappingEnablePair = HashMap<Int, Boolean>()

    private fun createParser(danmakuListObj: DanmakuListObj?): BaseDanmakuParser {
        if (danmakuListObj == null) {
            return object : BaseDanmakuParser() {
                override fun parse(): Danmakus {
                    return Danmakus()
                }
            }
        }
        val parser: BaseDanmakuParser = HBDanmakuObjParser(context)
        val dataSource: IDataSource<*> = DanmakuObjectSource(danmakuListObj)
        parser.load(dataSource)
        return parser
    }

    fun setDanmakuCallback(callback: DrawHandler.Callback?) {
        if (initialized) {
            callback?.let {
                danmakuView.setCallback(it)
            }
        }
    }

    fun setMaxLine(danmakuType: Int, lines: Int) {
        maxLinePair[danmakuType] = lines
    }

    fun setOverLappingEnable(danmakuType: Int, enable: Boolean) {
        overlappingEnablePair[danmakuType] = enable
    }

    /**
     * 初始化
     * @param style 弹幕样式 0-默认
     */
    fun init(style: Int) {
        init(style, null)
    }

    fun dp2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun init(style: Int, danmakuListObj: DanmakuListObj?) {
        val lp = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        danmakuView.layoutParams = lp

        maxLinePair[BaseDanmaku.TYPE_SCROLL_RL] = 5
        overlappingEnablePair[BaseDanmaku.TYPE_SCROLL_RL] = true
        overlappingEnablePair[BaseDanmaku.TYPE_FIX_TOP] = true

        val refreshRate = (context as Activity).windowManager.defaultDisplay.refreshRate

        danmakuContext
            .setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 0.5f)
            .setDuplicateMergingEnabled(false)
            .setScrollSpeedFactor(1f)
            .setScaleTextSize(1f)
            .setCacheStuffer(SpannedCacheStuffer(context), null)
            .setMaximumLines(maxLinePair)
            .setRefreshTime((1000 / refreshRate).toInt())
            .preventOverlapping(overlappingEnablePair)
            .setDanmakuMargin(dp2px(context, 40f))

        danmakuParser = createParser(danmakuListObj)
        danmakuView.setCallback(object : DrawHandler.Callback {
            override fun updateTimer(timer: DanmakuTimer) {
                timerTime = timer.currMillisecond
                if (speed != 100) {
                    val offset = (timer.lastInterval() * (speed - 100) / 100f).toLong()
                    timer.add(offset)
                    timer.addOffset(offset)
                }
            }

            override fun drawingFinished() {
            }

            override fun danmakuShown(danmaku: BaseDanmaku) {

            }

            override fun prepared() {}
        })
        danmakuView.onDanmakuClickListener = object : IDanmakuView.OnDanmakuClickListener {
            override fun onDanmakuClick(danmakus: IDanmakus): Boolean {
                return onDanmakuClick.invoke(danmakus)
            }

            override fun onDanmakuLongClick(danmakus: IDanmakus): Boolean {
                return onDanmakuLongClick.invoke(danmakus)
            }

            override fun onViewClick(view: IDanmakuView): Boolean {
                return onDanmakuViewClick.invoke(view)
            }
        }
        danmakuView.prepare(danmakuParser, danmakuContext)
        danmakuView.showFPS(false)
        danmakuView.enableDanmakuDrawingCache(true)

        initialized = true
    }

    fun createDanmaku(param: DanmakuParam): BaseDanmaku? {
        val danmaku: BaseDanmaku? = danmakuContext.mDanmakuFactory.createDanmaku(param.type)
        val textSize = param.textSize * context.resources.displayMetrics.density
        val shadowSize = 0.5f * context.resources.displayMetrics.density
        danmaku?.paddingHor = param.paddingHor
        danmaku?.paddingVer = param.paddingVer
        danmaku?.priority = param.priority
        danmaku?.isLive = param.isLive
        danmaku?.time = param.time
        danmaku?.textSize = textSize
        danmaku?.sizeGap = 2 * context.resources.displayMetrics.density
        danmaku?.shadowSize = shadowSize
        danmaku?.text = param.text
        danmaku?.textColor = param.textColor
        danmaku?.textShadowColor = param.textShadowColor
        //danmaku?.underlineColor = param.underlineColor
        //danmaku?.borderColor = param.borderColor
        danmaku?.danmakuID = param.danmakuID
        danmaku?.userHash = param.userHash
        danmaku?.tag = param.tag
        //if (currentUserID!= null && currentUserID == param.userHash) {
        //    danmaku?.isSelf = true
        //}
        danmaku?.paddingVer = dp2px(context, 4f)
        danmaku?.paddingHor = dp2px(context, 10f)
        return danmaku
    }

}