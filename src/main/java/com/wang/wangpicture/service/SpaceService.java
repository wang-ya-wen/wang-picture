
package com.wang.wangpicture.service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.wangpicture.model.dto.space.SpaceAddRequest;
import com.wang.wangpicture.model.dto.space.SpaceQueryRequest;
import com.wang.wangpicture.model.entity.Space;
import com.wang.wangpicture.model.entity.User;
import com.wang.wangpicture.model.vo.SpaceVo;
import com.baomidou.mybatisplus.extension.service.IService;


import javax.servlet.http.HttpServletRequest;

/**
 *
 */
public interface SpaceService extends IService<Space> {
    /**
     * 创建空间
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);
    /**
     * 获取查询对象
     * @param spaceQueryRequest
     * @return
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 获取图片包装类
     * @param space
     * @param request
     * @return
     */
    SpaceVo getSpaceVo(Space space, HttpServletRequest request);

    /**
     * 分页获取图片封装类
     * @param spacePage
     * @param request
     * @return
     */
    Page<SpaceVo> getSpaceVoPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 数据校验
     * @param space
     */
    void validSpace(Space space,boolean add);

    /**
     * 根据空间级别填充空间对象
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 校验用户是否有某个空间的权限
     *
     * @param loginUser
     * @param space
     */
    void checkSpaceAuth(User loginUser, Space space);
}
