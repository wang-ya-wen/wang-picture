package com.wang.wangpicture.model.dto.space;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 图片编辑请求
 */
@Data
public class SpaceEditRequest implements Serializable {
    private static final long serialVersionUID = 7296246433949099735L;
    /**
     * 空间id
     */
    private Long id;
    /**
     * 空间名称

     */
    private String spaceName;

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
}
