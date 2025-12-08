package com.wang.wangpicture.controller;

import cn.hutool.http.server.HttpServerResponse;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.utils.IOUtils;
import com.wang.wangpicture.annotation.AuthCheck;
import com.wang.wangpicture.common.BaseResponse;
import com.wang.wangpicture.common.ResultUtils;
import com.wang.wangpicture.config.CosClientConfig;
import com.wang.wangpicture.constant.UserConstant;
import com.wang.wangpicture.exception.BusinessException;
import com.wang.wangpicture.exception.ErrorCode;
import com.wang.wangpicture.manager.CosManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {


    @Resource
    private CosManager cosManager;
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {
        //文件目录
        String filename=multipartFile.getOriginalFilename();
        String filepath=String.format("/test/%s",filename);
        File file=null;
        try {
            //上传文件
            file=File.createTempFile(filepath,null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath,file);
            //返回可访问的地址
            return ResultUtils.success(filepath);
        } catch (IOException e) {
            System.out.println("file upload error,filepath="+filepath);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传失败");
        }finally {
            if(file!=null){
                //删除临时文件
                boolean delete = file.delete();
                if(!delete){
                    System.out.println("file delete error,filepath="+filepath);
                }
            }
        }
    }
    /**
     * 测试文件下载
     */
    @GetMapping("/test/download")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput=null;
        try {
            COSObject cosObject=cosManager.getObject(filepath);
            cosObjectInput= cosObject.getObjectContent();
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            //设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition","attachment;filename="+filepath);

            //写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (IOException e) {
            System.out.println("file download error,fielpath"+filepath);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"下载失败");
        }finally {

            //释放流
            if(cosObjectInput!=null){
                cosObjectInput.close();
            }
        }

    }
}
