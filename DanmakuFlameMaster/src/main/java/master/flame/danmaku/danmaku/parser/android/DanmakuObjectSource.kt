package master.flame.danmaku.danmaku.parser.android

import master.flame.danmaku.danmaku.parser.IDataSource
import master.flame.danmaku.danmaku.parser.android.bean.DanmakuListObj

class DanmakuObjectSource(private val danmakuListObj: DanmakuListObj): IDataSource<DanmakuListObj> {
    override fun data(): DanmakuListObj {
        return danmakuListObj
    }

    override fun release() {
        // do nothing
    }
}