package com.wang.wangpicture.model.dto.picture;

import com.wang.wangpicture.api.aliyun.model.CreateOutPaintingTaskRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建扩图任务请求
 */
@Data
public class CreatePictureOutPaintingTaskRequest implements Serializable {

    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 扩图参数
     */
    private CreateOutPaintingTaskRequest.Parameters parameters;

    private static final long serialVersionUID = 1L;

    public Long getPictureId() {
        return pictureId;
    }

    public void setPictureId(Long pictureId) {
        this.pictureId = pictureId;
    }

    public CreateOutPaintingTaskRequest.Parameters getParameters() {
        return parameters;
    }

    public void setParameters(CreateOutPaintingTaskRequest.Parameters parameters) {
        this.parameters = parameters;
    }
}