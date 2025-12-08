package com.wang.wangpicture.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间更新请求
 */
@Data
public class SpaceUpdateRequest implements Serializable {
    private static final long serialVersionUID = 7296246433949099735L;
    /**
     * 空间id
     */
    private Long id;
    /**
     * 空间名
     */
    private String spaceName;
    /**
     * 空间级别
     */
    private String spaceLevel;
    /**
     * 空间大小
     */
    private Long maxSize;
    /**
     * 空间图片的最大数量
     */
    private Long maxCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSpaceName() {
        return spaceName;
    }

    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
    }

    public String getSpaceLevel() {
        return spaceLevel;
    }

    public void setSpaceLevel(String spaceLevel) {
        this.spaceLevel = spaceLevel;
    }

    public Long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Long maxSize) {
        this.maxSize = maxSize;
    }

    public Long getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(Long maxCount) {
        this.maxCount = maxCount;
    }
}
