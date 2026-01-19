package com.wang.wangpicture.model.dto.space;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 空间级别
 */
@Data

public class SpaceLevel {
    /**
     * 值
     */
    private int value;
    /**
     * 中文
     */
    private String text;
    /**
     * 最大数量
     */
    private long maxCount;
    /**
     * 最大容量
     */
    private long  maxSize;

    public SpaceLevel(int value, String text, long maxCount, long maxSize) {
        this.value = value;
        this.text = text;
        this.maxCount = maxCount;
        this.maxSize = maxSize;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(long maxCount) {
        this.maxCount = maxCount;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }
}
