package com.wang.wangpicture.model.dto.spaceUser;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建空间成员请求
 */
@Data
public class SpaceUserAddRequest implements Serializable {

    /**
     * 空间 ID
     */
    private Long spaceId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

    private static final long serialVersionUID = 1L;

    public Long getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(Long spaceId) {
        this.spaceId = spaceId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSpaceRole() {
        return spaceRole;
    }

    public void setSpaceRole(String spaceRole) {
        this.spaceRole = spaceRole;
    }
}