package com.wang.wangpicture.model.dto.picture;

import com.wang.wangpicture.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 图片审核请求
 */
@Data
public class PictureReviewRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = 7296246433949099735L;
    /**
     * id
     */
    private Long id;

    /**
     * 审核状态 0-待审核 1-通过;2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;



    public Integer getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(Integer reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getReviewMessage() {
        return reviewMessage;
    }

    public void setReviewMessage(String reviewMessage) {
        this.reviewMessage = reviewMessage;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }




}
