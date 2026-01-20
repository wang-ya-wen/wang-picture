package com.wang.wangpicture.manager.websocket.disruptor;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.concurrent.ThreadFactory;

/**
 * 图片编辑事件 Disruptor配置
 */
@Configuration
public class PictureEditEventDisruptorConfig {
    @Resource
    private PictureEditEventWorkHandler pictureEditEventWorkHandler;

    @Bean("pictureEditEventDisruptor")
    public Disruptor<PictureEditEvent> messageModelRingBuffer() {
        //定义ringBuffer的大小  一般适用于百万级别的
        int bufferSize = 1024 * 256;
        //创建Disruptor
        Disruptor<PictureEditEvent> pictureEditEventDisruptor = new Disruptor<>(
                PictureEditEvent::new,
                bufferSize,
                ThreadFactoryBuilder.create()
                        .setNamePrefix("pictureEditEventDisruptor")
                        .build()
        );
        //设置消费者
        pictureEditEventDisruptor.handleEventsWithWorkerPool(pictureEditEventWorkHandler);
        //启动disruptor
        pictureEditEventDisruptor.start();
        return pictureEditEventDisruptor;
    }

}
