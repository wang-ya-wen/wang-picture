package com.wang.wangpicture.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wang.wangpicture.model.dto.space.SpaceAddRequest;
import com.wang.wangpicture.model.dto.space.SpaceQueryRequest;
import com.wang.wangpicture.model.dto.spaceUser.SpaceUserAddRequest;
import com.wang.wangpicture.model.dto.spaceUser.SpaceUserQueryRequest;
import com.wang.wangpicture.model.entity.Space;
import com.wang.wangpicture.model.entity.SpaceUser;
import com.wang.wangpicture.model.entity.User;
import com.wang.wangpicture.model.vo.SpaceUserVo;
import com.wang.wangpicture.model.vo.SpaceVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 *
 */
public interface SpaceUserService extends IService<SpaceUser> {
    /**
     * 创建空间成员
     * @param spaceUserAddRequest
     * @return
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 校验空间成员
     *
     * @param spaceUser
     * @param add       是否为创建时检验
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);

    /**
     * 获取空间成员包装类（单条）
     *
     * @param spaceUser
     * @param request
     * @return
     */
    SpaceUserVo getSpaceUserVo(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 获取空间成员包装类（列表）
     *
     * @param spaceUserList
     * @return
     */
    List<SpaceUserVo> getSpaceUserVoList(List<SpaceUser> spaceUserList);

    /**
     * 获取查询对象
     *
     * @param spaceUserQueryRequest
     * @return
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);
}
