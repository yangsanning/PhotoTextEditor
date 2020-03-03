package ysn.com.editor.imagetexteditor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;

import ysn.com.editor.imagetexteditor.listener.OnCloseImageSpanClickListener;

/**
 * @Author yangsanning
 * @ClassName CloseImageSpan
 * @Description 自带关闭按钮的ImageSpan
 * @Date 2020/2/25
 * @History 2020/2/25 author: description:
 */
public class CloseImageSpan extends ImageSpan {

    /**
     * marginTop:   关闭图标的上边距
     * marginRight: 关闭图标的右边距
     */
    private float closeIconMarginTop;
    private float closeIconMarginRight;

    private boolean isInit;
    private boolean isSelect;

    private Bitmap closeBitmap;
    private Rect closeRect;
    private OnCloseImageSpanClickListener onCloseImageSpanClickListener;

    public CloseImageSpan(Drawable drawable, Bitmap closeBitmap, float closeIconMarginTop, float closeIconMarginRight) {
        super(drawable);
        this.closeBitmap = closeBitmap;
        this.closeIconMarginTop = closeIconMarginTop;
        this.closeIconMarginRight = closeIconMarginRight;
    }

    public CloseImageSpan(Drawable drawable, Bitmap closeBitmap, float closeIconMarginTop, float closeIconMarginRight,
                          OnCloseImageSpanClickListener onCloseImageSpanClickListener) {
        this(drawable, closeBitmap, closeIconMarginTop, closeIconMarginRight);
        this.onCloseImageSpanClickListener = onCloseImageSpanClickListener;
    }

    /**
     * 绘制关闭按钮
     *
     * @param x 基线的横坐标（以 TextView 左上角为坐标原点）
     * @param y 基线的纵坐标（以 TextView 左上角为坐标原点）
     */
    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        super.draw(canvas, text, start, end, x, top, y, bottom, paint);
        if (isInit && isSelect) {
            Rect drawableRect = getDrawable().getBounds();
            float closeBitmapLeft = x + drawableRect.right - closeBitmap.getWidth() - closeIconMarginRight;
            float closeBitmapTop = y - drawableRect.bottom + closeIconMarginTop;

            closeRect = new Rect((int) closeBitmapLeft, (int) closeBitmapTop,
                    ((int) closeBitmapLeft + closeBitmap.getWidth()), ((int) closeBitmapTop + closeBitmap.getHeight()));

            canvas.drawBitmap(closeBitmap, closeBitmapLeft, closeBitmapTop, paint);
        } else {
            closeRect = null;
        }
        isInit = true;
        Log.d("test", "draw");
    }

    /**
     * 提供给{@link ClickableMovementMethod}使用的点击事件
     * 这里进行不同点击事件的回调处理
     */
    public void onClick(View view, int x, int y, CloseImageSpan imageSpan, boolean isDown) {
        if (onCloseImageSpanClickListener == null) {
            return;
        }

        if (closeRect != null && closeRect.contains(x, y)) {
            onCloseImageSpanClickListener.onClose(this);
        } else {
            if (isDown) {
                onCloseImageSpanClickListener.onImageDown(imageSpan);
            } else {
                onCloseImageSpanClickListener.onImageUp(imageSpan);
            }
        }
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    /**
     * 设置点击事件回调
     *
     * @param onCloseImageSpanClickListener 击事件回调
     */
    public void setOnCloseImageSpanClickListener(OnCloseImageSpanClickListener onCloseImageSpanClickListener) {
        this.onCloseImageSpanClickListener = onCloseImageSpanClickListener;
    }
}
