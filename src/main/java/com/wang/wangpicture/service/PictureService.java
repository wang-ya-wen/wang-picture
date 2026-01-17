package com.wang.wangpicture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wang.wangpicture.model.dto.picture.*;
import com.wang.wangpicture.model.entity.Picture;
import com.wang.wangpicture.model.entity.User;
import com.wang.wangpicture.model.vo.PictureVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 *
 */
public interface PictureService extends IService<Picture> {
   /**
    * 上传图片
    * @param inputSource 文件输入源
    * @param pictureUploadRequest
    * @param loginUser
    * @return
    */
   PictureVo uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);

   /**
    * 获取查询对象
    * @param pictureQueryRequest
    * @return
    */
   QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

   /**
    * 获取图片包装类
    * @param picture
    * @param request
    * @return
    */
   PictureVo getPictureVo(Picture picture, HttpServletRequest request);

   /**
    * 分页获取图片封装类
    * @param picturePage
    * @param request
    * @return
    */
   Page<PictureVo> getPictureVoPage(Page<Picture> picturePage,HttpServletRequest request);

   /**
    * 数据校验
    * @param picture
    */
   void validPicture(Picture picture);

   /**
    * 图片审核
    * @param pictureReviewRequest
    * @param loginUser
    */
   void doPictureReview(PictureReviewRequest pictureReviewRequest,User loginUser);

   /**
    * 填充审核参数
    * @param picture
    * @param loginUser
    */
    void fillReviewParams(Picture picture,User loginUser);

   /**
    * 批量抓取和创建图片
    * @param pictureUploadByBatchRequest
    * @param loginUser
    * @return
    */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest,User loginUser);

   /**
    * 校验空间图片的权限
    * @param loginUser
    * @param picture
    */
    void checkPictureAuth(User loginUser,Picture picture);

   /**
    * 删除图片
    * @param pictureId
    * @param loginUser
    */
    void deletePicture(long pictureId,User loginUser);

   /**
    * 编辑图片
    * @param pictureEditRequest
    * @param loginUser
    */
    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

    /**
     * 通过颜色搜图
     * @param spaceId
     * @param picColor
     * @param loginUser
     * @return
     */
    List<PictureVo> searchPictureByColor(Long spaceId, String picColor, User loginUser);

    /**
     * 清理图片文件
     * @param oldPicture
     */
    void clearPictureFile(Picture oldPicture);
}
