package com.wang.wangpicture.model.dto.picture;

import com.wang.wangpicture.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 图片编辑请求
 */
@Data
public class PictureQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = 7296246433949099735L;
    /**
     * id
     */
    private Long id;
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
    private List<String> tags;

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
     * 搜索词(同时搜名称、简介等)
     */
    private String searchText;
    /**
     * 创建用户id
     */
    private Long userId;
    /**
     * 空间id
     */
    private Long spaceId;
    /**
     * 是否只查询spaceId为null的数据
     */
    private boolean nullSpaceId;
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
    /**
     * 开始编辑时间
     */
    private Date startEditTime;
    /**
     * 结束编辑时间
     */
    private Date endEditTime;

    public Date getStartEditTime() {
        return startEditTime;
    }

    public void setStartEditTime(Date startEditTime) {
        this.startEditTime = startEditTime;
    }

    public Date getEndEditTime() {
        return endEditTime;
    }

    public void setEndEditTime(Date endEditTime) {
        this.endEditTime = endEditTime;
    }

    public boolean isNullSpaceId() {
        return nullSpaceId;
    }

    public void setNullSpaceId(boolean nullSpaceId) {
        this.nullSpaceId = nullSpaceId;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
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

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(Long spaceId) {
        this.spaceId = spaceId;
    }
}
