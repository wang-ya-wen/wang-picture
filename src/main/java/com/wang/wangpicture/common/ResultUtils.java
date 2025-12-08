package com.wang.wangpicture.common;

import com.wang.wangpicture.exception.ErrorCode;

public class ResultUtils {
    /**
     * 成功
     * @Param data 数据
     * @Param <T> 数据类型
     * @return  响应
     */
    public static <T> BaseResponse<T> success(T data){

        return new BaseResponse<>(0,data,"ok");
    }

    /**
     * 失败
     * @param errorCode 错误码
     * @return
     */
    public static  BaseResponse<?> error(ErrorCode errorCode){

        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败
     * @param code 错误码
     * @param message 错误信息
     * @return
     */
    public static  BaseResponse<?> error(int code,String message){

        return new BaseResponse<>(code,null,message);
    }

    /**
     * 失败
     * @param errorCode 错误码
     * @param message
     * @return
     */
    public static  BaseResponse<?> error(ErrorCode errorCode,String message){
        return new BaseResponse<>(errorCode.getCode(),null,message);
    }
}
