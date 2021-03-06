package ysn.com.editor.phototexteditor.span;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.style.ImageSpan;
import android.view.View;

import java.util.List;

import ysn.com.editor.phototexteditor.utils.ImageUtils;
import ysn.com.editor.phototexteditor.utils.PaintUtils;

/**
 * @Author yangsanning
 * @ClassName NotesSpan
 * @Description 注释 Span, 结合{@link PhotoSpan} 使用, 如需单独使用请参考并自定义
 * @Date 2020/3/12
 * @History 2020/3/12 author: description:
 */
public class NotesSpan extends ImageSpan implements IEditorSpan {

    /**
     * width 宽
     * textColor 文字颜色
     * textSize 文字大小
     * marginTop 上边距
     * marginBottom 下边距
     * height 高
     * lineHeight 行高
     */
    private String notes;
    private int width;
    private int marginTop;
    private int marginBottom;
    private int height;
    private int lineHeight;

    private TextPaint textPaint;
    private Drawable drawable;
    private List<String> lineTextList;

    private String showText = "[注释]";

    private OnNotesSpanClickListener onNotesSpanClickListener;

    /**
     * @param notes        注释
     * @param width        宽
     * @param textColor    文字颜色
     * @param textSize     文字大小
     * @param marginTop    上边距
     * @param marginBottom 下边距
     */
    public NotesSpan(String notes, int width, int textColor, float textSize, int marginTop, int marginBottom) {
        super((Drawable) null);
        this.notes = notes;
        this.width = width;
        this.marginTop = marginTop;
        this.marginBottom = marginBottom;

        // 初始化画笔
        initTextPaint(textColor, textSize);

        // 按行分割文字
        lineTextList = PaintUtils.cutTextByLine(textPaint, notes, width);
        lineHeight = PaintUtils.getFontHeight(textPaint);
        height = lineHeight * lineTextList.size() + marginTop + marginBottom;
    }

    /**
     * 初始化画笔
     */
    private void initTextPaint(int textColor, float textSize) {
        textPaint = new TextPaint();
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    /**
     * @param text   完整文本
     * @param start  setSpan 里设置的 start
     * @param end    setSpan 里设置的 end
     * @param x
     * @param top    当前 span 所在行的上方 y
     * @param y      metric 里 baseline 的位置
     * @param bottom 当前 span 所在行的下方y(包含了行间距)，会和下一行的top重合
     * @param paint  使用此 span 的画笔
     */
    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        super.draw(canvas, text, start, end, x, top, y, bottom, paint);

        Drawable drawable = getDrawable();
        Rect drawableRect = drawable.getBounds();

        if (lineTextList.size() == 0) {
            return;
        }

        int newX = (int) (x + drawableRect.right / 2);
        int newY;
        for (int i = 0, length = lineTextList.size(); i < length; i++) {
            newY = y + lineHeight * (i + 1) - height + marginTop;
            canvas.drawText(lineTextList.get(i), newX, newY, textPaint);
        }
    }

    @Override
    public Drawable getDrawable() {
        if (drawable == null) {
            Bitmap bitmap = ImageUtils.zoom(ImageUtils.drawableToBitmap(new ColorDrawable(Color.WHITE), width, height), width);
            drawable = new BitmapDrawable(bitmap);
            drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
        }
        return drawable;
    }

    @Override
    public String getStartTag() {
        return "<notes>";
    }

    @Override
    public String getEndTag() {
        return "</notes>";
    }

    @Override
    public String getShowText() {
        return showText;
    }

    @Override
    public int getShowTextLength() {
        return getShowText().length();
    }

    @Override
    public String getResult() {
        return getStartTag() + notes + getEndTag();
    }

    @Override
    public void onClick(View view, int x, int y, IEditorSpan iEditorSpan, boolean isDown) {
        if (onNotesSpanClickListener != null) {
            onNotesSpanClickListener.onNoteSpanClick(view, iEditorSpan);
        }
    }

    public void setOnNotesSpanClickListener(OnNotesSpanClickListener onNotesSpanClickListener) {
        this.onNotesSpanClickListener = onNotesSpanClickListener;
    }

    public interface OnNotesSpanClickListener {

        void onNoteSpanClick(View view, IEditorSpan iEditorSpan);
    }
}
