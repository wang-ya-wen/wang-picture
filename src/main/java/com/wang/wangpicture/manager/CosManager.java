package com.wang.wangpicture.manager;

import cn.hutool.core.io.FileUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.wang.wangpicture.config.CosClientConfig;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 下载对象
     *
     * @param key 唯一键
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 上传对象（附带图片信息）
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        // 1.对图片进行处理（获取基本信息也被视作为一种图片的处理）
        PicOperations picOperations = new PicOperations();
       //1表示返回原图信息
        picOperations.setIsPicInfo(1);
        List<PicOperations.Rule> ruleList = new ArrayList<>();
        //图片压缩
        String webpKey=FileUtil.mainName(key)+".webp";
        PicOperations.Rule compressRule=new PicOperations.Rule();
        compressRule.setFileId(webpKey);
        compressRule.setBucket(cosClientConfig.getBucket());
        compressRule.setRule("imageMogr2/format/webp");
        ruleList.add(compressRule);
        //缩略图处理,仅对>20kB的进行缩略
        if(file.length()>2*1024){
            PicOperations.Rule thumbRule=new PicOperations.Rule();
            //拼接缩略图的路径
            String thumbKey=FileUtil.mainName(key)+"_thumb."+FileUtil.getSuffix(key);
            thumbRule.setFileId(thumbKey);
            thumbRule.setBucket(cosClientConfig.getBucket());
            //缩放规则 <Width>x<Height>>(如果大于原图宽高则不处理）
            thumbRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>",256,256));
            ruleList.add(thumbRule);
        }

        //构造处理参数
        picOperations.setRules(ruleList);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 删除对象
     *
     * @param key 唯一键
     */
    public void deleteObject(String key) {
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }
}