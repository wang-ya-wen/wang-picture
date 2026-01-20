package com.wang.wangpicture.manager.websocket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.wang.wangpicture.manager.websocket.disruptor.PictureEditEvent;
import com.wang.wangpicture.manager.websocket.disruptor.PictureEditEventProducer;
import com.wang.wangpicture.manager.websocket.model.PictureEditActionEnum;
import com.wang.wangpicture.manager.websocket.model.PictureEditMessageTypeEnum;
import com.wang.wangpicture.manager.websocket.model.PictureEditRequestMessage;
import com.wang.wangpicture.manager.websocket.model.PictureEditResponseMessage;
import com.wang.wangpicture.model.entity.User;
import com.wang.wangpicture.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 图片编辑处理器
 */
@Component
public class PictureEditHandler extends TextWebSocketHandler {
    //每张图片的编辑状态key:pictureId,value:当前正在编辑的用户ID
    private final Map<Long, Long> pictureEditingUsers = new ConcurrentHashMap<>();
    //保存所有连接的会话key:pictureId,value:用户会话集合
    private final Map<Long, Set<WebSocketSession>> pictureSessions = new ConcurrentHashMap<>();
    @Resource
    private UserService userService;
    @Resource
    @Lazy
    private PictureEditEventProducer pictureEditEventProducer;

    public PictureEditHandler(UserService userService) {
        this.userService = userService;
    }

    /**
     * 连接建立成功
     *
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        //保存会话到集合中
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        pictureSessions.putIfAbsent(pictureId, ConcurrentHashMap.newKeySet());
        pictureSessions.get(pictureId).add(session);
        //构造响应，发送骄傲如编辑的消息通知
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String message = String.format("用户%s加入了图片编辑", user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUserVo(userService.getUserVO(user));
        //广播给所有用户
        broadcastToPicture(pictureId, pictureEditResponseMessage);

    }

    /**
     * 收到前端发送的消息，根据消息类别处理消息
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        //获取其消息内容，将JSON转换为PictureEditMessage
        PictureEditRequestMessage pictureEditRequestMessage = JSONUtil.toBean(message.getPayload(), PictureEditRequestMessage.class);
        //从Session属性中获取到公共参数
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        //生产消息到disruptor环形队列中
        pictureEditEventProducer.publishEvent(pictureEditRequestMessage, session, user, pictureId);
//        //根据消息类型处理消息替换为上面的一行代码即可完成
//        switch (pictureEditMessageTypeEnum) {
//
//            case ENTER_EDIT:
//                handleEnterEditMessage(pictureEditRequestMessage, session, user, pictureId);
//                break;
//            case EXIT_EDIT:
//                handleExitEditMessage(pictureEditRequestMessage, session, user, pictureId);
//                break;
//            case EDIT_ACTION:
//                handleEditActionMessage(pictureEditRequestMessage, session, user, pictureId);
//                break;
//            default:
//                //其他消息类型，返回错误提示
//                PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
//                pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ERROR.getValue());
//                pictureEditResponseMessage.setMessage("未知消息类型");
//                pictureEditResponseMessage.setUserVo(userService.getUserVO(user));
//                session.sendMessage(new TextMessage(JSONUtil.toJsonStr(pictureEditResponseMessage)));
//                break;
//
//        }

    }

    /**
     * 进入编辑状态
     * @param pictureEditRequestMessage
     * @param session
     * @param user
     * @param pictureId
     */
    public void handleEnterEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws IOException {
         //没有用户正在编辑才可以进入编辑
        if(!pictureEditingUsers.containsKey(pictureId)){
            //设置用户正在编辑该图片
            pictureEditingUsers.put(pictureId,user.getId());
            //构造响应，发送正在编辑的消息通知
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ENTER_EDIT.getValue());
            String message = String.format("用户%s正在进行编辑", user.getUserName());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUserVo(userService.getUserVO(user));
            //广播给所有用户
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }
    }

    /**
     * 编辑操作
     * @param pictureEditRequestMessage
     * @param session
     * @param user
     * @param pictureId
     */
    public void handleEditActionMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws IOException {
        Long editingUserId = pictureEditingUsers.get(pictureId);
        String editAction = pictureEditRequestMessage.getEditAction();
        PictureEditMessageTypeEnum actionEnum = PictureEditMessageTypeEnum.getEnumByValue(editAction);
        if(actionEnum==null){
            System.out.println("无效的编辑动作");
            return;
        }
        //确认是当前的编辑者
        if(editingUserId!=null&&editingUserId.equals(user.getId())){
            //构造响应
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getValue());
            String message=String.format("%s正在对图片进行%s操作",user.getUserName(),actionEnum.getText());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUserVo(userService.getUserVO(user));
            //广播给除了当前客户端之外的其他用户，否则会造成错误，使得当前的用户再次进行操作，就错误了
            broadcastToPicture(pictureId, pictureEditResponseMessage,session);

        }
    }

    /**
     * 退出编辑
     * @param pictureEditRequestMessage
     * @param session
     * @param user
     * @param pictureId
     */
    public void handleExitEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws IOException {
        Long editingUserId = pictureEditingUsers.get(pictureId);
        //确认是当前的编辑者
        if(editingUserId!=null&&editingUserId.equals(user.getId())){
            //移除用户正在编辑图片
            pictureEditingUsers.remove(pictureId);
            //构造响应
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
            String message=String.format("%s退出了编辑",user.getUserName());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUserVo(userService.getUserVO(user));
            //广播给除了当前客户端之外的其他用户，否则会造成错误，使得当前的用户再次进行操作，就错误了
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }
    }


    /**
     * 关闭连接
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        //从Session属性中获取到公共参数
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        //移除当前用户的编辑状态
        handleExitEditMessage(null,session,user,pictureId);
        //删除会话
        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);
        if(sessionSet!=null){
            sessionSet.remove(session);
            if(sessionSet.isEmpty()){
                pictureSessions.remove(pictureId);
            }
        }
        //构造响应，通知其他用户
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String message=String.format("%s离开编辑了",user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUserVo(userService.getUserVO(user));
        broadcastToPicture(pictureId, pictureEditResponseMessage);

    }

    /**
     * 广播给该图片的所有用户(支持排除掉某个session)
     *
     * @param pictureId
     * @param pictureEditResponseMessage
     * @param excludeSession             被排除掉的session 把自身排除，防止将自己的消息再发给自己
     * @throws IOException
     */
    //编写广播方法
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage, WebSocketSession excludeSession) throws IOException {
        //获取当前所有图片的会话集合
        Set<WebSocketSession> webSocketSessions = pictureSessions.get(pictureId);
        if (CollUtil.isNotEmpty(webSocketSessions)) {
            //创建ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();
            //配置序列化将Long转为String类型
            SimpleModule simpleModule = new SimpleModule();
            // 注册 Long 类型序列化器：Long -> String
            simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
            simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
            objectMapper.registerModule(simpleModule);
            //序列化为Json字符串
            String str = objectMapper.writeValueAsString(pictureEditResponseMessage);
            TextMessage textMessage = new TextMessage(str);
            for (WebSocketSession session : webSocketSessions) {
                //排除掉的session不发送
                if (excludeSession != null && session.equals(excludeSession)) {
                    continue;
                }
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
        }
    }

    /**
     * 广播给该图片的所有用户
     *
     * @param pictureId
     * @param pictureEditResponseMessage
     * @throws IOException
     */
    //编写广播方法
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage) throws IOException {
        broadcastToPicture(pictureId, pictureEditResponseMessage, null);
    }
}
