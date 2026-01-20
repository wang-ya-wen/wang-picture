package com.wang.wangpicture.manager.websocket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图片编辑请求消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PictureEditRequestMessage {
    /**
     * 消息类型：例如"Enter_edit"，“EXIT_EDIT"，”EDIT_ACTION“
     */
    private String type;
    /**
     * 执行的编辑动作（放大、缩小)
     */
    private String editAction;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEditAction() {
        return editAction;
    }

    public void setEditAction(String editAction) {
        this.editAction = editAction;
    }
}
