package com.wang.wangpicture.model.vo.space.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间使用分析响应类
 */
@Data
public class SpaceUsageAnalyzeResponse implements Serializable {
    /**
     * 已使用大小
     */
    private Long usedSize;
    /**
     * 总大小
     */
    private Long maxSize;
    /**
     * 空间使用比例
     */
    private Double sizeUsageRatio;
    /**
     * 当前图片数量
     */
    private Long usedCount;
    /**
     * 最大图片数量
     */
    private Long maxCount;
    /**
     * 图片数量占比
     */
    private Double countUsageRatio;
    private final static long serialVersionUID = 1L;

    public Long getUsedSize() {
        return usedSize;
    }

    public void setUsedSize(Long usedSize) {
        this.usedSize = usedSize;
    }

    public Long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Long maxSize) {
        this.maxSize = maxSize;
    }

    public Double getSizeUsageRatio() {
        return sizeUsageRatio;
    }

    public void setSizeUsageRatio(Double sizeUsageRatio) {
        this.sizeUsageRatio = sizeUsageRatio;
    }

    public Long getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(Long usedCount) {
        this.usedCount = usedCount;
    }

    public Long getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(Long maxCount) {
        this.maxCount = maxCount;
    }

    public Double getCountUsageRatio() {
        return countUsageRatio;
    }

    public void setCountUsageRatio(Double countUsageRatio) {
        this.countUsageRatio = countUsageRatio;
    }
}
