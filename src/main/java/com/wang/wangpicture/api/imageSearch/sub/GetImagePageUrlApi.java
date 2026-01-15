package com.wang.wangpicture.api.imageSearch.sub;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.wang.wangpicture.exception.BusinessException;
import com.wang.wangpicture.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GetImagePageUrlApi {
    /**
     * 获取以图搜图页面地址
     * @param imageUrl
     * @return
     */
    public static String getImagePageUrl(String imageUrl){
        //1.准备请求参数
        Map<String,Object> formData = new HashMap<>();
        formData.put("image_url",imageUrl);
        formData.put("tn","pc");
        formData.put("from","pc");
        formData.put("image_source","PC_UPLOAD_URL");
        //获取当前时间段
        String uptime=String.valueOf(System.currentTimeMillis());
        //请求地址
        String url="https://graph.baidu.com/upload?uptime="+uptime;
        //2.发送请求
        try{
            HttpResponse httpResponse = HttpRequest.post(url)
                    .form(formData)
                    .timeout(5000)
                    .execute();
            if(httpResponse.getStatus()!= HttpStatus.HTTP_OK){
                throw  new BusinessException(ErrorCode.OPERATION_ERROR,"接口调用失败");
            }
            //解析响应
            String body=httpResponse.body();
            Map<String,Object> result = JSONUtil.toBean(body, Map.class);
            //3.处理响应结果
            if(result==null || !Integer.valueOf(0).equals(result.get("status"))){
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"接口调用失败");
            }

            Map<String,Object> data=(Map<String,Object>)result.get("data");
            String rawUrl=(String) data.get("url");
            String searchResultUrl = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
            //如果URL为空
            if(StrUtil.isBlank(searchResultUrl)){
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"搜索失败");
            }
            return searchResultUrl;
        }
        catch (Exception e){
//            log.error("图片搜索失败",e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"搜索失败");
        }

    }
}
