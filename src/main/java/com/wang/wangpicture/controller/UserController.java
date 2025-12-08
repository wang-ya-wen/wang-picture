package com.wang.wangpicture.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.wangpicture.annotation.AuthCheck;
import com.wang.wangpicture.common.BaseResponse;
import com.wang.wangpicture.common.DeleteRequest;
import com.wang.wangpicture.common.ResultUtils;
import com.wang.wangpicture.constant.UserConstant;
import com.wang.wangpicture.exception.BusinessException;
import com.wang.wangpicture.exception.ErrorCode;
import com.wang.wangpicture.exception.ThrowUtils;
import com.wang.wangpicture.model.dto.user.*;
import com.wang.wangpicture.model.entity.User;
import com.wang.wangpicture.model.enums.UserRoleEnum;
import com.wang.wangpicture.model.vo.UserLoginVo;
import com.wang.wangpicture.model.vo.UserVo;
import com.wang.wangpicture.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.scripting.bsh.BshScriptUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    public UserService userService;
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse <Long> Register(@RequestBody UserRegisterRequest userRegisterRequest){
        ThrowUtils.throwIf(userRegisterRequest==null, ErrorCode.SYSTEM_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long userRegister = userService.userRegister(userAccount, userPassword, checkPassword);

        return ResultUtils.success(userRegister);
    }
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public BaseResponse <UserLoginVo> Login(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        ThrowUtils.throwIf(userLoginRequest==null, ErrorCode.SYSTEM_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        UserLoginVo userLoginVo = userService.userLogin(userAccount, userPassword,request);

        return ResultUtils.success(userLoginVo);
    }
    /**
     * 获取当前登录用户
     */
    @GetMapping("/get/login")
    public BaseResponse<UserLoginVo> getLoginUser(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }
    /**
     * 注销
     */
    @GetMapping("/logout")

    public BaseResponse<Boolean> userLogout(HttpServletRequest request){
        ThrowUtils.throwIf(request==null,ErrorCode.PARAMS_ERROR);
        boolean userLogout = userService.userLogout(request);
        return ResultUtils.success(userLogout);
    }
    /**
     * 创建用户
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse <Long> addUser(@RequestBody UserAddRequest userAddRequest){
        ThrowUtils.throwIf(userAddRequest==null, ErrorCode.SYSTEM_ERROR);
        User user=new User();
        BeanUtil.copyProperties(userAddRequest,user);
        //默认密码
        final String DEFAULT_PASSWORD="12345678";
        String encryptPassword=userService.encrptyPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        //插入数据库
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }
    /**
     * 根据id获取用户(仅管理员)
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id){
        //如果id小于等于0则抛出参数错误
        ThrowUtils.throwIf(id<=0,ErrorCode.PARAMS_ERROR);
        User user=new User();
        ThrowUtils.throwIf(user==null,ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }
    /**
     * 根据id获取包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVo> getUserVo(long id){
        BaseResponse<User> response=getUserById(id);
        User user=response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }
    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest){
        if(deleteRequest==null || deleteRequest.getId()<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }
    /**
     * 更新用户
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest){
        if(userUpdateRequest==null || userUpdateRequest.getId()<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user=new User();
        BeanUtils.copyProperties(userUpdateRequest,user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
    /**
     * 分页查询 分页获取用户封装列表   请求过于复杂使用postMapping
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVo>> listUserVoByPage(@RequestBody UserQueryRequest userQueryRequest){
        ThrowUtils.throwIf(userQueryRequest==null,ErrorCode.PARAMS_ERROR);
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize), userService.getQueryWrapper(userQueryRequest));
        Page<UserVo> userVoPage=new Page<>(current,pageSize,userPage.getTotal());
        List<UserVo> userVoList = userService.getUserVoList(userPage.getRecords());
        userVoPage.setRecords(userVoList);
        return ResultUtils.success(userVoPage);
    }
}
