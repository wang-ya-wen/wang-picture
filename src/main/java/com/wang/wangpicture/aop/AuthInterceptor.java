package com.wang.wangpicture.aop;

import com.wang.wangpicture.annotation.AuthCheck;
import com.wang.wangpicture.exception.BusinessException;
import com.wang.wangpicture.exception.ErrorCode;
import com.wang.wangpicture.model.entity.User;
import com.wang.wangpicture.model.enums.UserRoleEnum;
import com.wang.wangpicture.model.vo.UserLoginVo;
import com.wang.wangpicture.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行拦截
     * @return
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes=RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request=((ServletRequestAttributes)requestAttributes).getRequest();
        //获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum=UserRoleEnum.getEnumByValue(mustRole);
        //如果不需要权限，放行
        if(mustRoleEnum==null){
            return joinPoint.proceed();
        }
        //必须有权限才会通过以下的代码
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        if(userRoleEnum==null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //要求必须有管理员权限，但用户没有管理员权限
        if(UserRoleEnum.ADMIN.equals(mustRoleEnum) &&!UserRoleEnum.ADMIN.equals(userRoleEnum)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //通过权限校验,放行
        return joinPoint.proceed();
    }
}
