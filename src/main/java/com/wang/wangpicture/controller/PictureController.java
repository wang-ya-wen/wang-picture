package com.wang.wangpicture.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sun.corba.se.impl.logging.POASystemException;
import com.wang.wangpicture.annotation.AuthCheck;
import com.wang.wangpicture.api.aliyun.AliYunAiApi;
import com.wang.wangpicture.api.aliyun.model.CreateOutPaintingTaskRequest;
import com.wang.wangpicture.api.aliyun.model.CreateOutPaintingTaskResponse;
import com.wang.wangpicture.api.aliyun.model.GetOutPaintingTaskResponse;
import com.wang.wangpicture.api.imageSearch.ImageSearchApiFacade;
import com.wang.wangpicture.api.imageSearch.model.ImageSearchResult;
import com.wang.wangpicture.common.BaseResponse;
import com.wang.wangpicture.common.DeleteRequest;
import com.wang.wangpicture.common.ResultUtils;
import com.wang.wangpicture.constant.UserConstant;
import com.wang.wangpicture.exception.BusinessException;
import com.wang.wangpicture.exception.ErrorCode;
import com.wang.wangpicture.exception.ThrowUtils;
import com.wang.wangpicture.manager.auth.model.SpaceUserPermissionConstant;
import com.wang.wangpicture.model.dto.picture.*;
import com.wang.wangpicture.model.entity.Picture;
import com.wang.wangpicture.model.entity.Space;
import com.wang.wangpicture.model.entity.User;
import com.wang.wangpicture.model.enums.PictureReviewStatusEnum;
import com.wang.wangpicture.model.vo.PictureTagCategory;
import com.wang.wangpicture.model.vo.PictureVo;
import com.wang.wangpicture.service.PictureService;
import com.wang.wangpicture.service.SpaceService;
import com.wang.wangpicture.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

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
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 本地缓存
     */

    private final Cache<String,String> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(1025)
            .maximumSize(10_000L)
            //缓存5分钟后移除
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();
    //使用热key
    /**
     * 1. 热点数据本地缓存 (存放 PictureVO)
     * 设置较短的过期时间（如 1 分钟），因为热点可能随时变化，且防止数据长时间不一致
     */
    private final Cache<Long, PictureVo> LOCAL_HOT_DATA = Caffeine.newBuilder()
            .initialCapacity(1024)
            .maximumSize(10_000L)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    /**
     * 2. 访问计数器 (用于探测热点)
     * key: pictureId, value: 访问次数
     * 5秒内访问次数统计，过期后自动重置，相当于一个简易的滑动窗口
     */
    private final Cache<Long, AtomicLong> LOCAL_COUNTER = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build();

    /**
     * 热点阈值：5秒内访问超过 10 次就算热点（根据实际流量调整）
     */
    private static final int HOT_THRESHOLD = 10;
    @Autowired
    private AliYunAiApi aliYunAiApi;

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
        pictureService.deletePicture(deleteRequest.getId(),loginUser);
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
        //todo 可自行实现，如果是更新，可以清理图片资源
        //    pictureService.clearPictureFile(oldPicture);
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

    /**
     * 根据id获取图片(给普通用户使用)
     * @param id
     * @param request
     * @return
     */
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
     * 根据 id 获取图片（封装类）- 带热点探测
     */
    @GetMapping("/get/vo/withKey")
    public BaseResponse<PictureVo> getPictureVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);

        // --- 【核心逻辑 A：先查本地热点缓存】 ---
        PictureVo pictureVO = LOCAL_HOT_DATA.getIfPresent(id);
        if (pictureVO != null) {
            // 命中缓存，但注意：PermissionList 是跟用户相关的，不能缓存，需要单独处理
            // 这里我们需要深拷贝或者重新处理 user 相关的动态部分，
            // 但因为 PictureVO 主要是静态数据，User 和 Space 相对固定，
            // 唯独 permissionList 必须在下文重新计算。
        } else {
            // --- 【核心逻辑 B：未命中，查数据库】 ---

            // 1. 正常查询数据库
            Picture picture = pictureService.getById(id);
            ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);

            // 2. 转换为 VO
            pictureVO = pictureService.getPictureVo(picture, request);

            // --- 【核心逻辑 C：热点探测与写入】 ---
            // 获取计数器（如果不存在则新建，初始值为 0）
            AtomicLong counter = LOCAL_COUNTER.get(id, key -> new AtomicLong(0));
            // 计数 +1
            long count = counter.incrementAndGet();

            // 如果访问量超过阈值，写入本地热点缓存
            if (count >= HOT_THRESHOLD) {
                // 存入 Caffeine
                LOCAL_HOT_DATA.put(id, pictureVO);
                // 可选：打印日志方便调试
                // log.info("发现热点图片 id: {}, 当前 QPS 估算: {}", id, count / 5.0);
            }
        }

        // --- 【核心逻辑 D：处理动态数据（权限校验）】 ---
        // 这部分逻辑必须每次执行，不能缓存，因为不同用户对同一张图的权限不同
        Long spaceId = pictureVO.getSpaceId();
        Space space = null;
        if (spaceId != null) {
//            boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
//            ThrowUtils.throwIf(!hasPermission, ErrorCode.NO_AUTH_ERROR);
            space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 重新计算权限列表（这是动态的！）
//        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
//        pictureVO.setPermissionList(permissionList);

        return ResultUtils.success(pictureVO);
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
    /**
     * 分页获取图片列表（封装类，有缓存的)
     * @param pictureQueryRequest
     * @param request
     * @return
     */
    @Deprecated
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVo>> listPictureVoByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                                      HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize(); // 建议统一变量名，MyBatis Plus通常用 pageSize

        // 1. 限制爬虫/非法请求
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        // 2. 普通用户只能看审核通过的 (这一步必须在生成 Cache Key 之前做，否则 key 不准)
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        // 3. 构建缓存 Key
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String redisKey = String.format("picture:listPictureVoByPage:%s", hashKey);
        String cacheKey = String.format("listPictureVoByPage:%s", hashKey);
        //1.先查本地缓存
        //从本地缓存中查询
        String cacheValue = LOCAL_CACHE.getIfPresent(cacheKey);
        if(cacheValue!=null){
            //本地缓存命中，返回结果
            Page<PictureVo> cacheResult = JSONUtil.toBean(cacheValue, Page.class);
            return ResultUtils.success(cacheResult);
        }
        //2.本地缓存未命中，查询Redis分布式缓存
        // 4. 查询 Redis 缓存
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
//        String redisValue = opsForValue.get(redisKey);
        cacheValue=opsForValue.get(redisKey);
        if(cacheValue!=null){
            //如果缓存命中，更新本地缓存，返回结果
            LOCAL_CACHE.put(cacheKey,cacheValue);
            Page<PictureVo> cacheResult = JSONUtil.toBean(cacheValue, Page.class);
            return ResultUtils.success(cacheResult);
        }
//        // 5. 缓存命中
//        if (StrUtil.isNotBlank(cacheValue)) {
//
//            Page<PictureVo> cacheResult = JSONUtil.toBean(cacheValue, Page.class);
//            return ResultUtils.success(cacheResult);
//        }

        // 6. 缓存未命中，查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));

        // 7. 封装 VO
        Page<PictureVo> pictureVoPage = pictureService.getPictureVoPage(picturePage, request);

        // 8. 存入缓存 (注意：如果结果为空，也建议缓存一个短时间，防止缓存穿透)
        String cacheResult = JSONUtil.toJsonStr(pictureVoPage);

        // 设置随机过期时间 (5-10分钟)，防止雪崩
        int cacheExpireTime = 300 + RandomUtil.randomInt(0, 300);
        opsForValue.set(redisKey, cacheResult, cacheExpireTime, TimeUnit.SECONDS);
        //写入本地缓存
        LOCAL_CACHE.put(cacheKey, cacheResult);

        return ResultUtils.success(pictureVoPage);
    }

    /**
     * 分页获取图片列表（给普通用户
        }
        //查询数据库
        Page<Picture> picturePage=pictureService.page(new Page<>(current,picSize),
                pictureService.getQueryWrapper(pictureQueryRequest));
        //获取封装类
        return ResultUtils.success(pictureService.getPictureVoPage(picturePage,request));
    }
    /**
     * 编辑图片(仅本人或管理员可用)
     * @param pictureEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request){
        if(pictureEditRequest==null || pictureEditRequest.getId()<=0){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        User loginUser = userService.getLoginUser(request);

        pictureService.editPicture(pictureEditRequest,loginUser);
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

    /**
     * 图片审核(仅管理员可用)
     * @param pictureReviewRequest
     * @param request
     * @return
     */
   @PostMapping("/review")
   @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
   public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,HttpServletRequest request){
        ThrowUtils.throwIf(pictureReviewRequest==null,ErrorCode.PARAMS_ERROR);
        User loginUser=userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest,loginUser);
        return ResultUtils.success(true);

   }

    /**
     * 批量上传
     * @param pictureUploadByBatchRequest
     * @param request
     * @return
     */

    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,HttpServletRequest request){
        ThrowUtils.throwIf(pictureUploadByBatchRequest==null,ErrorCode.PARAMS_ERROR);
        User loginUser=userService.getLoginUser(request);
        int count=pictureService.uploadPictureByBatch(pictureUploadByBatchRequest,loginUser);
        return ResultUtils.success(count);

    }

    /**
     * 以图搜图
     * @param searchPictureByPictureRequest
     * @return
     */
    @PostMapping("/search/picture")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<ImageSearchResult>> searchPictureByPicture(@RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest){
        ThrowUtils.throwIf(searchPictureByPictureRequest==null,ErrorCode.PARAMS_ERROR);
        Long pictureId=searchPictureByPictureRequest.getPictureId();
        ThrowUtils.throwIf(pictureId==null||pictureId<=0,ErrorCode.PARAMS_ERROR);
        Picture picture=pictureService.getById(pictureId);
        if(picture==null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        List<ImageSearchResult> imageSearchResults = ImageSearchApiFacade.searchImage(picture.getUrl());
        return ResultUtils.success(imageSearchResults);
        }
    /**
     * 按颜色搜图
     * @param searchPictureByColorRequest
     * @return
     */
    @PostMapping("/search/color")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<PictureVo>> searchPictureByColor(@RequestBody SearchPictureByColorRequest searchPictureByColorRequest,HttpServletRequest request){
        ThrowUtils.throwIf(searchPictureByColorRequest==null,ErrorCode.PARAMS_ERROR);
        Long spaceId=searchPictureByColorRequest.getSpaceId();
        String picColor=searchPictureByColorRequest.getPicColor();
        User loginUser = userService.getLoginUser(request);
        List<PictureVo> pictureVoList = pictureService.searchPictureByColor(spaceId, picColor,loginUser);
        return ResultUtils.success(pictureVoList);
    }

    /**
     * 创建AI扩图任务
     *
     * @param
     * @param request
     * @return
     */
    @PostMapping("/out_painting/create_task")
    public BaseResponse<CreateOutPaintingTaskResponse> createPictureOutPaintingTask(@RequestBody CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest,
                                                                                    HttpServletRequest request) {
        if (createPictureOutPaintingTaskRequest == null || createPictureOutPaintingTaskRequest.getPictureId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        CreateOutPaintingTaskResponse response = pictureService.createPictureOutPaintingTask(createPictureOutPaintingTaskRequest, loginUser);
        return ResultUtils.success(response);
    }

    /**
     * 查询AI扩图任务
     *
     * @param taskId
     * @return
     */
    @GetMapping("/out_painting/get_task")
    public BaseResponse<GetOutPaintingTaskResponse> getPictureOutPaintingTaskStatus(String taskId) {
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR);
        GetOutPaintingTaskResponse outPaintingTask = aliYunAiApi.getOutPaintingTask(taskId);
        return ResultUtils.success(outPaintingTask);
    }
    }

