package com.wang.wangpicture.model.vo;


import cn.hutool.json.JSONUtil;
import com.wang.wangpicture.model.entity.Picture;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class PictureVo implements Serializable {
    private static final long serialVersionUID = -6492306386516185838L;
    /**
     * id
     */
    private Long id;
    /**
     * 图片url
     */
    private String url;
    /**
     * 缩略图 url
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
     * 图片比例
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
     * 创建用户信息
     */
    private UserVo user;
    /**
     * 封装类转对象
     */
    public static Picture voToObj(PictureVo pictureVo){
        if(pictureVo==null){
            return null;
        }
        Picture picture=new Picture();
        BeanUtils.copyProperties(pictureVo,picture);
        //类型不同，需要转换
        picture.setTags(JSONUtil.toJsonStr(pictureVo.getTags()));
        return picture;
    }
    /**
     * 对象转封装类
     */
    public static PictureVo objToVo(Picture picture){
        if(picture==null){
            return null;
        }
        PictureVo pictureVo=new PictureVo();
        BeanUtils.copyProperties(picture,pictureVo);
        //类型不同，需要转换
        pictureVo.setTags(JSONUtil.toList(picture.getTags(),String.class));
        return pictureVo;
    }

    public String getPicColor() {
        return picColor;
    }

    public void setPicColor(String picColor) {
        this.picColor = picColor;
    }

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

    public UserVo getUser() {
        return user;
    }

    public void setUser(UserVo user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "PictureVo{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", name='" + name + '\'' +
                ", introduction='" + introduction + '\'' +
                ", tags=" + tags +
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
                ", user=" + user +
                '}';
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
