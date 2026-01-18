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
public class SpaceCategoryAnalyzeResponse implements Serializable {

    /**
     * 当前图片数量
     */
    private Long usedCount;
    /**
     * 图片分类
     */
    private String category;
    /**
     * 分类图片总大小
     */
    private Long totalSize;
    private final static long serialVersionUID = 1L;

    public SpaceCategoryAnalyzeResponse(Long usedCount, String category, Long totalSize) {
        this.usedCount = usedCount;
        this.category = category;
        this.totalSize = totalSize;
    }

    public Long getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(Long usedCount) {
        this.usedCount = usedCount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }
}
