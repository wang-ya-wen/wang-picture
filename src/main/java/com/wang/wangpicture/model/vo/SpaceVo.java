package com.wang.wangpicture.model.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.wang.wangpicture.model.entity.Space;
import org.springframework.beans.BeanUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SpaceVo implements Serializable {
        /**
         * id
         */
        private Long id;

        /**
         * 空间名称
         */
        private String spaceName;

        /**
         * 空间级别:0-普通版 1-专业版 2-旗舰版
         */
        private Integer spaceLevel;
        /**
         * 空间类型
         */
        private Integer spaceType;
        /**
         * 权限列表
         */
        private List<String> permissionList=new ArrayList<>();

    public List<String> getPermissionList() {
        return permissionList;
    }

    public void setPermissionList(List<String> permissionList) {
        this.permissionList = permissionList;
    }

    public Integer getSpaceType() {
        return spaceType;
    }

    public void setSpaceType(Integer spaceType) {
        this.spaceType = spaceType;
    }

    /**
         * 空间图片的最大总大小
         */
        private Long maxSize;

        /**
         * 空间图片的最大数量
         */
        private Long maxCount;

        /**
         * 当前空间下图片的总大小
         */
        private Long totalSize;

        /**
         * 创建用户的id
         */
        private Long userId;

        /**
         * 创建时间
         */
        private Date createTime;

        /**
         * 编辑时间
         */
        private Date editTime;

        /**
         * 编辑时间
         */
        private Date updateTime;
    /**
     * 创建用户信息
     */
    private UserVo user;


        private static final long serialVersionUID = 1L;
    /**
     * 封装类转对象
     */
    public static Space voToObj(SpaceVo spaceVo){
        if(spaceVo==null){
            return null;
        }
        Space space=new Space();
        BeanUtils.copyProperties(spaceVo,space);

        return space;
    }
    /**
     * 对象转封装类
     */
    public static SpaceVo objToVo(Space space){
        if(space==null){
            return null;
        }
        SpaceVo spaceVo=new SpaceVo();
        BeanUtils.copyProperties(space,spaceVo);
        return spaceVo;
    }
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

    public Integer getSpaceLevel() {
        return spaceLevel;
    }

    public void setSpaceLevel(Integer spaceLevel) {
        this.spaceLevel = spaceLevel;
    }

    public Long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Long maxSize) {
        this.maxSize = maxSize;
    }

    public Long getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(Long maxCount) {
        this.maxCount = maxCount;
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
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
}
