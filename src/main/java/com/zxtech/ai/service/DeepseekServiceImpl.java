package com.zxtech.ai.service;

import com.zxtech.ai.service.interfaces.LlmService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("deepseek")
public class DeepseekServiceImpl implements LlmService {
    private final RestTemplate restTemplate;
    private final String apiUrl = "https://api.deepseek.com/chat/completions/";

    @Value("${deepseek.api.token}")
    private String apiToken;

    public DeepseekServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public String generateText(List<Map<String, String>> messages) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-chat");
        requestBody.put("messages", messages);
        requestBody.put("stream", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiToken);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, requestEntity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> choice = ((List<Map<String, Object>>) response.getBody().get("choices")).get(0);
                Map<String, Object> message = (Map<String, Object>) choice.get("message");
                return message.get("content").toString();
            } else {
                return "调用 DeepSeek 失败，状态码：" + response.getStatusCodeValue();
            }
        } catch (Exception e) {
            return "调用 DeepSeek 接口异常：" + e.getMessage();
        }
    }
}
