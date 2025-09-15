package com.zxtech.ai.service.interfaces;

import java.util.List;
import java.util.Map;

public interface LlmService {
    String generateText(List<Map<String, String>> messages);
}
