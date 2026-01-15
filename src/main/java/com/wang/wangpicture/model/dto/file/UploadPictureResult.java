package com.wang.wangpicture.model.dto.file;

import lombok.Data;

@Data
public class UploadPictureResult {
    /**
     * 图片地址
     */
    private String url;
    /**
     * 缩略图地址
     */
    private String thumbnailUrl;

    /**
     * 图片名称
     */
    private String picName;
    /**
     * 文件体积
     */
    private Long picSize;
    /**
     * 图片宽度
     */
    private int picWidth;
    /**
     * 图片高度
     */
    private int picHeight;
    /**
     * 图片的宽高比例
     */
    private Double picScale;
    /**
     * 图片格式
     */
    private String picFormat;
    /**
     * 图片主色调
     */
    private String picColor;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPicName() {
        return picName;
    }

    public void setPicName(String picName) {
        this.picName = picName;
    }

    public Long getPicSize() {
        return picSize;
    }

    public void setPicSize(Long picSize) {
        this.picSize = picSize;
    }

    public int getPicWidth() {
        return picWidth;
    }

    public void setPicWidth(int picWidth) {
        this.picWidth = picWidth;
    }

    public int getPicHeight() {
        return picHeight;
    }

    public void setPicHeight(int picHeight) {
        this.picHeight = picHeight;
    }

    public Double getPicScale() {
        return picScale;
    }

    public void setPicScale(Double picScale) {
        this.picScale = picScale;
    }

    public String getPicFormat() {
        return picFormat;
    }

    public void setPicFormat(String picFormat) {
        this.picFormat = picFormat;
    }

    public String getPicColor() {
        return picColor;
    }

    public void setPicColor(String picColor) {
        this.picColor = picColor;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    @Override
    public String toString() {
        return "UploadPictureResult{" +
                "url='" + url + '\'' +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", picName='" + picName + '\'' +
                ", picSize=" + picSize +
                ", picWidth=" + picWidth +
                ", picHeight=" + picHeight +
                ", picScale=" + picScale +
                ", picFormat='" + picFormat + '\'' +
                ", picColor='" + picColor + '\'' +
                '}';
    }
}
