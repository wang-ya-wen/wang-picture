package com.wang.wangpicture.manager.websocket.disruptor;

import com.wang.wangpicture.manager.websocket.model.PictureEditRequestMessage;
import com.wang.wangpicture.model.entity.User;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

/**
 * 图片编辑事件
 */
@Data
public class PictureEditEvent {
    /**
     * 消息
     */
    private PictureEditRequestMessage pictureEditRequestMessage;
    /**
     * 当前用户的session
     */
    private WebSocketSession session;
    /**
     * 当前用户
     */
    private User user;
    /**
     * 图片id
     */
    private Long pictureId;

    public PictureEditRequestMessage getPictureEditRequestMessage() {
        return pictureEditRequestMessage;
    }

    public void setPictureEditRequestMessage(PictureEditRequestMessage pictureEditRequestMessage) {
        this.pictureEditRequestMessage = pictureEditRequestMessage;
    }

    public WebSocketSession getSession() {
        return session;
    }

    public void setSession(WebSocketSession session) {
        this.session = session;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getPictureId() {
        return pictureId;
    }

    public void setPictureId(Long pictureId) {
        this.pictureId = pictureId;
    }
}
