package com.wang.wangpicture.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wang.wangpicture.common.ResultUtils;
import com.wang.wangpicture.exception.BusinessException;
import com.wang.wangpicture.exception.ErrorCode;
import com.wang.wangpicture.exception.ThrowUtils;
import com.wang.wangpicture.manager.FileManager;
import com.wang.wangpicture.manager.upload.FilePictureUpload;
import com.wang.wangpicture.manager.upload.PictureUploadTemplate;
import com.wang.wangpicture.manager.upload.UrlPictureUpload;
import com.wang.wangpicture.mapper.PictureMapper;
import com.wang.wangpicture.model.dto.file.UploadPictureResult;
import com.wang.wangpicture.model.dto.picture.PictureQueryRequest;
import com.wang.wangpicture.model.dto.picture.PictureReviewRequest;
import com.wang.wangpicture.model.dto.picture.PictureUploadByBatchRequest;
import com.wang.wangpicture.model.dto.picture.PictureUploadRequest;
import com.wang.wangpicture.model.entity.Picture;
import com.wang.wangpicture.model.entity.Space;
import com.wang.wangpicture.model.entity.User;
import com.wang.wangpicture.model.enums.PictureReviewStatusEnum;
import com.wang.wangpicture.model.vo.PictureVo;
import com.wang.wangpicture.model.vo.UserVo;
import com.wang.wangpicture.service.PictureService;
import com.wang.wangpicture.service.SpaceService;
import com.wang.wangpicture.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 *图片实现
 */
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService {

    @Resource
    private FileManager fileManager;
    @Resource
    private UserService userService;
    @Resource
    private FilePictureUpload filePictureUpload;
    @Resource
    private UrlPictureUpload urlPictureUpload;
    @Resource
    private SpaceService spaceService;
    @Override
    public PictureVo uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        //校验参数
        ThrowUtils.throwIf(loginUser==null, ErrorCode.NO_AUTH_ERROR);
        //校验空间是否存在
        Long spaceId = pictureUploadRequest.getSpaceId();
        if(spaceId!=null){
            Space space=spaceService.getById(spaceId);
            ThrowUtils.throwIf(space==null,ErrorCode.NOT_FOUND_ERROR,"空间不存在");
            //校验是否有空间的权限，仅管理员才能上传

        }
        //校验是否有空间的权限
        //判断是新增还是删除
        Long pictureId=null;
        if(pictureUploadRequest!=null){
            pictureId=pictureUploadRequest.getId();
        }
        //如果是更新，判断图片是否存在
        if(pictureId!=null){
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture==null,ErrorCode.NOT_FOUND_ERROR,"图片不存在");
            //仅本人或管理员可编辑图片
            if(!oldPicture.getUserId().equals(loginUser.getId())&&!userService.isAdmin(loginUser)){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
//            boolean exists = this.lambdaQuery()
//                    .eq(Picture::getId, pictureId)
//                    .exists();
//            ThrowUtils.throwIf(!exists,ErrorCode.NOT_FOUND_ERROR,"图片不存在");
        }
        //上传图片，得到图片信息
        //按照用户id,划分目录
        String unloadPathPrefix=String.format("public/%s",loginUser.getId());
        //根据inputSource的类型区分文件上传方式
        PictureUploadTemplate pictureUploadTemplate=filePictureUpload;
        if(inputSource instanceof String){
            pictureUploadTemplate=urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, unloadPathPrefix);
        //构造要构造的图片信息
        Picture picture=new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        //支持外层传递名称
        String picName=uploadPictureResult.getPicName();
        if(pictureUploadRequest!=null&&StrUtil.isNotBlank(pictureUploadRequest.getPicName())){
            picName=pictureUploadRequest.getPicName();
        }
        picture.setName(picName);
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        //补充审核参数
        this.fillReviewParams(picture,loginUser);
        //操作数据库
        //如果picId不为空表示更新，否则是新增
        if(pictureId!=null){
            //如果不是更新，需要补充id和编辑时间
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }

        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR,"图片上传失败");
        return PictureVo.objToVo(picture);
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
            QueryWrapper<Picture> queryWrapper=new QueryWrapper<>();
            if(pictureQueryRequest==null){
                return queryWrapper;
            }
            //从对象中取值
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewrId = pictureQueryRequest.getReviewrId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        //从多字段中搜索
        if(StrUtil.isNotBlank(searchText)){
            //需要拼接查询条件
            queryWrapper.and(qw->qw.like("name",searchText)
            .or()
            .like("introduction",searchText));
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id),"id",id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId),"userId",userId);
        queryWrapper.like(StrUtil.isNotBlank(name),"name",name);
        queryWrapper.like(StrUtil.isNotBlank(introduction),"introduction",introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat),"picFormat",picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category),"category",category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth),"picWidth",picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight),"picHeight",picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize),"picWidth",picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale),"picWidth",picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus),"reviewStatus",reviewStatus);
        queryWrapper.like(StrUtil.isNotEmpty(reviewMessage),"reviewMessage",reviewMessage);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewrId),"reviewrId",reviewrId);

        //JSON数据查询
        if(CollUtil.isNotEmpty(tags)){
            for(String tag:tags){
                queryWrapper.like("tags","\"+tag+\"");
            }
        }
        //排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField),sortOrder.equals("ascend"),sortField);
        return queryWrapper;

    }

    @Override
    public PictureVo getPictureVo(Picture picture, HttpServletRequest request) {
        //对象转封装类
        PictureVo pictureVo=PictureVo.objToVo(picture);
        //关联查询用户信息
        Long userId=picture.getUserId();
        if(userId!=null&&userId>0){
            User user=userService.getById(userId);
            UserVo userVo=userService.getUserVO(user);
            pictureVo.setUser(userVo);
        }
        return pictureVo;
    }

    @Override
    public Page<PictureVo> getPictureVoPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVo> pictureVoPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(),picturePage.getTotal());
        if(CollUtil.isEmpty(pictureList)){
            return pictureVoPage;
        }
        //对象列表=>封装对象列表
        List<PictureVo> pictureVoList=pictureList.stream().map(PictureVo::objToVo).collect(Collectors.toList());
        //1.关联查询用户信息
        Set<Long> userIdSet=pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIduSERListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        //2.填充信息
        pictureVoList.forEach(pictureVo -> {
            Long userId = pictureVo.getUserId();
            User user=null;
            if(userIduSERListMap.containsKey(userId)){
                user=userIduSERListMap.get(userId).get(0);
            }
            pictureVo.setUser(userService.getUserVO(user));
        });
        pictureVoPage.setRecords(pictureVoList);
        return pictureVoPage;
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture==null,ErrorCode.PARAMS_ERROR);
        //从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        //修改数据时id不能为空,有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id),ErrorCode.PARAMS_ERROR,"id 不能为空");
        if(StrUtil.isNotBlank(url)){
            ThrowUtils.throwIf(url.length()>1024,ErrorCode.PARAMS_ERROR,"url过长");

        }
        if(StrUtil.isNotBlank(introduction)){
            ThrowUtils.throwIf(introduction.length()>800,ErrorCode.PARAMS_ERROR,"简介过长");
        }

    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        //1.校验参数
        ThrowUtils.throwIf(pictureReviewRequest==null,ErrorCode.PARAMS_ERROR);
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        String reviewMessage = pictureReviewRequest.getReviewMessage();
        if(id==null||reviewStatusEnum==null||PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.判断图片是否存在
        Picture oldPicture=this.getById(id);
        ThrowUtils.throwIf(oldPicture==null,ErrorCode.PARAMS_ERROR);
        //3.校验审核状态是否重复
        if(oldPicture.getReviewStatus().equals(reviewStatus)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请勿重复审核");
        }
        //4.数据库操作
        Picture updatePicture=new Picture();
        BeanUtils.copyProperties(pictureReviewRequest,updatePicture);
        updatePicture.setReviewrId(loginUser.getId());
        updatePicture.setReviewTime(new Date());
        boolean result = this.updateById(updatePicture);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);

    }

    /**
     * 填充审核参数
     * @param picture
     * @param loginUser
     */
    @Override
    public void fillReviewParams(Picture picture,User loginUser){
        if(userService.isAdmin(loginUser)){
            //管理员自动过审
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewrId(loginUser.getId());
            picture.setReviewTime(new Date());
            
        }else{
            //非管理员，无论是编辑还是创建默认都是待审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }

        
    }

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        //校验参数
        String searchText = pictureUploadByBatchRequest.getSearchText();
        Integer count = pictureUploadByBatchRequest.getCount();
        //名称前缀默认等于搜索关键词
        String namePrefix=pictureUploadByBatchRequest.getNamePrefix();
        if(StrUtil.isBlank(namePrefix)){
            namePrefix=searchText;
        }
        ThrowUtils.throwIf(count>30,ErrorCode.PARAMS_ERROR,"最多30张");
        //抓取内容
        String fetchUrl=String.format("https://www.bing.com/images/async?q=%s&mmasync=1",searchText);
        Document document;

        try {
            document= Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            System.out.println("获取页面失败"+e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"获取页面失败");
        }
        //解析内容
        Element div=document.getElementsByClass("dgControl").first();
        if(ObjUtil.isEmpty(div)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"获取元素失败");
        }
        Elements imgElementList=div.select("img.mimg");
        //遍历元素，依次处理上传图片
        int uploadCount=0;
        for(Element imgElement:imgElementList){
            String fileUrl=imgElement.attr("src");
            if(StrUtil.isBlank(fetchUrl)){
                System.out.println("当前连接为空，已跳过{}"+fileUrl);
                continue;
            }
            //处理图片的地址，防止转义或者和对象存储冲突的问题
            //codefather.cn?wang 应该只保留codefather.cn
            int questionMarkIndex = fileUrl.indexOf("?");
            if(questionMarkIndex>-1){
                fileUrl=fileUrl.substring(0,questionMarkIndex);
            }
            //上传图片
            PictureUploadRequest pictureUploadRequest=new PictureUploadRequest();
            pictureUploadRequest.setFileUrl(fileUrl);
            pictureUploadRequest.setPicName(namePrefix+(uploadCount+1));
            try{
            PictureVo pictureVo = this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
            System.out.println("图片上传成功，id={}"+pictureVo.getId());
            uploadCount++;}
            catch (Exception e){
                System.out.println("上传失败"+e);
                continue;
            }
            if(uploadCount>=count){
                break;
            }
        }
        return uploadCount;
    }

}




