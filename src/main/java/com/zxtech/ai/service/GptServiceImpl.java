package com.zxtech.ai.service;

import com.zxtech.ai.service.interfaces.LlmService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("gpt")
public class GptServiceImpl implements LlmService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    private final String apiUrl = "https://api.openai.com/v1/chat/completions";

    public GptServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }
    @Override
    public String generateText(List<Map<String, String>> messages) {

        // 构造请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); // 你可以改成你想用的模型名

        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 1000);
        requestBody.put("temperature", 0.7);

        // 构造请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, requestEntity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List choices = (List) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map firstChoice = (Map) choices.get(0);
                    Map message = (Map) firstChoice.get("message");
                    if (message != null) {
                        Object content = message.get("content");
                        return content != null ? content.toString() : "GPT返回内容为空";
                    }
                }
                return "GPT返回结果结构异常";
            } else {
                return "调用GPT接口失败，状态码：" + response.getStatusCodeValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "调用GPT接口异常：" + e.getMessage();
        }
    }
}
