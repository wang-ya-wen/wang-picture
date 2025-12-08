package com.wang.wangpicture.manager;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.*;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.OriginalInfo;
import com.wang.wangpicture.common.ResultUtils;
import com.wang.wangpicture.config.CosClientConfig;
import com.wang.wangpicture.exception.BusinessException;
import com.wang.wangpicture.exception.ErrorCode;
import com.wang.wangpicture.exception.ThrowUtils;
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
 * Deprecated 已废弃，改为使用upload包的模板方法优化
 */
@Slf4j
@Service
@Deprecated
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;
    @Resource
    private COSClient cosClient;
    @Resource
    private CosManager cosManager;
    /**
     * 上传图片
     * @param multipartFile 文件
     * @param uploadPathPrefix  上传路径前缀
     * @return
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile,String uploadPathPrefix){
        //校验图库
        validPicture(multipartFile);
        //图片上传地址
        String uuid= RandomUtil.randomString(16);
        String originalFilename=multipartFile.getOriginalFilename();
        //自己拼接文件上传路径，而不是使用原始文件名称，可以增强安全性
        String uploadFilename=String.format("%s_%s.%s", DateUtil.formatDate(new Date()),uuid,
                FileUtil.getSuffix(originalFilename));
        String uploadPath=String.format("/%s/%s",uploadPathPrefix,uploadFilename);
        //解析结果并返回
        File file=null;
        try {
            //上传文件
            file=File.createTempFile(uploadPath,null);
            multipartFile.transferTo(file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            //获取图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
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
            
            //返回可访问的地址
            return uploadPictureResult;
        } catch (IOException e) {
            System.out.println("图片上传到对象存储失败"+e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传失败");
        }finally {
            deleteTempFile(file);
        }

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
