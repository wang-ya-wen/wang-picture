package com.wang.wangpicture.model.dto.space.analyze;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 空间用户上传行为分析
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceUserAnalyzeRequest extends SpaceAnalyzeRequest {

    /**
     * 用户id
     */
    private Long userId;
    /**
     * 时间维度：day/month/year
     */
    private String timeDimension;

    public SpaceUserAnalyzeRequest(Long userId, String timeDimension) {
        this.userId = userId;
        this.timeDimension = timeDimension;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTimeDimension() {
        return timeDimension;
    }

    public void setTimeDimension(String timeDimension) {
        this.timeDimension = timeDimension;
    }
}
