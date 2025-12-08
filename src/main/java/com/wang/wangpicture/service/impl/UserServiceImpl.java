package com.wang.wangpicture.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wang.wangpicture.constant.UserConstant;
import com.wang.wangpicture.exception.BusinessException;
import com.wang.wangpicture.exception.ErrorCode;
import com.wang.wangpicture.model.dto.user.UserQueryRequest;
import com.wang.wangpicture.model.entity.User;
import com.wang.wangpicture.model.enums.UserRoleEnum;
import com.wang.wangpicture.model.vo.UserLoginVo;
import com.wang.wangpicture.model.vo.UserVo;
import com.wang.wangpicture.service.UserService;
import com.wang.wangpicture.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.omg.CORBA.UserException;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{
    /**
     * 用户注册
     * @param userAccount  用户账户
     * @param userPassword  用户密码
     * @param checkPassword  确认密码
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {

        //1.校验参数
        if(userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号过短");
        }
        if(userPassword.length()<8||checkPassword.length()<8){
            throw new  BusinessException(ErrorCode.PARAMS_ERROR,"输入的密码过短");
        }
        if(!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次输入的密码不一致");
        }
        //2.检查用户账号是否和数据库中已有的重复
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("userAccount",userAccount);
        long count=this.baseMapper.selectCount(queryWrapper);
        if(count>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户重复");
        }
        //3.密码一定要加密
        String result = encrptyPassword(userPassword);
        //4.插入数据到数据库中
        User user=new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(result);
        user.setUserRole(UserRoleEnum.USER.getValue());
        user.setUserName("无名");
        boolean saveResult = this.save(user);
        if(!saveResult){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"注册失败，数据库错误");
        }
        return user.getId();
    }
    /**
     * 密码加密
     */
    @Override
    public String encrptyPassword(String userPassword){
        final String SALT="wang";
        return DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());

    }

    @Override
    public UserLoginVo userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验
        if(StrUtil.hasBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if(userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号错误");
        }
        if(userPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码过短");
        }
        //2.对用户输入的密码进行加密
        String encrptyPassword = encrptyPassword(userPassword);
        //3.查询数据库 不存在，则抛异常
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword",encrptyPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        if(user==null){
            System.out.println("user login failed,userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在或密码错误");
        }
        //4.保存用户的登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE,user);
        return this.getLoginUserVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser=new User();
        currentUser = (User) userObj;
        if(currentUser==null ||currentUser.getId()==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        //再从数据库中查一次(不追求性能的话)
        Long userId=currentUser.getId();
        currentUser = this.getById(userId);
        if(currentUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public UserLoginVo getLoginUserVO(User user) {
           if(user==null){
               return null;
           }
           UserLoginVo userLoginVo=new UserLoginVo();
        BeanUtil.copyProperties(user,userLoginVo);
        return userLoginVo;
    }

    @Override
    public UserVo getUserVO(User user) {
        if(user==null){
            return null;
        }
        UserVo userVo=new UserVo();
        BeanUtil.copyProperties(user,userVo);
        return userVo;
    }

    /**
     * 获取脱敏后的用户列表
     * @param userList
     * @return
     */
    @Override
    public List<UserVo> getUserVoList(List<User> userList) {
        if(CollUtil.isEmpty(userList)){
            return new ArrayList<>();
        }
        return userList.stream()
                .map(this::getUserVO)
                .collect(Collectors.toList());

    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        //判断是否已经登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if(userObj==null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"未登录");
        }
        //移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);

        return true;
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if(userQueryRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(id),"id",id);
        queryWrapper.like(ObjUtil.isNotEmpty(userAccount),"userAccount",userAccount);
        queryWrapper.like(ObjUtil.isNotEmpty(userName),"userName",userName);
        queryWrapper.like(ObjUtil.isNotEmpty(userProfile),"userProfile",userProfile);
        queryWrapper.eq(ObjUtil.isNotEmpty(userRole),"userRole",userRole);
        queryWrapper.orderBy(ObjUtil.isNotEmpty(sortField),sortOrder.equals("ascend"),sortField);

        return queryWrapper;
    }

    @Override
    public boolean isAdmin(User user) {

        return  user!=null&&!UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }


}




