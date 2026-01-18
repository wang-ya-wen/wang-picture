package com.wang.wangpicture.model.dto.space;

import com.wang.wangpicture.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = 7296246433949099735L;
    /**
     * 空间id
     */
    private Long id;
    /**
     * 用户名
     */
    private String userId;
    /**
     * 空间名称
     */
    private String spaceName;
    /**
     * 空间级别
     */
    private Integer spaceLevel;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSpaceName() {
        return spaceName;
    }

    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
    }

    public Integer getSpaceLevel() {
        return spaceLevel;
    }

    public void setSpaceLevel(Integer spaceLevel) {
        this.spaceLevel = spaceLevel;
    }
}
