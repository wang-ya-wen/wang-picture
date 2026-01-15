package com.wang.wangpicture.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName picture
 */
@TableName(value ="picture")
@Data
public class Picture implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 图片url

     */
    private String url;
    /**
     * 图片缩略图地址

     */
    private String thumbnailUrl;


    /**
     * 图片名称

     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签(JSON数组)
     */
    private String tags;

    /**
     * 图片体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片宽高比例
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

    public String getPicColor() {
        return picColor;
    }

    public void setPicColor(String picColor) {
        this.picColor = picColor;
    }

    /**
     * 创建用户id
     */
    private Long userId;
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
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除

     */
    @TableLogic
    private Byte isDelete;
    /**
     * 审核状态 0-待审核 1-通过;2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 审核人ID
     */
    private Long reviewrId;

    /**
     * 审核时间
     */
    private Date reviewTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Long getPicSize() {
        return picSize;
    }

    public void setPicSize(Long picSize) {
        this.picSize = picSize;
    }

    public Integer getPicWidth() {
        return picWidth;
    }

    public void setPicWidth(Integer picWidth) {
        this.picWidth = picWidth;
    }

    public Integer getPicHeight() {
        return picHeight;
    }

    public void setPicHeight(Integer picHeight) {
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getEditTime() {
        return editTime;
    }

    public void setEditTime(Date editTime) {
        this.editTime = editTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Byte getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Byte isDelete) {
        this.isDelete = isDelete;
    }

    public Integer getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(Integer reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getReviewMessage() {
        return reviewMessage;
    }

    public void setReviewMessage(String reviewMessage) {
        this.reviewMessage = reviewMessage;
    }

    public Long getReviewrId() {
        return reviewrId;
    }

    public void setReviewrId(Long reviewrId) {
        this.reviewrId = reviewrId;
    }

    public Date getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(Date reviewTime) {
        this.reviewTime = reviewTime;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    @Override
    public String toString() {
        return "Picture{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", name='" + name + '\'' +
                ", introduction='" + introduction + '\'' +
                ", category='" + category + '\'' +
                ", tags='" + tags + '\'' +
                ", picSize=" + picSize +
                ", picWidth=" + picWidth +
                ", picHeight=" + picHeight +
                ", picScale=" + picScale +
                ", picFormat='" + picFormat + '\'' +
                ", picColor='" + picColor + '\'' +
                ", userId=" + userId +
                ", spaceId=" + spaceId +
                ", createTime=" + createTime +
                ", editTime=" + editTime +
                ", updateTime=" + updateTime +
                ", isDelete=" + isDelete +
                ", reviewStatus=" + reviewStatus +
                ", reviewMessage='" + reviewMessage + '\'' +
                ", reviewrId=" + reviewrId +
                ", reviewTime=" + reviewTime +
                '}';
    }
}