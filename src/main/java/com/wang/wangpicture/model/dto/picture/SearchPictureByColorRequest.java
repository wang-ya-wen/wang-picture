package com.wang.wangpicture.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 以图搜图请求
 */
@Data
public class SearchPictureByColorRequest implements Serializable {
    /**
     * 图片主色调
     */
    private String picColor;
    /**
     * 空间id
     *
     */
    private Long spaceId;
    private static final long serialVersionUID = 1L;

    public String getPicColor() {
        return picColor;
    }

    public void setPicColor(String picColor) {
        this.picColor = picColor;
    }

    public Long getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(Long spaceId) {
        this.spaceId = spaceId;
    }
}
