package com.wang.wangpicture.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户创建请求
 */
@Data
public class PictureUploadRequest implements Serializable {
    private static final long serialVersionUID = 7296246433949099735L;
    /**
     * 图片id
     */
    private Long id;

    /**
     * 图片地址
     */
    private String fileUrl;
    /**
     * 空间id
     */
    private Long spaceId;

    public Long getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(Long spaceId) {
        this.spaceId = spaceId;
    }

    /**
     * 图片名称
     */
    private String picName;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getPicName() {
        return picName;
    }

    public void setPicName(String picName) {
        this.picName = picName;
    }
}
