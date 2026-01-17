package com.wang.wangpicture;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@MapperScan("com.wang.wangpicture.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class WangPictureApplication {

    public static void main(String[] args) {

        SpringApplication.run(WangPictureApplication.class, args);
    }

}
