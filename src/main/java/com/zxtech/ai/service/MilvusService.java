package com.zxtech.ai.service;

import io.milvus.client.MilvusClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.SearchResults;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.SearchResultsWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class MilvusService {

    @Autowired
    private MilvusClient milvusClient;

    private final String collectionName = "MR_Doc";

    // 向量的度量类型，统一为 IP（Inner Product）
    private final MetricType metricType = MetricType.IP;

    public void initCollection(int dimension) {
        if (hasCollection()) return;

        FieldType idField = FieldType.newBuilder()
                .withName("id")
                .withDataType(DataType.Int64)
                .withPrimaryKey(true)
                .withAutoID(true)
                .build();

        FieldType contentField = FieldType.newBuilder()
                .withName("content")
                .withDataType(DataType.VarChar)
                .withMaxLength(512)
                .build();

        FieldType vectorField = FieldType.newBuilder()
                .withName("embedding")
                .withDataType(DataType.FloatVector)
                .withDimension(dimension)
                .build();

        CreateCollectionParam createCollectionReq = CreateCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .withDescription("RAG doc collection")
                .withShardsNum(2)
                .addFieldType(idField)
                .addFieldType(contentField)
                .addFieldType(vectorField)
                .build();

        milvusClient.createCollection(createCollectionReq);
    }

    public void insert(String content, List<Float> embedding) {
        List<InsertParam.Field> fields = Arrays.asList(
                new InsertParam.Field("content", Collections.singletonList(content)),
                new InsertParam.Field("embedding", Collections.singletonList(embedding))
        );

        InsertParam param = InsertParam.newBuilder()
                .withCollectionName(collectionName)
                .withFields(fields)
                .build();

        milvusClient.insert(param);
    }

    public List<String> search(List<Float> queryVec, int topK) {
        SearchParam param = SearchParam.newBuilder()
                .withCollectionName(collectionName)
                .withTopK(topK)
                .withVectors(Collections.singletonList(queryVec))
                .withVectorFieldName("embedding")
                .withOutFields(Collections.singletonList("content"))
                .withMetricType(metricType) // 使用统一的 metricType
                .withParams("{\"nprobe\": 10}")
                .build();

        R<SearchResults> response = milvusClient.search(param);
        if (response.getStatus() != R.Status.Success.getCode()) {
            throw new RuntimeException("Milvus 搜索失败：" + response.getMessage());
        }

        SearchResults results =  response.getData();
        SearchResultsWrapper wrapper = new SearchResultsWrapper(results.getResults());

        List<String> hits = new ArrayList<>();
        List dataList = wrapper.getFieldData("content", 0);
        for (Object obj : dataList) {
            if (obj instanceof ByteBuffer) {
                ByteBuffer bb = (ByteBuffer) obj;
                byte[] bytes = new byte[bb.remaining()];
                bb.get(bytes);
                hits.add(new String(bytes, StandardCharsets.UTF_8));
            } else if (obj instanceof String) {
                hits.add((String) obj);
            }
        }
        return hits;
    }

    private boolean hasCollection() {
        R<Boolean> res = milvusClient.hasCollection(HasCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build());
        return res.getData();
    }

    public void loadCollection() {
        R<RpcStatus> result = milvusClient.loadCollection(
                LoadCollectionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build()
        );

        if (result.getStatus() != R.Status.Success.getCode()) {
            throw new RuntimeException("加载 Milvus Collection 失败: " + result.getMessage());
        }
    }

    public void createIndex(String collectionName, String vectorFieldName) {
        CreateIndexParam createIndexParam = CreateIndexParam.newBuilder()
                .withCollectionName(collectionName)
                .withFieldName(vectorFieldName)
                .withIndexType(IndexType.IVF_FLAT)
                .withMetricType(metricType) // 统一使用 IP
                .withExtraParam("{\"nlist\":128}")
                .withSyncMode(Boolean.TRUE)
                .build();

        R<RpcStatus> response = milvusClient.createIndex(createIndexParam);
        if (response.getStatus() != R.Status.Success.getCode()) {
            throw new RuntimeException("创建 Milvus 索引失败: " + response.getMessage());
        }
    }
}
