package master.flame.danmaku.danmaku.parser

import android.content.Context
import android.graphics.Color
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.IDanmakus
import master.flame.danmaku.danmaku.model.android.Danmakus
import master.flame.danmaku.danmaku.parser.android.DanmakuObjectSource
import master.flame.danmaku.danmaku.parser.android.bean.DanmakuListObj
import master.flame.danmaku.danmaku.parser.android.bean.DanmakuObj
import org.json.JSONException
import java.lang.NumberFormatException

/**
 * 解析弹幕对象数据
 */
class HBDanmakuObjParser(val context: Context): BaseDanmakuParser() {
    override fun parse(): IDanmakus {
        if(mDataSource != null && mDataSource is DanmakuObjectSource) {
            val jsonSource: DanmakuObjectSource = mDataSource as DanmakuObjectSource
            return doParse(jsonSource.data())
        }
        return Danmakus()
    }

    /**
     * 解析弹幕
     * @param danmakuListData 弹幕数据
     * @return 转换后的Danmakus
     */
    private fun doParse(danmakuListData: DanmakuListObj?): Danmakus {
        var danmakus = Danmakus()
        if (danmakuListData == null || danmakuListData.danmakus?.isEmpty() != false) {
            return danmakus
        }
        danmakus = parseObj(danmakuListData.danmakus, danmakus)
        return danmakus
    }

    /**
     * 对Obj数据进行解析，生成相应弹幕实例并添加至danmakus中
     */
    private fun parseObj(danmakuList: List<DanmakuObj>?, danmakuInfo: Danmakus?): Danmakus {
        var danmakus: Danmakus? = danmakuInfo
        if (danmakus == null) {
            danmakus = Danmakus()
        }
        if (danmakuList == null || danmakuList.isEmpty()) {
            return danmakus
        }
        for ((i, obj) in danmakuList.withIndex()) {
            try {
                val type = obj.type ?: 1 // 弹幕类型
                if (type == 7) // FIXME : hard code
                    continue
                val time = obj.video_time ?: 0L // 出现时间
                val color = Color.parseColor(obj.color)// 颜色
                val textSize = obj.size ?: 15F // 字体大小
                val item: BaseDanmaku? = mContext.mDanmakuFactory.createDanmaku(type, mContext)
                if (item != null) {
                    item.time = time
                    item.textSize = textSize * context.resources.displayMetrics.density + 0.5f
                    item.textColor =  color
                    item.textShadowColor =
                        if (color <= Color.BLACK) Color.WHITE else Color.BLACK
                    item.index = i
                    item.flags = mContext.mGlobalFlagValues
                    item.timer = mTimer
                    item.text = obj.text
                    danmakus.addItem(item)
                }

            } catch (e: JSONException) {
            } catch (e: NumberFormatException) {
            }
        }
        return danmakus
    }
}