package com.zxtech.ai.service;

import com.zxtech.ai.service.interfaces.LlmService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LlmServiceFactory {
    private final Map<String, LlmService> services;

    // Spring自动注入所有实现了LlmService的Bean
    public LlmServiceFactory(List<LlmService> serviceList) {
        services = serviceList.stream()
                .collect(Collectors.toMap(s -> {
                    // 根据类上的 @Service("name") 取名字，或者自己定义getName接口
                    return s.getClass().getAnnotation(Service.class).value();
                }, s -> s));
    }

    public LlmService getService(String modelName) {
        return services.getOrDefault(modelName, services.get("gpt")); // 默认gpt
    }
}
