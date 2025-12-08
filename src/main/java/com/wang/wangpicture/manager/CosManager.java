package com.wang.wangpicture.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.demo.PicOperationDemo;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.wang.wangpicture.config.CosClientConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.annotation.Resource;
import java.io.File;

@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;
    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     * @param key 唯一键
     * @param file
     * @return
     */
    // 将本地文件上传到 COS
    public PutObjectResult putObject(String key, File file){
        PutObjectRequest putObjectRequest=new PutObjectRequest(cosClientConfig.getBucket(),key,file);
        return cosClient.putObject(putObjectRequest);
    }
    /**
     * 下载对象
     */
    public COSObject getObject(String key){
        GetObjectRequest getObjectRequest=new GetObjectRequest(cosClientConfig.getBucket(),key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 上传并解析图片的方法
     */
    // 将本地文件上传到 COS
    public PutObjectResult putPictureObject(String key, File file){
        PutObjectRequest putObjectRequest=new PutObjectRequest(cosClientConfig.getBucket(),key,file);
        //对图片处理
        PicOperations picOperations=new PicOperations();
        //1表示返回原图信息
        picOperations.setIsPicInfo(1);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }
}
