package com.wang.wangpicture.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wang.wangpicture.exception.BusinessException;
import com.wang.wangpicture.exception.ErrorCode;
import com.wang.wangpicture.exception.ThrowUtils;
import com.wang.wangpicture.mapper.SpaceMapper;
import com.wang.wangpicture.model.dto.space.analyze.*;
import com.wang.wangpicture.model.entity.Picture;
import com.wang.wangpicture.model.entity.Space;
import com.wang.wangpicture.model.entity.User;
import com.wang.wangpicture.model.vo.space.analyze.SpaceCategoryAnalyzeResponse;
import com.wang.wangpicture.model.vo.space.analyze.SpaceSizeAnalyzeResponse;
import com.wang.wangpicture.model.vo.space.analyze.SpaceUsageAnalyzeResponse;
import com.wang.wangpicture.model.vo.space.analyze.SpaceUserAnalyzeResponse;
import com.wang.wangpicture.service.PictureService;
import com.wang.wangpicture.service.SpaceAnalyzeService;
import com.wang.wangpicture.service.SpaceService;
import com.wang.wangpicture.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.builder.BuilderException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 空间分析实现
 */
@Service
@Slf4j
public class SpaceAnalyzeServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceAnalyzeService {
    @Resource
    private UserService userService;
    @Resource
    private SpaceService spaceService;
    @Resource
    private PictureService pictureService;

    @Override
    public SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceAnalyzeRequest, User loginUser) {
        //校验参数
        ThrowUtils.throwIf(spaceAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        //全空间或公共图库需要从Picture表查询
        if (spaceAnalyzeRequest.isQueryPublic() || spaceAnalyzeRequest.isQueryAll()) {
            //校验权限,仅管理员可以使用
            checkSpaceAnalyzeAuth(spaceAnalyzeRequest, loginUser);
            //统计图库的使用空间
            QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("picSize");
            //补充查询范围
            fillAnalyzeQueryWrapper(spaceAnalyzeRequest, queryWrapper);
            List<Object> objects = pictureService.getBaseMapper().selectObjs(queryWrapper);
            long usedSize = objects.stream().mapToLong(obj -> (Long) obj).sum();
            long usedCount = objects.stream().count();
            //封装返回对象
            SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
            spaceUsageAnalyzeResponse.setUsedSize(usedSize);
            //公共空间没有容量限制
            spaceUsageAnalyzeResponse.setMaxSize(null);
            spaceUsageAnalyzeResponse.setSizeUsageRatio(null);
            spaceUsageAnalyzeResponse.setUsedCount(usedCount);
            spaceUsageAnalyzeResponse.setMaxCount(null);
            spaceUsageAnalyzeResponse.setCountUsageRatio(null);
            return spaceUsageAnalyzeResponse;
        } else {
            //特定空间只需要在空间内分析
            Long spaceId = spaceAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR);
            //获取空间信息
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            //校验权限
            checkSpaceAnalyzeAuth(spaceAnalyzeRequest, loginUser);
            //封装返回结果
            Long maxSize = space.getMaxSize();
            Long maxCount = space.getMaxCount();
            Long totalSize = space.getTotalSize();
            Long totalCount = space.getTotalCount();
            SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
            //计算使用比例
            double sizeUsageRatio = NumberUtil.round(totalSize * 100.0 / maxSize, 2).doubleValue();
            double countRatio = NumberUtil.round(totalCount * 100.0 / maxCount, 2).doubleValue();
            spaceUsageAnalyzeResponse.setUsedSize(totalSize);
            spaceUsageAnalyzeResponse.setMaxSize(maxSize);
            spaceUsageAnalyzeResponse.setSizeUsageRatio(sizeUsageRatio);
            spaceUsageAnalyzeResponse.setUsedCount(totalCount);
            spaceUsageAnalyzeResponse.setMaxCount(maxCount);
            spaceUsageAnalyzeResponse.setCountUsageRatio(countRatio);
            return spaceUsageAnalyzeResponse;


        }


    }

    @Override
    public List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        //校验权限
        checkSpaceAnalyzeAuth(spaceCategoryAnalyzeRequest, loginUser);
        Space space = spaceService.getById(loginUser.getId());
        if (space == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }
        QueryWrapper<Picture> queryWrapper=new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceCategoryAnalyzeRequest,queryWrapper);
        //使用MybatisPlus的分组查询
        queryWrapper.select("category","count(*) as count","sum(picSize) as totalSize")
                .groupBy("category");
        //查询并转换结果
        return pictureService.getBaseMapper().selectMaps(queryWrapper)
                .stream()
                .map(result -> {
                    String category = (String) result.get("category");
                    Long count = ((Number) result.get("count")).longValue();
                    Long totalSize = ((Number) result.get("totalSize")).longValue();
                    return new SpaceCategoryAnalyzeResponse(count, category, totalSize);
                })
                .collect(Collectors.toList());


    }

    @Override
    public List<SpaceCategoryAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        //校验权限
        checkSpaceAnalyzeAuth(spaceTagAnalyzeRequest, loginUser);
        QueryWrapper<Picture> queryWrapper=new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceTagAnalyzeRequest,queryWrapper);
        //查询所有符合条件的标签
        queryWrapper.select("tags");
        List<Object> tagsJsonList= pictureService.getBaseMapper().selectObjs(queryWrapper)
                .stream()
                .filter(ObjUtil::isNotNull)
                .map(Object::toString)
                .collect(Collectors.toList());
        //解析标签并统计
        Map<String, Long> tagCountMap = tagsJsonList.stream()
                .flatMap(tagJson -> JSONUtil.toList(tagJson.toString(),String.class).stream())
        .collect(Collectors.groupingBy(tag->tag, Collectors.counting()));
        //封装返回结果
        return tagCountMap.entrySet().stream()
                .sorted((e1,e2)->Long.compare(e2.getValue(),e1.getValue()))
                .map(entry -> new SpaceCategoryAnalyzeResponse(entry.getValue(), entry.getKey(), null))
                .collect(Collectors.toList());

    }

    @Override
    public List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        //校验权限
        checkSpaceAnalyzeAuth(spaceSizeAnalyzeRequest, loginUser);
        QueryWrapper<Picture> queryWrapper=new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceSizeAnalyzeRequest,queryWrapper);
        //查询所有符合条件的图片大小
        queryWrapper.select("picSize");
        List<Long> picSizeList=pictureService.getBaseMapper().selectObjs(queryWrapper)
                .stream()
                .filter(ObjUtil::isNotNull)
                .map(size->(Long) size)
                .collect(Collectors.toList());
        //定义分段范围
        // 定义分段范围，注意使用有序的 Map
        Map<String, Long> sizeRanges = new LinkedHashMap<>();
        sizeRanges.put("<100KB", picSizeList.stream().filter(size -> size < 100 * 1024).count());
        sizeRanges.put("100KB-500KB", picSizeList.stream().filter(size -> size >= 100 * 1024 && size < 500 * 1024).count());
        sizeRanges.put("500KB-1MB", picSizeList.stream().filter(size -> size >= 500 * 1024 && size < 1 * 1024 * 1024).count());
        sizeRanges.put(">1MB", picSizeList.stream().filter(size -> size >= 1 * 1024 * 1024).count());

        // 转换为响应对象
        return sizeRanges.entrySet().stream()
                .map(entry -> new SpaceSizeAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());


    }

    @Override
    public List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        //校验权限
        checkSpaceAnalyzeAuth(spaceUserAnalyzeRequest, loginUser);
        QueryWrapper<Picture> queryWrapper=new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceUserAnalyzeRequest,queryWrapper);
        //补充用户id查询
        Long userId = spaceUserAnalyzeRequest.getUserId();
        queryWrapper.eq(ObjUtil.isNotNull(userId),"userId",loginUser.getId());
        //补充分析维度：每日、每周、每月
        String timeDimension = spaceUserAnalyzeRequest.getTimeDimension();
        switch(timeDimension){
            case "day":
                queryWrapper.select("DATE_FORMAT(createTime,'%Y-%m-%d') as period", "count(*) as count");
                queryWrapper.groupBy("date");
                break;
            case "week":
                queryWrapper.select("YEARWEEK(createTime) as period", "count(*) as count");
                queryWrapper.groupBy("date");
                break;
            case "month":
                queryWrapper.select("DATE_FORMAT(createTime,'%Y-%m') as period", "count(*) as count");
                queryWrapper.groupBy("date");
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的查询维度");
        }
        //分组排序
        queryWrapper.groupBy("period").orderByAsc("period");
        //查询并封装结果//查询并转换结果
        return pictureService.getBaseMapper().selectMaps(queryWrapper)
                .stream()
                .map(result->{
                    String period=(String) result.get("period");
                    Long count=(Long) result.get("count");
                    return new SpaceUserAnalyzeResponse(period,count);
                }).collect(Collectors.toList());
    }

    @Override
    public List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        //校验权限，仅管理员可以查看
        ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);
        //查询所有空间//查询空间列表
        QueryWrapper<Space> queryWrapper=new QueryWrapper<>();
        queryWrapper.select("id","spaceName","userId","totalSize");
        queryWrapper.orderByDesc("totalSize");
        queryWrapper.last("limit "+spaceRankAnalyzeRequest.getTopN());
        List<Space> spaceList=spaceService.list(queryWrapper);
        return spaceList;

    }


    /**
     * 校验分析空间的权限
     *
     * @param spaceAnalyzeRequest
     * @param loginUser
     */
    private void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser) {
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
        boolean queryAll = spaceAnalyzeRequest.isQueryAll();
        //全空间分析或公共图库分析仅管理员可以访问
        if (queryAll || queryPublic) {
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);
        } else {
            //分析特定空间
            ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            spaceService.checkSpaceAuth(space, loginUser);

        }

    }

    /**
     * 根据请求对象封装查询参数
     *
     * @param spaceAnalyzeRequest
     * @param queryWrapper
     */
    private void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper) {
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
        boolean queryAll = spaceAnalyzeRequest.isQueryAll();
        //全空间分析
        if (queryAll) {
            return;
        }
        if (queryPublic) {
            queryWrapper.isNull("spaceId");
            return;
        }
        //分析特定空间
        if (spaceId != null) {
            queryWrapper.eq("spaceId", spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定查询范围");
    }


}




