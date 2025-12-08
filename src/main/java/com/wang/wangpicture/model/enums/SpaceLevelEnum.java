package com.wang.wangpicture.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.Objects;

/**
 * 空间级别枚举
 */
@Getter
public enum SpaceLevelEnum {
    COMMON("普通版",0,100,100L*1024*1024),
    PROFESSIONAL("专业版",1,1000,1000L*1024*1024),
    FLAGSHIP("旗舰版",2,10000,10000L*1024*1024);

    private final String text;
    private final int value;
    private final long max_Count;
    private final long max_Size;

    /**
     *
     * @param text 文本
     * @param value  值
     * @param max_Count  最大图片信息
     * @param max_Size    最大图片总数量
     */
    SpaceLevelEnum(String text, int value, long max_Count, long max_Size) {
        this.text = text;
        this.value = value;
        this.max_Count = max_Count;
        this.max_Size = max_Size;
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

    public long getMax_Count() {
        return max_Count;
    }

    public long getMax_Size() {
        return max_Size;
    }
}
