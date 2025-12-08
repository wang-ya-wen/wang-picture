package com.wang.wangpicture.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.wang.wangpicture.config.CosClientConfig;
import com.wang.wangpicture.exception.BusinessException;
import com.wang.wangpicture.exception.ErrorCode;
import com.wang.wangpicture.exception.ThrowUtils;
import com.wang.wangpicture.manager.CosManager;
import com.wang.wangpicture.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 图片上传模板
 */
@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    private CosClientConfig cosClientConfig;
    @Resource
    private COSClient cosClient;
    @Resource
    private CosManager cosManager;
    /**
     * 上传图片
     * @param inputSource 文件
     * @param uploadPathPrefix  上传路径前缀
     * @return
     */
    public UploadPictureResult uploadPicture(Object inputSource,String uploadPathPrefix){
        //1.校验图库
        validPicture(inputSource);
        //2.图片上传地址
        String uuid= RandomUtil.randomString(16);
        String originalFilename=getOriginalFilename(inputSource);
        //自己拼接文件上传路径，而不是使用原始文件名称，可以增强安全性
        String uploadFilename=String.format("%s_%s.%s", DateUtil.formatDate(new Date()),uuid,
                FileUtil.getSuffix(originalFilename));
        String uploadPath=String.format("/%s/%s",uploadPathPrefix,uploadFilename);
        //解析结果并返回
        File file=null;
        try {
            //3.获取临时文件，获取文件到服务器
            file=File.createTempFile(uploadPath,null);
            //处理文件来源
            processFile(inputSource,file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            //4.获取图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            UploadPictureResult uploadPictureResult = buildResult(originalFilename, uploadPath, file, imageInfo);

            //5.返回可访问的地址
            return uploadPictureResult;
        } catch (IOException e) {
            System.out.println("图片上传到对象存储失败"+e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传失败");
        }finally {
            //6.临时文件清理
            deleteTempFile(file);
        }

    }
    /**
     * 处理输入源并生成本地临时文件
     * @param inputSource
     */
    protected abstract void processFile(Object inputSource,File file);

    /**
     * 校验输入源（本地文件或URL）
     * @param inputSource
     */
    protected abstract void validPicture(Object inputSource);

    /**
     * 获取输入源的原始文件名
     * @param inputSource
     * @return
     */
    protected abstract String getOriginalFilename(Object inputSource);
    /**
     * 封装返回结果
     * @param originalFilename
     * @param uploadPath
     * @param file
     * @param imageInfo 对象存储返回的图片信息
     * @return
     */
    private UploadPictureResult buildResult(String originalFilename, String uploadPath, File file, ImageInfo imageInfo) {
        //计算宽高
        int picWidth = imageInfo.getWidth();
        int picHeight= imageInfo.getHeight();
        double picScale= NumberUtil.round(picWidth*1.0/picWidth,2).doubleValue();
        //封装返回结果
        UploadPictureResult uploadPictureResult=new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfig.getHost()+"/"+uploadPath);
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        return uploadPictureResult;
    }


    /**
     * 校验文件/图片
     * @param multipartFile
     */
    private void validPicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile==null, ErrorCode.PARAMS_ERROR,"文件不能为空");
        //1.校验文件大小
        long fileSize = multipartFile.getSize();
        final long ONE_M=1024*1024;
        ThrowUtils.throwIf(fileSize>2*ONE_M,ErrorCode.PARAMS_ERROR,"文件大小不能超过2MB");
        //2.校验文件后缀
        String fileSuffix= FileUtil.getSuffix(multipartFile.getOriginalFilename());
        //定义一个允许上传的文件后缀集合
        final List<String> ALLOW_FORMAT_LIST= Arrays.asList("jpeg","jpg","png","webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix),ErrorCode.PARAMS_ERROR,"文件类型错误");
    }
    /**
     * //临时文件清理
     * @param file
     */
    public void deleteTempFile(File file) {
        if(file!=null){
            //删除临时文件
            boolean deleteResult = file.delete();
            if(!deleteResult){
                System.out.println("file delete error,filepath="+file.getAbsolutePath());
            }
        }
    }


}
