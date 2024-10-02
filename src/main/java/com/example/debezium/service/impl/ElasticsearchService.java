package com.example.debezium.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.example.debezium.handler.event.enums.OperationType;
import com.example.debezium.model.IDataModel;
import com.example.debezium.service.IElasticsearchService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import static com.example.debezium.config.runner.DataModelRegisterRunner.DATA_MODEL_MAP;
import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Service
@AllArgsConstructor
public class ElasticsearchService implements IElasticsearchService {

    private final ObjectMapper objectMapper;
    private final ElasticsearchOperations operations;
    private final ElasticsearchClient client;

    public void process(OperationType operation, String table, String id, String data) {
        log.trace("Elasticsearch Process: {} {} {} {}", operation, table, id, data);
        IDataModel dataModel = DATA_MODEL_MAP.getOrDefault(table, null);
        if (ObjectUtils.isEmpty(dataModel)) {
            log.debug("No DataModel Matched for the Table: {}", table);
            try {
                this.handleData(operation, table, id, data);
            } catch (Exception e) {
                log.error("Data Handle Error: {}", data, e);
            }
            return;
        }
        try {
            IDataModel model = dataModel.convert(objectMapper, data, dataModel.getClass());
            model.setId(id);
            this.handleData(operation, model);
        } catch (JsonProcessingException e) {
            log.error("DataModel Convert Error: {}-{}", dataModel.getClass(), data, e);
        }
    }

    private void handleData(OperationType operation, IDataModel model) {
        switch (operation) {
            case SEARCH, INSERT -> operations.save(model);
            case UPDATE -> operations.update(model);
            case DELETE -> operations.delete(model);
            default -> log.warn("Operation Mismatch: {}", operation);
        }
    }

    @SneakyThrows
    private void handleData(OperationType operation, String table, String id, String data) {
        var index = table.substring(table.lastIndexOf(".") + 1);
        var document = Document.parse(data);
        switch (operation) {
            case SEARCH, INSERT, UPDATE -> {
                var request = IndexRequest.of(builder -> {
                    var idxBuilder = builder
                            .index(index)
                            .document(document);
                    if (hasText(id)) {
                        idxBuilder.id(id);
                    }
                    return idxBuilder;
                });
                var res = client.index(request);
                log.trace("[{}] Index Response: {}", operation, res);
            }
            case DELETE -> {
                var request = DeleteRequest.of(builder -> builder.index(index).id(id));
                var res = client.delete(request);
                log.trace("[{}] Delete Response: {}", operation, res);
            }
            default -> log.warn("Operation Mismatch: {} {}-{}", operation, table, document);
        }
    }
}
