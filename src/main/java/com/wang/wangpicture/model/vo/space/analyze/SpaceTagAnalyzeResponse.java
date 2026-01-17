package com.wang.wangpicture.model.vo.space.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 空间分类分析响应
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SpaceTagAnalyzeResponse implements Serializable {
    /**
     * 标签名称
     */
    private String tag;
    /**
     * 使用次数
     */
    private Long count;
    private static final long serialVersionUID = 1L;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
