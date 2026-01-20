package com.wang.wangpicture.manager.websocket;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.wang.wangpicture.manager.auth.SpaceUserAuthManager;
import com.wang.wangpicture.model.entity.Picture;
import com.wang.wangpicture.model.entity.Space;
import com.wang.wangpicture.model.entity.User;
import com.wang.wangpicture.model.enums.SpaceTypeEnum;
import com.wang.wangpicture.service.PictureService;
import com.wang.wangpicture.service.SpaceService;
import com.wang.wangpicture.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.info.ProjectInfoProperties;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * WebSocket拦截器，建立连接前要先校验
 */
@Component
public class WsHandshakeInterceptor implements HandshakeInterceptor {

    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;
    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;
    @Resource
    private SpaceService spaceService;
    @Autowired
    private ProjectInfoProperties projectInfoProperties;

    /**
     * 简历连接前要先校验
     *
     * @param request
     * @param response
     * @param wsHandler
     * @param attributes 给WebSocketSession会话设置属性
     * @return
     * @throws Exception
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest httpServletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            //从请求中获取参数
            String pictureId = httpServletRequest.getParameter("pictureId");
            if (StrUtil.isBlank(pictureId)) {
                System.out.println("缺少图片参数，拒绝握手");
                return false;
            }
            //获取当前登录用户
            User loginUser = userService.getLoginUser(httpServletRequest);
            if (ObjectUtil.isEmpty(loginUser)) {
                System.out.println("未登录，拒绝握手");
                return false;
            }
            //校验图片
            Picture picture = pictureService.getById(pictureId);
            if (ObjectUtil.isEmpty(picture)) {
                System.out.println("图片不存在，拒绝握手");
                return false;
            }
            Long spaceId = picture.getSpaceId();
            Space space = null;
            if (spaceId != null) {
                space = spaceService.getById(spaceId);
                if (ObjectUtil.isEmpty(space)) {
                    System.out.println("空间不存在，拒绝握手");
                    return false;
                }
                if (space.getSpaceType() != SpaceTypeEnum.TEAM.getValue()) {
                    System.out.println("不是团队空间，拒绝握手");
                    return false;
                }


            }
            List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
            if (!permissionList.contains("edit")) {
                System.out.println("没有编辑图片的权限，拒绝握手");
                return false;
            }
            //设置用户登录信息属性到WebSocket会话中
            attributes.put("loginUser", loginUser);
            attributes.put("userId", loginUser.getId());
            attributes.put("pictureId", Long.valueOf(pictureId));//记得转换为long类型
        }

        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
