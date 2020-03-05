package ysn.com.editor.imagetexteditor.span;

/**
 * @Author yangsanning
 * @ClassName IEditSpan
 * @Description 编辑器Span需要继承的接口
 * @Date 2020/3/5
 * @History 2020/3/5 author: description:
 */
public interface IEditorSpan {

    /**
     * .获取标签头
     *
     * @return 标签头
     */
    String getStartTag();

    /**
     * 获取标签尾
     *
     * @return 标签尾
     */
    String getEndTag();

    /**
     * 获取源文本（带标签）
     *
     * @return 源文本（带标签）
     */
    String getText();

    /**
     * 获取源文本长度
     *
     * @return 源文本长度
     */
    int length();
}
