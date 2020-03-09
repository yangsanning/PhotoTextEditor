package ysn.com.editor.imagetexteditor;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import java.lang.reflect.Method;

import ysn.com.editor.imagetexteditor.component.ClickableMovementMethod;
import ysn.com.editor.imagetexteditor.component.EditTextWithScrollView;
import ysn.com.editor.imagetexteditor.span.EditorImageSpan;
import ysn.com.editor.imagetexteditor.span.IEditorSpan;
import ysn.com.editor.imagetexteditor.utils.DeviceUtils;
import ysn.com.editor.imagetexteditor.utils.ImageUtils;
import ysn.com.editor.imagetexteditor.utils.LogUtils;
import ysn.com.editor.imagetexteditor.utils.SpanUtils;

/**
 * @Author yangsanning
 * @ClassName ImageTextEditor
 * @Description 一句话概括作用
 * @Date 2020/2/26
 * @History 2020/2/26 author: description:
 */
public class ImageTextEditor extends EditTextWithScrollView implements EditorImageSpan.OnCloseImageSpanClickListener {

    private static final String STRING_LINE_FEED = "\n";

    /**
     * closeIconRes:          关闭按钮的资源id
     * closeIconWidth:        关闭按钮的宽
     * closeIconHeight:       关闭按钮的高
     * closeIconMarginTop:    关闭图标的上边距
     * closeIconMarginRight:  关闭图标的右边距
     * imageWidth:            图片宽度
     */
    private int closeIconRes;
    private int closeIconWidth;
    private int closeIconHeight;
    private float closeIconMarginTop;
    private float closeIconMarginRight;
    private int imageTargetWidth = 800;

    private boolean isDelete;
    private int selStart, selEnd;
    private EditorImageSpan lastImageSpan;

    public ImageTextEditor(Context context) {
        this(context, null);
    }

    public ImageTextEditor(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageTextEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ImageTextEditor);

        closeIconRes = typedArray.getResourceId(R.styleable.ImageTextEditor_ite_close_icon, R.drawable.editor_ic_close);
        closeIconWidth = typedArray.getDimensionPixelSize(R.styleable.ImageTextEditor_ite_close_icon_width, 60);
        closeIconHeight = typedArray.getDimensionPixelSize(R.styleable.ImageTextEditor_ite_close_icon_height, 60);
        closeIconMarginTop = typedArray.getDimensionPixelSize(R.styleable.ImageTextEditor_ite_close_icon_margin_top, 30);
        closeIconMarginRight = typedArray.getDimensionPixelSize(R.styleable.ImageTextEditor_ite_close_icon_margin_right, 30);

        typedArray.recycle();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
    }

    @Override
    public void setSelection(int index) {
        super.setSelection(index);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        if (isDelete) {
            isDelete = false;
            setSelection(this.selStart);
            return;
        }
        this.selStart = selStart;
        this.selEnd = selEnd;

        LogUtils.d("onSelectionChanged " + " selStart: " + selStart + " selEnd: " + selEnd);

        Editable text = getText();

        // 处理 IEditorSpan 的焦点事件
        if (delaEditorSpanSelection(text)) {
            return;
        }

        // 处理 EditorImageSpan 选中与非选中状态
        dealEditorImageSpanSelection(text);

        super.onSelectionChanged(selStart, selEnd);
    }

    /**
     * 处理删除事件
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        LogUtils.d("dispatchKeyEvent");
        if (event.getKeyCode() == KeyEvent.KEYCODE_DEL && event.getAction() != KeyEvent.ACTION_UP) {
            Editable text = getText();
            if (!TextUtils.isEmpty(text)) {
                if (selStart == selEnd) {
                    EditorImageSpan[] editorImageSpans = SpanUtils.getEditorImageSpans(text, (selStart - 2), selStart);
                    if (editorImageSpans.length > 0) {
                        selectImageSpan(text, editorImageSpans[0]);
                        hideSoftInput();
                        return true;
                    }

                    IEditorSpan[] editorSpans = SpanUtils.getEditorSpans(text, (selStart - 1), selStart);
                    if (editorSpans.length > 0) {
                        IEditorSpan editorSpan = editorSpans[0];
                        selStart = text.getSpanStart(editorSpan);
                        isDelete = true;
                        SpannableStringBuilder style = new SpannableStringBuilder(getText());
                        SpannableStringBuilder newStyle = new SpannableStringBuilder();
                        newStyle.append(style.subSequence(0, getText().getSpanStart(editorSpan)));
                        newStyle.append(style.subSequence(getSpanEnd(text, editorSpan), getText().length()));
                        setText(newStyle);
                        return true;
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                enableFocusable();
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 点击{@link EditorImageSpan#onClick(View, int, int, EditorImageSpan, boolean)}图片-按下
     */
    @Override
    public void onImageDown(EditorImageSpan imageSpan) {
        setInputVisible(bFalse());
    }

    /**
     * 点击@link UrlImageSpan#onClick(View, int, int, UrlImageSpan, boolean)}图片-抬起
     */
    @Override
    public void onImageUp(EditorImageSpan imageSpan) {
        unSelectImageSpan(getText());
        selectImageSpan(getText(), imageSpan);
        hideSoftInput();
    }

    /**
     * 点击{@link EditorImageSpan#onClick(View, int, int, EditorImageSpan, boolean)}关闭按钮
     */
    @Override
    public void onClose(final EditorImageSpan imageSpan) {
        lastImageSpan = null;
        Editable text = getText();
        int spanEnd = getSpanEnd(text, imageSpan);
        text.removeSpan(imageSpan);
        text.replace(spanEnd - imageSpan.getShowTextLength(), spanEnd, "");
        setText(text);
        setSelection(Math.min(spanEnd, text.length()));
    }

    /****************************************************  私有方法  **********************************************/

    /**
     * 处理 IEditorSpan 的焦点事件
     *
     * @return true 为时间已处理, false 时间未处理
     */
    private boolean delaEditorSpanSelection(Editable text) {
        IEditorSpan[] editorSpans = SpanUtils.getEditorSpans(text);
        for (IEditorSpan editorSpan : editorSpans) {
            int spanStart = getSpanStart(text, editorSpan);
            int spanEnd = getSpanEnd(text, editorSpan);

            if (selStart == selEnd) {
                if (selStart > spanStart && selStart < spanEnd) {
                    // 如果点击偏前面则移动焦点移动到前面, 反之则移动到后面
                    setSelection(((selStart - spanStart) > (spanEnd - selStart)) ? spanEnd : spanStart);
                    return true;
                }
            } else if (selEnd > spanStart && selEnd < spanEnd) {
                if (selStart < spanStart) {
                    setSelection(selStart, spanStart);
                } else {
                    setSelection(spanStart, spanEnd);
                }
                return true;
            } else if (selStart > spanStart && selStart < spanEnd) {
                setSelection(spanStart, Math.max(selEnd, spanEnd));
                return true;
            }
        }
        return false;
    }

    /**
     * 处理{@link EditorImageSpan} 选中与非选中状态
     */
    private void dealEditorImageSpanSelection(Editable text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        EditorImageSpan[] editorImageSpans = SpanUtils.getEditorImageSpans(text, (selStart - 1), selStart);
        if (editorImageSpans.length > 0) {
            selectImageSpan(text, editorImageSpans[0]);
        } else {
            unSelectImageSpan(text);
        }
    }

    private void selectImageSpan(Editable text, EditorImageSpan imageSpan) {
        lastImageSpan = imageSpan;
        lastImageSpan.setSelect(bTrue());
        updateLastImageSpan(text);
    }

    private void unSelectImageSpan(Editable text) {
        if (lastImageSpan == null) {
            return;
        }
        lastImageSpan.setSelect(bFalse());
        updateLastImageSpan(text);
        lastImageSpan = null;
    }

    /**
     * 切换imageSpan
     */
    private void updateLastImageSpan(Editable text) {
        int spanStart = text.getSpanStart(lastImageSpan);
        int spanEnd = text.getSpanEnd(lastImageSpan);
        if (spanStart >= 0 || spanEnd >= 0) {
            text.setSpan(lastImageSpan, spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    /**
     * 启用焦点
     */
    private void enableFocusable() {
        setFocusable(bTrue());
        setFocusableInTouchMode(bTrue());
        setCursorVisible(bTrue());
        setInputVisible(bTrue());
    }

    /**
     * 显示光标时是否弹起键盘
     */
    private void setInputVisible(Boolean visible) {
        Class<EditText> cls = EditText.class;
        Method method;
        try {
            method = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
            method.setAccessible(true);
            method.invoke(this, visible);
        } catch (Exception e) {
            //TODO: handle exception
        }
    }

    /**
     * 隐藏键盘并禁用焦点
     */
    private void hideSoftInput() {
        setFocusable(bFalse());
        setFocusableInTouchMode(bFalse());
        setCursorVisible(bFalse());
        DeviceUtils.hideSoftInput(getContext(), this);
    }

    private SpannableStringBuilder getStyle() {
        return new SpannableStringBuilder(getText());
    }

    private int getSpanStart(Editable text, IEditorSpan span) {
        return text.getSpanStart(span);
    }

    private int getSpanEnd(Editable text, IEditorSpan span) {
        return text.getSpanEnd(span);
    }

    private boolean bFalse() {
        return Boolean.FALSE;
    }

    private boolean bTrue() {
        return Boolean.TRUE;
    }

    /**
     * 根据图片路径获取 ImageSpan
     */
    private EditorImageSpan getUrlImageSpan(String imagePath) {
        Bitmap bitmap = ImageUtils.getBitmap(imagePath);
        bitmap = ImageUtils.zoom(bitmap, getImageTargetWidth());
        Drawable drawable = new BitmapDrawable(bitmap);
        drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
        EditorImageSpan urlImageSpan = new EditorImageSpan(drawable, imagePath);
        Bitmap closeBitmap = ImageUtils.drawableToBitmap(getResources().getDrawable(closeIconRes), closeIconWidth, closeIconHeight);
        urlImageSpan.bindCloseBitmap(closeBitmap, closeIconMarginTop, closeIconMarginRight);
        urlImageSpan.setOnCloseImageSpanClickListener(this);
        return urlImageSpan;
    }

    /****************************************************  公开方法  **********************************************/

    public void setImageTargetWidth(int imageTargetWidth) {
        this.imageTargetWidth = imageTargetWidth;
    }

    public int getImageTargetWidth() {
        return imageTargetWidth == 0 ? getWidth() : imageTargetWidth;
    }

    /**
     * 添加图片
     *
     * @param imagePath 图片路径
     */
    public void addImage(String imagePath) {
        if (!hasFocus()) {
            return;
        }
        EditorImageSpan urlImageSpan = getUrlImageSpan(imagePath);
        SpannableStringBuilder style = getStyle();
        boolean isNeedLineFeed = selStart - 1 > 0 && !String.valueOf(style.charAt(selStart - 1)).equals(STRING_LINE_FEED);
        style.insert(selStart, isNeedLineFeed ? STRING_LINE_FEED : "");
        int start = selStart + (isNeedLineFeed ? STRING_LINE_FEED.length() : 0);
        int end = start + urlImageSpan.getShowTextLength();
        style.insert(start, urlImageSpan.getShowText());
        style.setSpan(urlImageSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        style.insert(end, STRING_LINE_FEED);
        setText(style);
        setSelection(end + 1);

        setMovementMethod(ClickableMovementMethod.get());
    }

    /**
     * 添加自定义 Span
     *
     * @param iEditorSpan 实现了{@link IEditorSpan} 的Span
     */
    public void addEditorSpan(IEditorSpan iEditorSpan) {
        if (!hasFocus()) {
            return;
        }
        int selEnd = selStart + iEditorSpan.getShowTextLength();
        SpannableStringBuilder style = getStyle();
        String showText = iEditorSpan.getShowText();
        style.insert(selStart, showText);
        style.setSpan(iEditorSpan, selStart, selEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        setText(style);
        setSelection(selEnd);
    }

    public String getEditTexts() {
        return SpanUtils.getEditTexts(getText());
    }
}