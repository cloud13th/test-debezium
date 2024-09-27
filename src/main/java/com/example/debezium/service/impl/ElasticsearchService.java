package com.example.debezium.service.impl;

import com.example.debezium.handler.event.enums.OperationType;
import com.example.debezium.model.IDataModel;
import com.example.debezium.service.IElasticsearchService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import static com.example.debezium.config.init.DataModelRegisterRunner.DATA_MODEL_MAP;

@Slf4j
@Service
@AllArgsConstructor
public class ElasticsearchService implements IElasticsearchService {

    private final ObjectMapper objectMapper;
    private final ElasticsearchOperations operations;

    public void process(OperationType operation, String table, String id, String data) {
        log.debug("Elasticsearch Process: {} {} {} {}", operation, table, id, data);
        IDataModel dataModel = DATA_MODEL_MAP.getOrDefault(table, null);
        if (ObjectUtils.isEmpty(dataModel)) {
            log.debug("No DataModel Matched for the Table: {}", table);
            this.handleData(operation, table, data);
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

    private void handleData(OperationType operation, String index, String data) {
        IndexCoordinates coordinates = IndexCoordinates.of(index);
        switch (operation) {
            case SEARCH, INSERT -> operations.save(data, coordinates);
            case UPDATE -> operations.update(data, coordinates);
            case DELETE -> operations.delete(data, coordinates);
            default -> log.warn("Operation Mismatch: {} {}-{}", operation, index, data);
        }
    }
}
