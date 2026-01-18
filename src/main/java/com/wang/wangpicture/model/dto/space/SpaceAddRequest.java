package com.wang.wangpicture.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间创建请求
 */
@Data
public class SpaceAddRequest implements Serializable {
    private static final long serialVersionUID = 7296246433949099735L;
    /**
     * 空间名
     */
    private String spaceName;
    /**
     * 空间级别
     */
    private String spaceLevel;
    /**
     * 空间类型
     */
    private Integer spaceType;

    public Integer getSpaceType() {
        return spaceType;
    }

    public void setSpaceType(Integer spaceType) {
        this.spaceType = spaceType;
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
}
