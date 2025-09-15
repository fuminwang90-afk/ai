package com.zxtech.ai.service;

import com.alibaba.dashscope.embeddings.TextEmbedding;
import com.alibaba.dashscope.embeddings.TextEmbeddingParam;
import com.alibaba.dashscope.embeddings.TextEmbeddingResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmbeddingService {

    /**
     * 使用 SDK 调用通义千问文本嵌入接口
     *
     * @param text 输入文本
     * @return 向量结果
     */
    public List<Float> getEmbedding(String text) {
        try {

            // 构造参数
            TextEmbeddingParam param = TextEmbeddingParam.builder()
                    .model("text-embedding-v4")
                    .texts(List.of(text))
                    .parameter("dimension", 2048)
                    .build();

            // 调用 SDK 接口
            TextEmbedding embedding = new TextEmbedding();
            TextEmbeddingResult result = embedding.call(param);

            // 从结果中提取向量
            List<Float> vector = new ArrayList<>();
            // 获取向量（返回的是 List<Double>）
            List<Double> doubleList = result.getOutput()
                    .getEmbeddings()
                    .get(0)
                    .getEmbedding();


            // 转换为 List<Float>
            for (Double d : doubleList) {
                vector.add(d.floatValue());
            }

            return vector;

        } catch (ApiException | NoApiKeyException e) {
            throw new RuntimeException("调用 DashScope SDK 获取向量失败", e);
        }
    }


}
