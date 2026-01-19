package com.wang.wangpicture.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.wang.wangpicture.common.BaseResponse;
import com.wang.wangpicture.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(NotLoginException.class)
    public BaseResponse<?> notLoginException(NotLoginException e) {
        System.out.println("NotLoginException"+ e);
        return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, e.getMessage());
    }

    @ExceptionHandler(NotPermissionException.class)
    public BaseResponse<?> notPermissionExceptionHandler(NotPermissionException e) {
        System.out.println("NotPermissionException"+ e);
        return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, e.getMessage());
    }
/**
 * 异常处理器，用于处理业务异常
 * @param e 业务异常对象
 * @return 返回一个包含错误码和错误信息的BaseResponse对象
 */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e){
    // 注释掉的日志记录代码，用于记录BusinessException异常
//        log.error("BusinessException",e);
    // 使用System.out输出异常信息，用于调试
        System.out.println("BusinessException"+e);

    // 调用ResultUtils工具类的error方法，返回一个包含错误码和错误信息的响应对象
        return ResultUtils.error(e.getCode(),e.getMessage());
    }
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> businessExceptionHandler(RuntimeException e){
//        log.error("RuntimeException",e);
        System.out.println("RuntimeException"+e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"系统错误");
    }

}
