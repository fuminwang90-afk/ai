package com.zxtech.ai.service;

import com.zxtech.ai.service.interfaces.LlmService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RetrievalService{

    @Autowired
    private MilvusService milvusService;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private LocalDataService localDataService; // 访问数据库内容

    @Autowired
    private LlmServiceFactory llmServiceFactory; // 调用 LLM 模型

    @PostConstruct
    public void init() {
        milvusService.initCollection(2048); // DashScope 的向量维度
        // 1. 加载数据库中的文档
        List<String> allTextList = localDataService.getAllTexts();

        // 2. 向量化并写入 Milvus
        for (String text : allTextList) {
            List<Float> embedding = embeddingService.getEmbedding(text);
            milvusService.insert(text, embedding);
        }
        // 3. 创建索引
        milvusService.createIndex("MR_Doc", "MR_Doc_Vec");

        // 4. 加载 collection（可选：提升查询效率）
        milvusService.loadCollection();
    }

    public List<String> searchRelevantContext(String query) {
        List<Float> vec = embeddingService.getEmbedding(query);
        return milvusService.search(vec, 3);
    }


    public List<Map<String, String>> buildMessages(String question, List<String> contextList) {
        StringBuilder sb = new StringBuilder();
        sb.append("请根据以下提供的资料与用户自然对话回答问题。字数需要控制在70个字以内，尽量简介\n\n");
        for (int i = 0; i < contextList.size(); i++) {
            sb.append("【资料").append(i + 1).append("】").append(contextList.get(i)).append("\n");
        }

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", "你是一个专业的家装销售经理、对装修行业有着非常丰富的经验。"));
        messages.add(Map.of("role", "user", "content", sb + "\n用户问题：" + question));
        return messages;
    }


    //  3. 提问并得到模型答案
    public String answerQuestion(String question) {
        List<String> contextList = searchRelevantContext(question);
        //String prompt = buildPrompt(question, contextList);
        List<Map<String, String>> messages = buildMessages(question, contextList);
        LlmService llmService = llmServiceFactory.getService("deepseek");
        return llmService.generateText(messages); // 你需要实现这个 LLM 调用方法
    }


}
