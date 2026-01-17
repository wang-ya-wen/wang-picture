package com.wang.wangpicture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wang.wangpicture.model.dto.space.SpaceAddRequest;
import com.wang.wangpicture.model.dto.space.SpaceQueryRequest;
import com.wang.wangpicture.model.dto.space.analyze.SpaceCategoryAnalyzeRequest;
import com.wang.wangpicture.model.dto.space.analyze.SpaceSizeAnalyzeRequest;
import com.wang.wangpicture.model.dto.space.analyze.SpaceTagAnalyzeRequest;
import com.wang.wangpicture.model.dto.space.analyze.SpaceUsageAnalyzeRequest;
import com.wang.wangpicture.model.entity.Space;
import com.wang.wangpicture.model.entity.User;
import com.wang.wangpicture.model.vo.SpaceVo;
import com.wang.wangpicture.model.vo.space.analyze.SpaceCategoryAnalyzeResponse;
import com.wang.wangpicture.model.vo.space.analyze.SpaceSizeAnalyzeResponse;
import com.wang.wangpicture.model.vo.space.analyze.SpaceUsageAnalyzeResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 *
 */
public interface SpaceAnalyzeService extends IService<Space> {
    /**
     * 获取空间使用情况分析
     *
     * @param spaceAnalyzeRequest
     * @param loginUser
     * @return
     */
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceAnalyzeRequest, User loginUser);

    /**
     * 获取空间图片分类分析
     *
     * @param spaceCategoryAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);

        /**
     * 获取空间标签分析
     *
     * @param spaceTagAnalyzeRequest
     * @param loginUser
     * @return
     */
        List<SpaceCategoryAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

    /**
     * 获取空间图片大小分析
      */
    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);


}
