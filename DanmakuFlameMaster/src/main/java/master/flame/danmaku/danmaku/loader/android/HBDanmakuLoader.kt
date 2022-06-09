package master.flame.danmaku.danmaku.loader.android

import android.net.Uri
import master.flame.danmaku.danmaku.loader.ILoader
import master.flame.danmaku.danmaku.loader.IllegalDataException
import master.flame.danmaku.danmaku.parser.android.JSONSource
import java.io.InputStream
import java.lang.Exception

class HBDanmakuLoader private constructor() : ILoader {
    companion object {
        @JvmStatic
        val instance:HBDanmakuLoader by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            HBDanmakuLoader()
        }
    }

    private var dataSource: JSONSource? = null

    override fun getDataSource(): JSONSource? {
        return dataSource
    }

    @Throws(IllegalDataException::class)
    override fun load(uri: String?) {
        dataSource = try {
            JSONSource(Uri.parse(uri))
        } catch (e: Exception) {
            throw IllegalDataException(e)
        }
    }

    @Throws(IllegalDataException::class)
    override fun load(`in`: InputStream?) {
        dataSource = try {
            JSONSource(`in`)
        } catch (e: Exception) {
            throw IllegalDataException(e)
        }
    }
}