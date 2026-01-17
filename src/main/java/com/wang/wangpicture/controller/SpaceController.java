package com.wang.wangpicture.controller;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.wangpicture.annotation.AuthCheck;
import com.wang.wangpicture.common.BaseResponse;
import com.wang.wangpicture.common.DeleteRequest;
import com.wang.wangpicture.common.ResultUtils;
import com.wang.wangpicture.constant.UserConstant;
import com.wang.wangpicture.exception.BusinessException;
import com.wang.wangpicture.exception.ErrorCode;
import com.wang.wangpicture.exception.ThrowUtils;
import com.wang.wangpicture.model.dto.space.*;
import com.wang.wangpicture.model.entity.Space;
import com.wang.wangpicture.model.entity.User;
import com.wang.wangpicture.model.enums.SpaceLevelEnum;
import com.wang.wangpicture.model.vo.SpaceVo;
import com.wang.wangpicture.service.PictureService;
import com.wang.wangpicture.service.SpaceService;
import com.wang.wangpicture.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping("/space")
public class SpaceController {

    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;
    @Resource
    private SpaceService spaceService;

    /**
     * 更新空间
     * @param spaceAddRequest
     * @return
     */
    @PostMapping("/add")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request){
        if(spaceAddRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断用户是否可以创建空间即是否已经拥有一个空间，如果有就拒绝
        User loginUser=userService.getLoginUser(request);
        long addSpace = spaceService.addSpace(spaceAddRequest, loginUser);
        return ResultUtils.success(addSpace);
    }
    /**
     * 删除空间
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest,HttpServletRequest request){
        if(deleteRequest==null || deleteRequest.getId()<=0 ){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"空间不存在");
        }
        User loginUser=userService.getLoginUser(request);
        Long id=deleteRequest.getId();
        //判断是否存在
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace==null,ErrorCode.NOT_FOUND_ERROR);
        //仅本人或者管理员可以删除
        //仅本人或管理员可编辑
        if(!oldSpace.getId().equals(loginUser.getId())&&!userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //操作数据库
        boolean result = spaceService.removeById(id);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新空间
     * @param spaceUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest, HttpServletRequest request){
        if(spaceUpdateRequest==null || spaceUpdateRequest.getId()<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //将实体类和DTO进行转换
        Space space=new Space();
        BeanUtils.copyProperties(spaceUpdateRequest,space);
        //自动填充数据
        spaceService.fillSpaceBySpaceLevel(space);
        //数据校验
        spaceService.validSpace(space,false);
        //判断是否存在
        Long id=spaceUpdateRequest.getId();
        Space oldSpace=spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace==null,ErrorCode.NOT_FOUND_ERROR);
        User loginUser=userService.getLoginUser(request);
        //操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据id获取空间(仅管理员可用)
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSpaceById(long id,HttpServletRequest request){
        ThrowUtils.throwIf(id<=0,ErrorCode.PARAMS_ERROR);
        //查询数据库
        Space space=spaceService.getById(id);
        ThrowUtils.throwIf(space==null,ErrorCode.NOT_FOUND_ERROR);
        //获取封装类
        return ResultUtils.success(space);
    }
    @GetMapping("/get/vo")
    public BaseResponse<SpaceVo> getSpaceVoById(long id, HttpServletRequest request){
        ThrowUtils.throwIf(id<=0,ErrorCode.PARAMS_ERROR);
        //查询数据库
        Space space=spaceService.getById(id);
        ThrowUtils.throwIf(space==null,ErrorCode.NOT_FOUND_ERROR);

        //获取封装类
        return ResultUtils.success(spaceService.getSpaceVo(space,request));
    }

    /**
     * 分页获取空间列表（给普通用户使用)
     * @param spaceQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVo>> listSpaceVoByPage(@RequestBody SpaceQueryRequest spaceQueryRequest,
                                                            HttpServletRequest request){
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        //限制爬虫
        ThrowUtils.throwIf(size>20,ErrorCode.PARAMS_ERROR);
        //查询数据库
        Page<Space> spacePage=spaceService.page(new Page<>(current,size),
                spaceService.getQueryWrapper(spaceQueryRequest));
        //获取封装类
        return ResultUtils.success(spaceService.getSpaceVoPage(spacePage,request));
    }
    /**
     * 分页获取空间列表(仅管理员可用)
     * @param spaceQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest){
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        //查询数据库
        Page<Space> spacePage=spaceService.page(new Page<>(current,size),
                spaceService.getQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(spacePage);
    }

    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request){
        if(spaceEditRequest==null || spaceEditRequest.getId()<=0){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        //在此处将实体类和DTO进行转换
        Space space=new Space();
        BeanUtils.copyProperties(spaceEditRequest,space);
        //自动填充数据
        spaceService.fillSpaceBySpaceLevel(space);
        //设置编辑时间
        space.setEditTime(new Date());
        //数据校验
        spaceService.validSpace(space,false);
        User loginUser=userService.getLoginUser(request);
        //判断是否存在
        long id=spaceEditRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace==null,ErrorCode.NOT_FOUND_ERROR);
        //仅本人或管理员可编辑
        if(!oldSpace.getId().equals(loginUser.getId())&&!userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);

    }

    /**
     * 获取所有的空间级别，便于前端展示
     * @return
     */
    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel(){
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum -> new SpaceLevel(
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getText(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize()
                )).collect(Collectors.toList());
        return ResultUtils.success(spaceLevelList);
    }


}
