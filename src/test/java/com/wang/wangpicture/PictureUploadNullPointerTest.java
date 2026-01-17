package com.wang.wangpicture;

import com.qcloud.cos.COSClient;
import com.wang.wangpicture.exception.BusinessException;
import com.wang.wangpicture.exception.ErrorCode;
import com.wang.wangpicture.manager.upload.FilePictureUpload;
import com.wang.wangpicture.manager.upload.UrlPictureUpload;
import com.wang.wangpicture.model.dto.file.UploadPictureResult;
import com.wang.wangpicture.model.dto.picture.PictureUploadRequest;
import com.wang.wangpicture.model.entity.User;
import com.wang.wangpicture.service.PictureService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 图片上传空指针问题排查测试类
 * 运行前确保：
 * 1. 本地有测试图片（建议放在 src/test/resources 目录下）
 * 2. 腾讯云COS配置正确
 * 3. 数据库已启动且表结构完整
 */
@SpringBootTest // 启动Spring容器，加载所有Bean
public class PictureUploadNullPointerTest {

    private static final Logger log = LoggerFactory.getLogger(PictureUploadNullPointerTest.class);

    // ========== 1. 注入需要测试的核心对象 ==========
    @Resource
    private PictureService pictureService; // 图片上传核心服务

    @Resource
    private FilePictureUpload filePictureUpload; // 文件上传模板

    @Resource
    private UrlPictureUpload urlPictureUpload; // URL上传模板

    @Resource
    private COSClient cosClient; // COS客户端

    // 测试文件路径（建议使用项目相对路径，避免硬编码绝对路径）
    private static final String TEST_FILE_PATH = "https://wyw-1390165950.cos.ap-guangzhou.myqcloud.com/test/deepseek_mermaid_20251120_86272f.png";
    // 备用测试文件路径（本地桌面）
    private static final String BACKUP_TEST_FILE_PATH = "C:/Users/wang/Desktop/1111.png";
    // 测试URL（使用稳定的公共图片URL）
    private static final String TEST_IMAGE_URL = "https://picsum.photos/800/600";
    // 测试用户ID
    private static final Long TEST_USER_ID = 1995686552845545474L;

    // ========== 2. 测试1：校验所有依赖对象是否注入成功（核心） ==========
    @Test
    public void testDependencyInjection() {
        log.info("===== 开始校验依赖注入 =====");

        // 校验核心服务
        if (pictureService == null) {
            log.error("❌ PictureService 注入失败，为null！");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "PictureService 注入失败");
        } else {
            log.info("✅ PictureService 注入成功");
        }

        // 校验文件上传模板
        if (filePictureUpload == null) {
            log.error("❌ filePictureUpload 注入失败，为null！");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "filePictureUpload 注入失败");
        } else {
            log.info("✅ filePictureUpload 注入成功");
        }

        // 校验URL上传模板
        if (urlPictureUpload == null) {
            log.error("❌ urlPictureUpload 注入失败，为null！");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "urlPictureUpload 注入失败");
        } else {
            log.info("✅ urlPictureUpload 注入成功");
        }

        // 校验COS客户端
        if (cosClient == null) {
            log.error("❌ COSClient 注入失败，为null！");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "COSClient 注入失败");
        } else {
            log.info("✅ COSClient 注入成功");
        }

        log.info("===== 依赖注入校验完成 =====");
    }

    // ========== 3. 测试2：模拟文件上传（修复File转MultipartFile问题） ==========
    @Test
    public void testFileUpload() {
        log.info("===== 开始模拟文件上传测试 =====");

        // 1. 获取有效的测试文件
        File testFile = getValidTestFile();
        if (testFile == null) {
            log.error("❌ 没有找到可用的测试图片，请检查文件路径");
            return;
        }

        // 2. 将File转换为MultipartFile（核心修复点）
        MultipartFile multipartFile = convertFileToMultipartFile(testFile);
        if (multipartFile == null) {
            log.error("❌ File转换为MultipartFile失败");
            return;
        }

        // 3. 构造测试数据
        // 构造登录用户（模拟管理员）
        User loginUser = buildTestUser();
        // 构造上传请求
        PictureUploadRequest uploadRequest = buildPictureUploadRequest("测试图片-" + System.currentTimeMillis());

        // 4. 执行上传（捕获并打印所有异常）
        try {
            // 调用核心上传方法（传入MultipartFile而非File）
            pictureService.uploadPicture(multipartFile, uploadRequest, loginUser);
            log.info("✅ 文件上传测试成功！");
        } catch (NullPointerException e) {
            log.error("❌ 文件上传触发空指针异常！", e);
        } catch (BusinessException e) {
            log.error("❌ 文件上传触发业务异常：{}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ 文件上传触发未知异常：", e);
        }

        log.info("===== 文件上传测试结束 =====");
    }

    // ========== 4. 测试3：模拟URL上传（优化稳定性） ==========
    @Test
    public void testUrlUpload() {
        log.info("===== 开始模拟URL上传测试 =====");

        // 1. 构造测试数据
        String testUrl = TEST_IMAGE_URL;
        User loginUser = buildTestUser();
        PictureUploadRequest uploadRequest = buildPictureUploadRequest("URL测试图片-" + System.currentTimeMillis());

        // 2. 执行上传
        try {
            pictureService.uploadPicture(testUrl, uploadRequest, loginUser);
            log.info("✅ URL上传测试成功！");
        } catch (NullPointerException e) {
            log.error("❌ URL上传触发空指针异常！", e);
        } catch (BusinessException e) {
            log.error("❌ URL上传触发业务异常：{}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ URL上传触发未知异常：", e);
        }

        log.info("===== URL上传测试结束 =====");
    }

    // ========== 5. 测试4：单独测试上传模板的uploadPicture方法 ==========
    @Test
    public void testUploadTemplate() {
        log.info("===== 开始测试上传模板核心方法 =====");

        // 1. 先校验模板
        testDependencyInjection();

        // 2. 获取有效的测试文件
        File testFile = getValidTestFile();
        if (testFile == null) {
            log.error("❌ 没有找到可用的测试图片，跳过模板测试");
            return;
        }

        // 3. 测试文件上传模板
        String uploadPath = "test/" + System.currentTimeMillis() + "/";
        try {
            // 如果uploadPicture方法期望File：直接传入
            UploadPictureResult result = filePictureUpload.uploadPicture(testFile, uploadPath);

            // 如果uploadPicture方法期望MultipartFile：转换后传入
            // MultipartFile multipartFile = convertFileToMultipartFile(testFile);
            // UploadPictureResult result = filePictureUpload.uploadPicture(multipartFile, uploadPath);

            if (result == null) {
                log.error("❌ filePictureUpload.uploadPicture 返回null！");
            } else {
                log.info("✅ filePictureUpload 上传成功，返回URL：{}", result.getUrl());
            }
        } catch (NullPointerException e) {
            log.error("❌ filePictureUpload.uploadPicture 触发空指针！", e);
        } catch (Exception e) {
            log.error("❌ filePictureUpload 上传失败：", e);
        }

        log.info("===== 上传模板测试结束 =====");
    }

    // ========== 私有工具方法 ==========

    /**
     * 获取有效的测试文件（先找默认路径，找不到再找备用路径）
     */
    private File getValidTestFile() {
        File testFile = new File(TEST_FILE_PATH);
        if (testFile.exists() && testFile.isFile()) {
            log.info("✅ 找到测试文件：{}", testFile.getAbsolutePath());
            return testFile;
        }

        testFile = new File(BACKUP_TEST_FILE_PATH);
        if (testFile.exists() && testFile.isFile()) {
            log.info("✅ 找到备用测试文件：{}", testFile.getAbsolutePath());
            return testFile;
        }

        return null;
    }

    /**
     * 将File转换为MultipartFile（解决类型转换异常的核心方法）
     */
    private MultipartFile convertFileToMultipartFile(File file) {
        try (InputStream inputStream = new FileInputStream(file)) {
            return new MockMultipartFile(
                    "file",                  // 请求参数名（对应接口接收的参数名）
                    file.getName(),          // 文件名
                    "image/png",             // 文件类型
                    inputStream              // 文件输入流
            );
        } catch (IOException e) {
            log.error("❌ 转换File到MultipartFile失败：{}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 构建测试用户对象
     */
    private User buildTestUser() {
        User loginUser = new User();
        loginUser.setId(TEST_USER_ID);
        loginUser.setUserRole("admin");
        return loginUser;
    }

    /**
     * 构建图片上传请求对象
     */
    private PictureUploadRequest buildPictureUploadRequest(String picName) {
        PictureUploadRequest uploadRequest = new PictureUploadRequest();
        uploadRequest.setPicName(picName);
        return uploadRequest;
    }
}