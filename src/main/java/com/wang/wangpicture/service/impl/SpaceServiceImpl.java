package com.wang.wangpicture.service.impl;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wang.wangpicture.exception.BusinessException;
import com.wang.wangpicture.exception.ErrorCode;
import com.wang.wangpicture.exception.ThrowUtils;
import com.wang.wangpicture.mapper.SpaceMapper;
import com.wang.wangpicture.model.dto.space.SpaceAddRequest;
import com.wang.wangpicture.model.dto.space.SpaceQueryRequest;
import com.wang.wangpicture.model.entity.Space;
import com.wang.wangpicture.model.entity.User;
import com.wang.wangpicture.model.enums.SpaceLevelEnum;
import com.wang.wangpicture.model.enums.SpaceTypeEnum;
import com.wang.wangpicture.model.vo.SpaceVo;
import com.wang.wangpicture.model.vo.UserVo;
import com.wang.wangpicture.service.SpaceService;
import com.wang.wangpicture.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.builder.BuilderException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *空间实现
 */
@Service
@Slf4j
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {
    @Resource
    private UserService userService;
    @Resource
    private TransactionTemplate transactionTemplate;
    /**
     * 创建空间
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        //1.校验参数填充默认值
        Space space=new Space();
        BeanUtils.copyProperties(spaceAddRequest,space);
        if(StrUtil.isBlank(space.getSpaceName())){
            space.setSpaceName("默认空间");
        }
        if(space.getSpaceLevel()==null){
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());

        }
        if(space.getSpaceType()==null){
            space.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        }
        //填充容量和大小
        this.fillSpaceBySpaceLevel(space);
        //2.校验参数
        this.validSpace(space,true);
        //3.校验权限，非管理员只能创建普通级别的空间
        Long userId = loginUser.getId();
        space.setUserId(userId);
        if(SpaceLevelEnum.COMMON.getValue()!=space.getSpaceLevel()&&!userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"无权限创建指定级别的空间");
        }
        //4.控制同一用户只能创建一个私有空间,以及一个团队空间
        String lock=String.valueOf(userId).intern();
        synchronized (lock){
            Long newSpaceId = transactionTemplate.execute(status -> {
                        //判断是否已有空间，如果已有不能创建
                        boolean exists = this.lambdaQuery()
                                .eq(Space::getUserId, userId)
                                .eq(Space::getSpaceType,space.getSpaceType())
                                .exists();
                        ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户每类空间只能创建一个");
                        //创建
                        boolean save = this.save(space);
                        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "保存到数据库失败");
                        //返回写入数据库的数据id
                        return space.getId();
                    }
            );
            //为了不让newSpaceId报黄，使用options包裹
            return Optional.ofNullable(newSpaceId).orElse(-1L);

        }

    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper=new QueryWrapper<>();
        if(spaceQueryRequest==null){
            return queryWrapper;
        }
        //从对象中取值
        Long id = spaceQueryRequest.getId();
        String userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();
        Integer spaceType=spaceQueryRequest.getSpaceType();
        queryWrapper.eq(ObjUtil.isNotEmpty(id),"id",id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId),"userId",userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceName),"spaceName",spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel),"spaceLevel",spaceLevel);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceType),"spaceType",spaceType);
        //排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField),sortOrder.equals("ascend"),sortField);
        return queryWrapper;
    }

    @Override
    public SpaceVo getSpaceVo(Space space, HttpServletRequest request) {
        //对象转封装类
        SpaceVo spaceVo=SpaceVo.objToVo(space);
        //关联查询用户信息
        Long userId=space.getUserId();
        if(userId!=null&&userId>0){
            User user=userService.getById(userId);
            UserVo userVo=userService.getUserVO(user);
            spaceVo.setUser(userVo);
        }
        return spaceVo;
    }

    @Override
    public Page<SpaceVo> getSpaceVoPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList=spacePage.getRecords();
        Page<SpaceVo> spaceVoPage=new Page<>(spacePage.getCurrent(),spacePage.getSize(),spacePage.getTotal());
        if(CollUtil.isEmpty(spaceList)){
            return spaceVoPage;
        }
        //对象列表=>封装对象列表
        List<SpaceVo> spaceVoList=spaceList.stream().map(SpaceVo::objToVo).collect(Collectors.toList());
        //1.关联查询用户信息
        Set<Long> userIdSet=spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIduSERListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        //2.填充信息
        spaceVoList.forEach(spaceVo -> {
            Long userId = spaceVo.getUserId();
            User user=null;
            if(userIduSERListMap.containsKey(userId)){
                user=userIduSERListMap.get(userId).get(0);
            }
            spaceVo.setUser(userService.getUserVO(user));
        });
        spaceVoPage.setRecords(spaceVoList);
        return spaceVoPage;

    }

    @Override
    public void validSpace(Space space,boolean add) {
        ThrowUtils.throwIf(space==null, ErrorCode.PARAMS_ERROR);
        //从对象中取值
        String spaceName=space.getSpaceName();
        Integer spaceLevel=space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum=SpaceLevelEnum.getEnumByValue(spaceLevel);
        Integer spaceType = space.getSpaceType();
        SpaceTypeEnum spaceTypeEnum= SpaceTypeEnum.getEnumByValue(spaceType);
        //创建时校验
        if(add){
            if(StrUtil.isBlank(spaceName)){
                throw  new BusinessException(ErrorCode.PARAMS_ERROR,"空间名称不能为空");
            }
            if(spaceLevel==null) {
                throw  new BusinessException(ErrorCode.PARAMS_ERROR,"空间级别不能为空");
            }
            if(spaceType==null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"空间类型不能为空");
            }
        }
        if(StrUtil.isNotBlank(spaceName)&&spaceName.length()>30){
            ThrowUtils.throwIf(spaceName==null,ErrorCode.PARAMS_ERROR,"空间名称不能过长");
        }

        if(spaceLevel!=null && spaceLevelEnum==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"空间级别不存在");

             }
        //修改数据时，空间级别校验
        if(spaceType!=null&&spaceTypeEnum==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"空间类型不存在");
        }

    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if(spaceLevelEnum!=null){
            Long maxSize = space.getMaxSize();
            if(space.getMaxSize()==null){
                space.setMaxSize(maxSize);
            }
            Long maxCount = space.getMaxCount();
            if(space.getMaxCount()==null){
                space.setMaxCount(maxCount);
            }
        }
    }

    @Override
    public void checkSpaceAuth(Space space, User loginUser) {
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        Long spaceUserId = space.getUserId();
        Long loginUserId = loginUser.getId();
        if (!loginUserId.equals(spaceUserId)) {
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);
        }

    }
}




