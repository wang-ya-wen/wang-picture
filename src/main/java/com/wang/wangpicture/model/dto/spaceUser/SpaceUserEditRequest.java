package com.wang.wangpicture.model.dto.spaceUser;

import lombok.Data;

import java.io.Serializable;

/**
 * 编辑空间成员请求
 */
@Data
public class SpaceUserEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSpaceRole() {
        return spaceRole;
    }

    public void setSpaceRole(String spaceRole) {
        this.spaceRole = spaceRole;
    }
}