package com.wang.wangpicture.model.vo.space.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 空间用户上传行为分析
 */

@NoArgsConstructor
@Data
public class SpaceUserAnalyzeResponse implements Serializable {
    /**
     * 时间区间
     */
    private String period;
    /**
     * 上传数量
     */
    private Long count;

    private static final long serialVersionUID = 1L;

    public SpaceUserAnalyzeResponse(String period, Long count) {
        this.period = period;
        this.count = count;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
