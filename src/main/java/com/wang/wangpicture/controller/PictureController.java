package com.wang.wangpicture.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.wang.wangpicture.annotation.AuthCheck;
import com.wang.wangpicture.common.BaseResponse;
import com.wang.wangpicture.common.DeleteRequest;
import com.wang.wangpicture.common.ResultUtils;
import com.wang.wangpicture.constant.UserConstant;
import com.wang.wangpicture.exception.BusinessException;
import com.wang.wangpicture.exception.ErrorCode;
import com.wang.wangpicture.exception.ThrowUtils;
import com.wang.wangpicture.model.dto.picture.*;
import com.wang.wangpicture.model.dto.space.SpaceEditRequest;
import com.wang.wangpicture.model.entity.Picture;
import com.wang.wangpicture.model.entity.Space;
import com.wang.wangpicture.model.entity.User;
import com.wang.wangpicture.model.enums.PictureReviewStatusEnum;
import com.wang.wangpicture.model.enums.UserRoleEnum;
import com.wang.wangpicture.model.vo.PictureTagCategory;
import com.wang.wangpicture.model.vo.PictureVo;
import com.wang.wangpicture.service.PictureService;
import com.wang.wangpicture.service.SpaceService;
import com.wang.wangpicture.service.UserService;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;


@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;
    @Resource
    private SpaceService spaceService;
    /**
     * 上传图片
     * @param multipartFile 文件
     * @param pictureUploadRequest  上传请求
     * @param request  从request中获取登录用户的信息
     * @return
     */
    @PostMapping("/upload")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVo> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request){
        User loginUser=userService.getLoginUser(request);
        PictureVo pictureVo=pictureService.uploadPicture(multipartFile,pictureUploadRequest,loginUser);
        return ResultUtils.success(pictureVo);
    }

    /**
     * 通过URL可重新上传图片
     * @param pictureUploadRequest
     * @param request
     * @return
     */
    @PostMapping("/upload/url")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVo> uploadPictureByUrl(@RequestBody
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request){
        User loginUser=userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVo pictureVo=pictureService.uploadPicture(fileUrl,pictureUploadRequest,loginUser);
        return ResultUtils.success(pictureVo);
    }

    /**
     * 删除图片
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest,HttpServletRequest request){
        if(deleteRequest==null || deleteRequest.getId()<=0 ){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"图片不存在");
        }
        User loginUser=userService.getLoginUser(request);
        Long id=deleteRequest.getId();
        //判断是否存在
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture==null,ErrorCode.NOT_FOUND_ERROR);
        //仅本人或者管理员可以删除
        //仅本人或管理员可编辑
        if(!oldPicture.getUserId().equals(loginUser.getId())&&!userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //操作数据库
        boolean result = pictureService.removeById(id);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新图片
     * @param pictureUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest,HttpServletRequest request){
        if(pictureUpdateRequest==null || pictureUpdateRequest.getId()<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //将实体类和DTO进行转换
        Picture picture=new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest,picture);
        //注意将list转换为string
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        //数据校验
        pictureService.validPicture(picture);
        //判断是否存在
        Long id=pictureUpdateRequest.getId();
        Picture oldPicture=pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture==null,ErrorCode.NOT_FOUND_ERROR);
        User loginUser=userService.getLoginUser(request);
        pictureService.fillReviewParams(picture,loginUser);
        //操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据id获取图片(仅管理员可用)
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id,HttpServletRequest request){
        ThrowUtils.throwIf(id<=0,ErrorCode.PARAMS_ERROR);
        //查询数据库
        Picture picture=pictureService.getById(id);
        ThrowUtils.throwIf(picture==null,ErrorCode.NOT_FOUND_ERROR);
        //获取封装类
        return ResultUtils.success(picture);
    }
    @GetMapping("/get/vo")
    public BaseResponse<PictureVo> getPictureVoById(long id,HttpServletRequest request){
        ThrowUtils.throwIf(id<=0,ErrorCode.PARAMS_ERROR);
        //查询数据库
        Picture picture=pictureService.getById(id);
        ThrowUtils.throwIf(picture==null,ErrorCode.NOT_FOUND_ERROR);
        //空间权限校验
        Long spaceId = picture.getSpaceId();
        if(spaceId!=null){
            User  loginUser=userService.getLoginUser(request);
            pictureService.checkPictureAuth(loginUser,picture);
        }
        //获取封装类
        return ResultUtils.success(pictureService.getPictureVo(picture,request));
    }

    /**
     * 分页获取图片列表(仅管理员可用)
     * @param pictureQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest){
        long current = pictureQueryRequest.getCurrent();
        long picSize = pictureQueryRequest.getPicSize();
        //查询数据库
        Page<Picture> picturePage=pictureService.page(new Page<>(current,picSize),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片列表（给普通用户使用)
     * @param pictureQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVo>> listPictureVoByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                            HttpServletRequest request){
        long current = pictureQueryRequest.getCurrent();
        long picSize = pictureQueryRequest.getPicSize();
        //限制爬虫
        ThrowUtils.throwIf(picSize>20,ErrorCode.PARAMS_ERROR);

        //空间权限jiaoyan
        Long spaceId = pictureQueryRequest.getSpaceId();
        if(spaceId==null){
            //公开图库
            //普通用户默认只能看到审核通过的数据
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
             pictureQueryRequest.setNullSpaceId(true);
        }else{
            User loginUser=userService.getLoginUser(request);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space==null,ErrorCode.NOT_FOUND_ERROR,"空间不存在");
            if(!loginUser.getId().equals(space.getUserId())){
                 throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"没有空间权限");
            }
        }
        //查询数据库
        Page<Picture> picturePage=pictureService.page(new Page<>(current,picSize),
                pictureService.getQueryWrapper(pictureQueryRequest));
        //获取封装类
        return ResultUtils.success(pictureService.getPictureVoPage(picturePage,request));
    }

    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request){
        if(pictureEditRequest==null || pictureEditRequest.getId()<=0){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        //在此处将实体类和DTO进行转换
        Picture picture=new Picture();
        BeanUtils.copyProperties(pictureEditRequest,picture);

        //设置编辑时间
        picture.setEditTime(new Date());
        //数据校验
        pictureService.validPicture(picture);
        User loginUser=userService.getLoginUser(request);
        //自动填充数据
        pictureService.fillReviewParams(picture,loginUser);
        //判断是否存在
        long id=pictureEditRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture==null,ErrorCode.NOT_FOUND_ERROR);
        //仅本人或管理员可编辑
        if(!oldPicture.getId().equals(loginUser.getId())&&!userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);

    }

    /**
     * 图片分类的标签
     * @return
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory(){
        PictureTagCategory pictureTagCategory=new PictureTagCategory();
        List<String> tagList= Arrays.asList("热门","搞笑","生活","高清","艺术","校园","背景","简历","创意");
        List<String> categoryList=Arrays.asList("模板","电商","表情包","素材","海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);

    }
   @PostMapping("/review")
   @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
   public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,HttpServletRequest request){
        ThrowUtils.throwIf(pictureReviewRequest==null,ErrorCode.PARAMS_ERROR);
        User loginUser=userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest,loginUser);
        return ResultUtils.success(true);

   }

    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,HttpServletRequest request){
        ThrowUtils.throwIf(pictureUploadByBatchRequest==null,ErrorCode.PARAMS_ERROR);
        User loginUser=userService.getLoginUser(request);
        int count=pictureService.uploadPictureByBatch(pictureUploadByBatchRequest,loginUser);
        return ResultUtils.success(count);

    }

}
