package com.wang.wangpicture.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.Objects;

/**
 * 空间级别枚举
 */
@Getter
public enum SpaceLevelEnum {
    COMMON(0, "普通版", 100, 100L * 1024 * 1024),
    PROFESSIONAL(1, "专业版", 1000, 1000L * 1024 * 1024),
    FLAGSHIP(2, "旗舰版", 10000, 10000L * 1024 * 1024);


    private final int value;
    private final String text;
    private final long maxCount;
    private final long maxSize;

    SpaceLevelEnum(int value, String text, long maxCount, long maxSize) {
        this.value = value;
        this.text = text;
        this.maxCount = maxCount;
        this.maxSize = maxSize;
    }

    public static SpaceLevelEnum getEnumByValue(Integer value) {
        if(ObjUtil.isEmpty(value)){
            return null;
        }
        for(SpaceLevelEnum spaceLevelEnum:SpaceLevelEnum.values()){
            if(Objects.equals(spaceLevelEnum.value, value)){
                return spaceLevelEnum;
            }
        }
        return null;
    }

    public String getText() {
        return text;
    }

    public int getValue() {
        return value;
    }

    public long getMaxCount() {
        return maxCount;
    }

    public long getMaxSize() {
        return maxSize;
    }
}
