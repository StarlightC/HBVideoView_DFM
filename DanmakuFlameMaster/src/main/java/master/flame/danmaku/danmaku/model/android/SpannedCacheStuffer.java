package master.flame.danmaku.danmaku.model.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.Layout;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

import java.lang.ref.SoftReference;

import master.flame.danmaku.R;
import master.flame.danmaku.danmaku.model.BaseDanmaku;

/**
 * Created by ch on 15-7-16.
 */
public class SpannedCacheStuffer extends SimpleTextCacheStuffer {

    public SpannedCacheStuffer(Context context){
        this.context = context;
    }

    private Context context;

    @Override
    public void measure(BaseDanmaku danmaku, TextPaint paint, boolean fromWorkerThread) {
        if (danmaku.text instanceof Spanned) {
            CharSequence text = danmaku.text;
            if (text != null) {
                StaticLayout staticLayout = new StaticLayout(text, paint, (int) Math.ceil(StaticLayout.getDesiredWidth(danmaku.text, paint)), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
                danmaku.paintWidth = staticLayout.getWidth();
                danmaku.paintHeight = staticLayout.getHeight();
                danmaku.obj = new SoftReference<>(staticLayout);
                return;
            }
        }
        super.measure(danmaku, paint, fromWorkerThread);
    }

    @Override
    public void drawStroke(BaseDanmaku danmaku, String lineText, Canvas canvas, float left, float top, Paint paint) {
        if (danmaku.obj == null) {
            super.drawStroke(danmaku, lineText, canvas, left, top, paint);
        } else {
            SoftReference<StaticLayout> reference = (SoftReference<StaticLayout>) danmaku.obj;
            StaticLayout staticLayout = reference.get();
            boolean requestRemeasure = 0 != (danmaku.requestFlags & BaseDanmaku.FLAG_REQUEST_REMEASURE);
            boolean requestInvalidate = 0 != (danmaku.requestFlags & BaseDanmaku.FLAG_REQUEST_INVALIDATE);

            if (requestInvalidate || staticLayout == null) {
                if (requestInvalidate) {
                    danmaku.requestFlags &= ~BaseDanmaku.FLAG_REQUEST_INVALIDATE;
                }
                CharSequence text = danmaku.text;
                if (text != null) {
                    TextPaint textPaint;
                    float textSize;
                    if (paint instanceof TextPaint) {
                        textPaint = (TextPaint)paint;
                        textSize = textPaint.getTextSize();
                    } else {
                        textPaint = new TextPaint();
                        if (isFullscreen) {
                            textSize = danmaku.textSize  + danmaku.sizeGap;
                        } else {
                            textSize = danmaku.textSize;
                        }
                    }
                    textPaint.setColor(danmaku.textShadowColor);
                    textPaint.setTextSize(textSize + danmaku.shadowSize);
                    if (requestRemeasure) {
                        staticLayout = new StaticLayout(text, textPaint, (int) Math.ceil(StaticLayout.getDesiredWidth(danmaku.text, textPaint)), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
                        danmaku.paintWidth = staticLayout.getWidth();
                        danmaku.paintHeight = staticLayout.getHeight();
                        danmaku.requestFlags &= ~BaseDanmaku.FLAG_REQUEST_REMEASURE;
                    } else {

                        staticLayout = new StaticLayout(text, textPaint, (int) danmaku.paintWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
                    }
                    danmaku.obj = new SoftReference<>(staticLayout);
                } else {
                    return;
                }
            }
            boolean needRestore = false;
            if (left != 0 && top != 0) {
                canvas.save();
                canvas.translate(left, top + paint.ascent());
                needRestore = true;
            }
            staticLayout.draw(canvas);
            if (needRestore) {
                canvas.restore();
            }
        }
    }

    @Override
    public void drawText(BaseDanmaku danmaku, String lineText, Canvas canvas, float left, float top, TextPaint paint, boolean fromWorkerThread) {
        if (danmaku.obj == null) {
            super.drawText(danmaku, lineText, canvas, left, top, paint, fromWorkerThread);
            return;
        }
        SoftReference<StaticLayout> reference = (SoftReference<StaticLayout>) danmaku.obj;
        StaticLayout staticLayout = reference.get();
        boolean requestRemeasure = 0 != (danmaku.requestFlags & BaseDanmaku.FLAG_REQUEST_REMEASURE);
        boolean requestInvalidate = 0 != (danmaku.requestFlags & BaseDanmaku.FLAG_REQUEST_INVALIDATE);

        if (requestInvalidate || staticLayout == null) {
            if (requestInvalidate) {
                danmaku.requestFlags &= ~BaseDanmaku.FLAG_REQUEST_INVALIDATE;
            }
            CharSequence text = danmaku.text;
            if (text != null) {
                if (requestRemeasure) {
                    staticLayout = new StaticLayout(text, paint, (int) Math.ceil(StaticLayout.getDesiredWidth(danmaku.text, paint)), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
                    danmaku.paintWidth = staticLayout.getWidth();
                    danmaku.paintHeight = staticLayout.getHeight();
                    danmaku.requestFlags &= ~BaseDanmaku.FLAG_REQUEST_REMEASURE;
                } else {
                    staticLayout = new StaticLayout(text, paint, (int) danmaku.paintWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
                }
                danmaku.obj = new SoftReference<>(staticLayout);
            } else {
                return;
            }
        }
        boolean needRestore = false;
        if (left != 0 && top != 0) {
            canvas.save();
            canvas.translate(left, top + paint.ascent());
            needRestore = true;
        }
        staticLayout.draw(canvas);
        if (needRestore) {
            canvas.restore();
        }
    }

    @Override
    public void clearCaches() {
        super.clearCaches();
        System.gc();
    }

    @Override
    public void clearCache(BaseDanmaku danmaku) {
        super.clearCache(danmaku);
        if (danmaku.obj instanceof SoftReference<?>) {
            ((SoftReference<?>) danmaku.obj).clear();
        }
    }

    @Override
    protected void drawBackground(BaseDanmaku danmaku, Canvas canvas, float left, float top, Paint paint) {
        Log.d("DFM", "\n\n Background:   W :" + danmaku.paintWidth + "  H: " + danmaku.paintHeight);
        super.drawBackground(danmaku, canvas, left, top, paint);
        if (danmaku.isSelf) {

            Log.d("DFM", "\n\n After:   W :" + danmaku.paintWidth + "  H: " + danmaku.paintHeight);
            int fifteenDP = dp2px(context, 15f);
            int fourteenDP = dp2px(context, 14f);
            int halfDP = dp2px(context, 1f) / 2;
            RectF rectF = new RectF(left, top,
                    left + danmaku.paintWidth ,
                    top + danmaku.paintHeight);
            paint.setColor(Color.parseColor("#24FFFFFF"));
            canvas.drawRoundRect(rectF, fifteenDP, fifteenDP, paint);
            RectF innerRectF = new RectF(left + halfDP, top + halfDP,
                    left + danmaku.paintWidth  - halfDP,
                    top + danmaku.paintHeight - halfDP);
            paint.setColor(Color.parseColor("#66000000"));
            canvas.drawRoundRect(innerRectF, fourteenDP + halfDP, fourteenDP + halfDP, paint);
        }
    }

    @Override
    public void releaseResource(BaseDanmaku danmaku) {
        clearCache(danmaku);
        super.releaseResource(danmaku);
    }

    private Integer dp2px(Context context, Float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return  (int)(dpValue * scale + 0.5f);
    }
}
