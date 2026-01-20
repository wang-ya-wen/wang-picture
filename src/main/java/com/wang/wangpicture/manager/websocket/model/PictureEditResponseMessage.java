package com.wang.wangpicture.manager.websocket.model;

import com.wang.wangpicture.model.vo.UserVo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图片编辑响应消息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PictureEditResponseMessage {
    /**
     * 消息类型，例如”INFO“、”ERROR"、“ENTER_EDIT”
     */
    private String type;
    /**
     * 信息
     */
    private String message;
    /**
     * 执行的编辑动作
     */
    private String editAction;
    /**
     * 用户信息
     */
    private UserVo userVo;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEditAction() {
        return editAction;
    }

    public void setEditAction(String editAction) {
        this.editAction = editAction;
    }

    public UserVo getUserVo() {
        return userVo;
    }

    public void setUserVo(UserVo userVo) {
        this.userVo = userVo;
    }
}
