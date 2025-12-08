package com.wang.wangpicture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wang.wangpicture.model.dto.user.UserQueryRequest;
import com.wang.wangpicture.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wang.wangpicture.model.vo.UserLoginVo;
import com.wang.wangpicture.model.vo.UserVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 *
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     * @param userAccount 账号
     * @param userPassword  密码
     * @param checkPassword  确认密码
     * @return
     */
    long userRegister(String userAccount,String userPassword,String checkPassword);

    /**
     * 密码加密
     * @param userPassword 用户密码
     * @return
     */
    String encrptyPassword(String userPassword);

    /**
     * 用户登录
     * @param userAccount
     * @param userPassword
     * @return  脱敏后的用户信息
     */
    UserLoginVo userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获得脱敏后的登录用户信息
     * @param user
     * @return
     */
    UserLoginVo getLoginUserVO(User user);
    /**
     * 获得脱敏后的用户信息
     * @param user
     * @return
     */
    UserVo getUserVO(User user);
    /**
     * 获得脱敏后的用户信息列表
     * @param userList
     * @return
     */
    List<UserVo> getUserVoList(List<User> userList);
    /**
     * 用户注销
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取查询条件
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 是否为管理员
     * @param user
     * @return
     */
    boolean isAdmin(User user);
}
