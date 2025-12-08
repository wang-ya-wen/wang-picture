package com.wang.wangpicture.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.math.BigInteger;

@Configuration
public class JacksonConfig {

    /**
     * 全局配置：Long/long/BigInteger 转 JSON 时序列化为字符串，避免精度丢失
     */
    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        // 1. 创建 ObjectMapper 实例（Jackson 的核心序列化/反序列化工具）
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 2. 创建自定义模块，注册序列化规则
        SimpleModule simpleModule = new SimpleModule();
        
        // 注册 Long 类型序列化器：Long -> String
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        // 注册 long 基本类型序列化器：long -> String
        simpleModule.addSerializer(long.class, ToStringSerializer.instance);
        // 可选：如果有 BigInteger 类型（超大数值），也一起序列化为字符串
        simpleModule.addSerializer(BigInteger.class, ToStringSerializer.instance);
        
        // 3. 将模块注入 ObjectMapper
        objectMapper.registerModule(simpleModule);
        
        // 4. 返回配置后的消息转换器
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }
}