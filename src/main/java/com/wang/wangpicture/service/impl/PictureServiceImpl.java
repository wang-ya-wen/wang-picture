package com.wang.wangpicture.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
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
import com.wang.wangpicture.model.dto.picture.*;
import com.wang.wangpicture.model.entity.Picture;
import com.wang.wangpicture.model.entity.Space;
import com.wang.wangpicture.model.entity.User;
import com.wang.wangpicture.model.enums.PictureReviewStatusEnum;
import com.wang.wangpicture.model.vo.PictureVo;
import com.wang.wangpicture.model.vo.UserVo;
import com.wang.wangpicture.service.PictureService;
import com.wang.wangpicture.service.SpaceService;
import com.wang.wangpicture.service.UserService;
import com.wang.wangpicture.utils.ColorSimilarUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
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
    @Resource
    private TransactionTemplate transactionTemplate;
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
            if(!loginUser.getId().equals(space.getId())){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"仅管理员可以创建空间");
            }
            //校验额度
            if(space.getTotalCount()>=space.getMaxCount()){
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"空间条数不足");
            }
            if(space.getTotalSize()>=space.getMaxSize()){
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"空间大小不足");
            }
        }
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
            //校验上传空间的id和自己创建的空间id是否一致
            //没传spaceId,则服用原有的图片的spaceId
            if(spaceId==null){
                if(oldPicture.getSpaceId()!=null){
                    spaceId=oldPicture.getSpaceId();
                }
                else{
                    //传了spaceId,必须和原图片的空间id一致
                    if(ObjUtil.notEqual(spaceId,oldPicture.getSpaceId())){
                        throw new BusinessException(ErrorCode.PARAMS_ERROR,"空间id不一致");
                    }
                }
            }
//            boolean exists = this.lambdaQuery()
//                    .eq(Picture::getId, pictureId)
//                    .exists();
//            ThrowUtils.throwIf(!exists,ErrorCode.NOT_FOUND_ERROR,"图片不存在");
        }
        //上传图片，得到图片信息
        //按照用户id,划分目录=>按照空间划分目录
        String unloadPathPrefix;
        if(spaceId==null){
            unloadPathPrefix=String.format("public/%s",loginUser.getId());
        }else{
            unloadPathPrefix=String.format("space/%s",spaceId);
        }

        //根据inputSource的类型区分文件上传方式
        PictureUploadTemplate pictureUploadTemplate=filePictureUpload;
        if(inputSource instanceof String){
            pictureUploadTemplate=urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, unloadPathPrefix);
        //构造要构造的图片信息
        Picture picture=new Picture();
        picture.setSpaceId(spaceId); //指定空间id
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
        picture.setPicColor(uploadPictureResult.getPicColor());
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
        //开启事务
        Long finalSpaceId = spaceId;
        transactionTemplate.execute(status -> {
            boolean result = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR,"图片上传失败");
            boolean update = spaceService.lambdaUpdate()
                    .eq(Space::getId, finalSpaceId)
                    .setSql("totalSize=totalSize+" + picture.getPicSize())
                    .setSql("totalCount=totalCount+1")
                    .update();
            ThrowUtils.throwIf(!update,ErrorCode.OPERATION_ERROR,"额度更新失败");
            return picture;
        });
        //更新空间的使用额度
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
        Long spaceId = pictureQueryRequest.getSpaceId();
        boolean nullSpaceId = pictureQueryRequest.isNullSpaceId();
        Date startEditTime = pictureQueryRequest.getStartEditTime();
        Date endEditTime=pictureQueryRequest.getEndEditTime();
        //从多字段中搜索
        if(StrUtil.isNotBlank(searchText)){
            //需要拼接查询条件
            queryWrapper.and(qw->qw.like("name",searchText)
            .or()
            .like("introduction",searchText));
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id),"id",id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId),"userId",userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId),"spaceId",spaceId);
        queryWrapper.isNull(nullSpaceId,"nullSpaceId");
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
        //大于等于开始时间
        queryWrapper.ge(ObjUtil.isNotEmpty(startEditTime),"startEditTime",startEditTime);
        //小于等于结束时间
        queryWrapper.lt(ObjUtil.isNotEmpty(endEditTime),"endEditTime",endEditTime);

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
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();

        // 修复：仅更新时校验id，新增时id可为空
        if(id != null){ // 有id表示更新，才校验id
            ThrowUtils.throwIf(id <= 0,ErrorCode.PARAMS_ERROR,"id 不合法");
        }
        // 必须校验url（图片url不能为空）
        ThrowUtils.throwIf(StrUtil.isBlank(url),ErrorCode.PARAMS_ERROR,"图片URL不能为空");
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

    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        Long spaceId=picture.getSpaceId();
        Long loginUserId=loginUser.getId();
        if(spaceId==null){
            // 公共图库，仅本人或管理员可操作
            if(!picture.getUserId().equals(loginUserId)&&!userService.isAdmin(loginUser)){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        } else {
            // 私有空间,仅空间管理员可操作
            Space space = spaceService.getById(spaceId);
            if(space == null || !space.getUserId().equals(loginUserId)){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
    }

    @Override
    public void deletePicture(long pictureId, User loginUser) {
        ThrowUtils.throwIf(pictureId<=0,ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser==null,ErrorCode.NO_AUTH_ERROR);
        //判断是否存在
        Picture oldPicture=this.getById(pictureId);
        ThrowUtils.throwIf(oldPicture==null,ErrorCode.NOT_FOUND_ERROR);
        //校验权限
        checkPictureAuth(loginUser,oldPicture);
        //开启事务
        transactionTemplate.execute(status -> {
            boolean result = this.removeById(pictureId);
            ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
            //更新空间的使用额度，释放额度
            // 原代码：更新空间时用了 picture 的 id，应该用 spaceId
            boolean update = spaceService.lambdaUpdate()
                    .eq(Space::getId, oldPicture.getSpaceId()) // 修复：oldPicture.getId() → oldPicture.getSpaceId()
                    .setSql("totalSize=totalSize-" + oldPicture.getPicSize())
                    .setSql("totalCount=totalCount-1")
                    .update();
            ThrowUtils.throwIf(!update,ErrorCode.OPERATION_ERROR,"额度更新失败");
            return true;
        });
        //操作数据库



    }
    @Override
    public void editPicture(PictureEditRequest pictureEditRequest,User loginUser){
        //在此处将实体类和DTO类进行转换
        Picture picture=new Picture();
        BeanUtils.copyProperties(pictureEditRequest,picture);
        //注意list转为String
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        //设置编辑时间
        picture.setEditTime(new Date());
        //数据校验
        this.validPicture(picture);
        //判断是否存在
        long id=pictureEditRequest.getId();
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture==null,ErrorCode.NOT_FOUND_ERROR);
        //校验权限
        checkPictureAuth(loginUser,oldPicture);
        //补充审核参数
        this.fillReviewParams(picture,loginUser);
        //操作数据库
        boolean result =this.updateById(picture);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);


    }

    @Override
    public List<PictureVo> searchPictureByColor(Long spaceId, String picColor, User loginUser) {
        //1.校验参数
        ThrowUtils.throwIf(spaceId == null || StrUtil.isBlank(picColor), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        //2.校验空间权限
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        if (!space.getId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
        }

        //3.查询该空间下的所有图片（必须要有主色调)
        List<Picture> pictureList = this.lambdaQuery()
                .eq(Picture::getSpaceId, spaceId)
                .isNotNull(Picture::getPicColor)
                .list();
        //如果没有，直接返回空列表
        if (CollUtil.isEmpty(pictureList)) {
            return new ArrayList<>();
        }
        //4.计算相似度，返回相似度最高的图片
        Color targetColor = Color.decode(picColor);
        List<Picture> sortedPictureList = pictureList.stream()
                .sorted(Comparator.comparingDouble(picture -> {
                    //获取图片主色调
                    String hexColor = picture.getPicColor();
                    //如果没有主色调图片会默认排序到最后
                    if (StrUtil.isBlank(hexColor)) {
                        return Double.MAX_VALUE;
                    }
                    Color pictureColor = Color.decode(hexColor);
                    //计算相似度
                    return -ColorSimilarUtils.calculateSimilarity(targetColor, pictureColor);
                }))
                .limit(12)
                .collect(Collectors.toList());
        //5.返回结果
        return sortedPictureList.stream()
                .map(PictureVo::objToVo)
                .collect(Collectors.toList());


    }
}




