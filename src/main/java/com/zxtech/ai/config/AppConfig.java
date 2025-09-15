package com.zxtech.ai.config;

import io.milvus.client.MilvusClient;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AppConfig {
    @Bean
    public MilvusClient milvusClient() {
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost("localhost") // 修改为你的 Milvus 地址
                .withPort(19530)       // 默认端口
                .build();
        return new MilvusServiceClient(connectParam);
    }
}
